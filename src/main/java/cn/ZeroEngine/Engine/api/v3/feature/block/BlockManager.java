package cn.ZeroEngine.Engine.api.v3.feature.block;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义方块注册中心 + 放置位置映射。
 *
 * 物品形式：SBlock 继承 SItem，注册时同步进入 ItemManager，玩家可用 /sfitem give 拿到。
 * 方块形式：放置后用 chunk 的 PersistentDataContainer 记录「区块内相对坐标 -> SBlock id」，
 *          运行期在内存中缓存，findAt(block) O(1) 查回。
 */
public class BlockManager {

    private static final String CHUNK_KEY = "sf_sblocks";
    private static final String SEP_POS = ",";
    private static final String SEP_ENTRY = ";";
    private static final String SEP_ID = ":";

    private final Plugin plugin;
    private final ItemManager itemManager;
    private final NamespacedKey chunkPdcKey;

    private final Map<String, SBlock> blocks = new HashMap<>();

    private final Map<UUID, Map<Long, Map<String, String>>> byWorld = new ConcurrentHashMap<>();

    private final Map<UUID, Map<String, Boolean>> redstoneState = new ConcurrentHashMap<>();

    public BlockManager(Plugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.chunkPdcKey = new NamespacedKey(plugin, CHUNK_KEY);
    }

    public BlockManager register(SBlock block) {
        SF sf = SF.sf();
        String id = block.id();
        if (blocks.containsKey(id)) {
            throw new IllegalStateException("SBlock already registered: " + id);
        }
        blocks.put(id, block);
        if (itemManager != null) {
            itemManager.registerIfAbsent(block);
        }
        sf.info("[Block] Registered: " + id + " (" + block.displayName() + " / " + block.material() + ")");
        return this;
    }

    public boolean registerIfAbsent(SBlock block) {
        if (blocks.containsKey(block.id())) return false;
        try {
            register(block);
            return true;
        } catch (IllegalStateException ignore) {
            return false;
        }
    }

    public BlockManager registerAll(SBlock... blocks) {
        for (SBlock b : blocks) register(b);
        return this;
    }

    public void unregister(String id) { blocks.remove(id); }

    public void unregisterAll() { blocks.clear(); }

    public SBlock get(String id) { return blocks.get(id); }

    public Collection<SBlock> all() { return Collections.unmodifiableCollection(blocks.values()); }

    public SBlock findItem(ItemStack item) {
        if (item == null || itemManager == null) return null;
        return blocks.isEmpty() ? null : blocks.values().stream()
                .filter(b -> b.is(item))
                .findFirst()
                .orElse(null);
    }

    public SBlock findAt(Block block) {
        if (block == null) return null;
        Map<String, String> chunkMap = chunkMapOf(block, false);
        if (chunkMap == null || chunkMap.isEmpty()) return null;
        String id = chunkMap.get(relPosKey(block));
        if (id == null) return null;
        return blocks.get(id);
    }

    public void placeAt(Block block, SBlock blockDef) {
        Map<String, String> chunkMap = chunkMapOf(block, true);
        chunkMap.put(relPosKey(block), blockDef.id());
        saveChunkPdc(block.getChunk(), chunkMap);
    }

    public void removeAt(Block block) {
        Map<String, String> chunkMap = chunkMapOf(block, false);
        if (chunkMap == null || chunkMap.isEmpty()) return;
        if (chunkMap.remove(relPosKey(block)) != null) {
            saveChunkPdc(block.getChunk(), chunkMap);
        }
        redstoneState.values().forEach(m -> m.remove(worldPosKey(block)));
    }

    public Set<java.util.Map.Entry<Block, SBlock>> scanPoweredAround(Block source, int radius) {
        Set<java.util.Map.Entry<Block, SBlock>> out = new HashSet<>();
        if (radius <= 0 || blocks.isEmpty()) return out;
        World w = source.getWorld();
        int sx = source.getX(), sy = source.getY(), sz = source.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = w.getBlockAt(sx + dx, sy + dy, sz + dz);
                    SBlock def = findAt(b);
                    if (def != null) out.add(new java.util.AbstractMap.SimpleEntry<>(b, def));
                }
            }
        }
        return out;
    }

    public Map<String, Boolean> redstoneStatesOf(UUID worldId) {
        return redstoneState.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>());
    }

    public void shutdown() {
        byWorld.clear();
        redstoneState.clear();
    }

    private Map<String, String> chunkMapOf(Block block, boolean create) {
        World w = block.getWorld();
        Map<Long, Map<String, String>> worldMap = byWorld.computeIfAbsent(w.getUID(), k -> new ConcurrentHashMap<>());
        Chunk c = block.getChunk();
        long chunkKey = chunkKey(c.getX(), c.getZ());
        Map<String, String> chunkMap = worldMap.get(chunkKey);
        if (chunkMap != null) return chunkMap;
        chunkMap = loadChunkPdc(c);
        if (chunkMap == null) {
            if (!create) return null;
            chunkMap = new HashMap<>();
        }
        worldMap.put(chunkKey, chunkMap);
        return chunkMap;
    }

    private Map<String, String> loadChunkPdc(Chunk c) {
        PersistentDataContainer pdc = c.getPersistentDataContainer();
        String raw;
        try { raw = pdc.get(chunkPdcKey, PersistentDataType.STRING); } catch (Throwable ignore) { return null; }
        if (raw == null || raw.isEmpty()) return null;
        Map<String, String> map = new HashMap<>();
        for (String entry : raw.split(SEP_ENTRY)) {
            if (entry.isEmpty()) continue;
            int idx = entry.lastIndexOf(SEP_ID);
            if (idx <= 0) continue;
            String pos = entry.substring(0, idx);
            String id = entry.substring(idx + 1);
            if (!pos.isEmpty() && !id.isEmpty()) map.put(pos, id);
        }
        return map.isEmpty() ? null : map;
    }

    private void saveChunkPdc(Chunk c, Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            c.getPersistentDataContainer().remove(chunkPdcKey);
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(SEP_ENTRY);
            sb.append(e.getKey()).append(SEP_ID).append(e.getValue());
            first = false;
        }
        try {
            c.getPersistentDataContainer().set(chunkPdcKey, PersistentDataType.STRING, sb.toString());
        } catch (Throwable t) {
            SF.sf().error("[Block] saveChunkPdc failed @ " + c + ": " + t.getMessage());
        }
    }

    private static String relPosKey(Block b) {
        return (b.getX() & 15) + SEP_POS + b.getY() + SEP_POS + (b.getZ() & 15);
    }

    private static String worldPosKey(Block b) {
        return b.getWorld().getUID() + SEP_POS + b.getX() + SEP_POS + b.getY() + SEP_POS + b.getZ();
    }

    private static long chunkKey(int cx, int cz) {
        return (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
    }
}
