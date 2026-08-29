package cn.ZeroEngine.Engine.api.v3.feature.crop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义农作物注册中心 + 位置映射。
 *
 * 作物方块用 vanilla Ageable Material，通过 chunk PDC 记录 "relX,relY,relZ -> cropId"
 * 区分不同自定义作物，重启后自动恢复。
 *
 * 注册：sf.crops().register(new TomatoCrop())
 * 查找：sf.crops().findAt(block)  O(1) 查作物身份
 */
public class CropManager {

    private static final String PDC_PREFIX = "sfcrop_";

    private final Plugin plugin;
    private final SF sf;
    private final Map<String, SCrop> registry = new HashMap<>();
    private final Map<String, NamespacedKey> chunkKeyCache = new ConcurrentHashMap<>();
    private final Set<Location> plantedCrops = ConcurrentHashMap.newKeySet();
    private BukkitRunnable growthTask;

    public CropManager(Plugin plugin) {
        this.plugin = plugin;
        this.sf = SF.sf();
        startGrowthTask();
    }

    private void startGrowthTask() {
        growthTask = new BukkitRunnable() {
            @Override public void run() {
                if (plantedCrops.isEmpty()) return;
                for (Location loc : plantedCrops) {
                    try {
                        Block b = loc.getBlock();
                        String cropId = readCropId(b);
                        if (cropId == null) { plantedCrops.remove(loc); continue; }
                        SCrop crop = registry.get(cropId);
                        if (crop == null) { plantedCrops.remove(loc); continue; }
                        if (!crop.isStageMode()) continue;
                        if (crop.isMature(b)) continue;
                        if (!crop.canGrowAt(b)) continue;
                        if (Math.random() >= crop.growthChance()) continue;
                        crop.growOneStep(b);
                    } catch (Throwable ignore) {}
                }
            }
        };
        growthTask.runTaskTimer(plugin, 100L, 100L);
    }

    public void indexCrop(Block block) {
        if (block != null) plantedCrops.add(block.getLocation());
    }

    public void unindexCrop(Block block) {
        if (block != null) plantedCrops.remove(block.getLocation());
    }

    public void scanChunk(Chunk chunk) {
        try {
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            for (NamespacedKey key : pdc.getKeys()) {
                String name = key.getKey();
                if (!name.startsWith(PDC_PREFIX)) continue;
                String cropId = pdc.get(key, PersistentDataType.STRING);
                if (cropId == null) continue;
                String rel = name.substring(PDC_PREFIX.length());
                String[] parts = rel.split("_");
                if (parts.length != 3) continue;
                int rx = Integer.parseInt(parts[0]);
                int ry = Integer.parseInt(parts[1]);
                int rz = Integer.parseInt(parts[2]);
                int bx = (chunk.getX() << 4) + rx;
                int bz = (chunk.getZ() << 4) + rz;
                Block b = chunk.getBlock(rx, ry, rz);
                if (b.getType().isAir()) { pdc.remove(key); continue; }
                plantedCrops.add(b.getLocation());
            }
        } catch (Throwable t) {
            sf.error("[Crop] scanChunk failed", t);
        }
    }

    public CropManager register(SCrop crop) {
        String id = crop.id();
        if (registry.containsKey(id)) {
            throw new IllegalStateException("SCrop already registered: " + id);
        }
        registry.put(id, crop);
        sf.info("[Crop] Registered: " + id + " (" + crop.displayName() + ", block=" + crop.cropBlock() + ", maxStage=" + crop.maxStage() + ")");
        return this;
    }

    public boolean registerIfAbsent(SCrop crop) {
        if (registry.containsKey(crop.id())) return false;
        try { register(crop); return true; }
        catch (IllegalStateException ignore) { return false; }
    }

    public void unregister(String id) { registry.remove(id); }

    public void unregisterAll() { registry.clear(); }

    public SCrop get(String id) { return registry.get(id); }

    public Collection<SCrop> all() { return Collections.unmodifiableCollection(registry.values()); }

    public SCrop findAt(Block block) {
        if (block == null) return null;
        String cropId = readCropId(block);
        return cropId == null ? null : registry.get(cropId);
    }

    public boolean placeAt(Block block, SCrop crop, int stage) {
        if (block == null || crop == null) return false;
        if (!crop.placeAt(block, stage)) return false;
        writeCropId(block, crop.id());
        indexCrop(block);
        return true;
    }

    public boolean removeAt(Block block) {
        if (block == null) return false;
        if (readCropId(block) == null) return false;
        clearCropId(block);
        unindexCrop(block);
        return true;
    }

    private String chunkKey(Block block) {
        Chunk chunk = block.getChunk();
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    private int relX(Block b) { return b.getX() & 15; }
    private int relY(Block b) { return b.getY() & 15; }
    private int relZ(Block b) { return b.getZ() & 15; }

    private String relKey(Block b) {
        return relX(b) + "_" + relY(b) + "_" + relZ(b);
    }

    private void writeCropId(Block block, String cropId) {
        try {
            ensureLoaded(block);
            org.bukkit.Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            pdc.set(keyFor(relKey(block)), PersistentDataType.STRING, cropId);
        } catch (Throwable t) {
            sf.error("[Crop] writeCropId failed at " + loc(block), t);
        }
    }

    private String readCropId(Block block) {
        try {
            org.bukkit.Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            String v = pdc.get(keyFor(relKey(block)), PersistentDataType.STRING);
            if (v == null) return null;
            if (!registry.containsKey(v)) return null;
            Material actual = block.getType();
            if (actual != registry.get(v).cropBlock()) return null;
            return v;
        } catch (Throwable ignore) {
            return null;
        }
    }

    private void clearCropId(Block block) {
        try {
            org.bukkit.Chunk chunk = block.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            pdc.remove(keyFor(relKey(block)));
        } catch (Throwable ignore) {}
    }

    private NamespacedKey keyFor(String relKey) {
        return chunkKeyCache.computeIfAbsent(PDC_PREFIX + relKey,
                k -> new NamespacedKey(plugin, k));
    }

    private void ensureLoaded(Block block) {
        if (!block.getChunk().isLoaded()) block.getChunk().load();
    }

    private String loc(Block b) {
        return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    public void shutdown() {
        if (growthTask != null) { try { growthTask.cancel(); } catch (Throwable ignore) {} }
        plantedCrops.clear();
        registry.clear();
        chunkKeyCache.clear();
    }
}
