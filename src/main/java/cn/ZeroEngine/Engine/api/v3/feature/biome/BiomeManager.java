package cn.ZeroEngine.Engine.api.v3.feature.biome;

import cn.ZeroEngine.Engine.api.v3.SF;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 自定义生物群系注册中心 + 应用器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>注册 / 查询 SBiome（按 id）</li>
 *   <li>对新生成的 chunk 应用 biome 替换：把目标 chunk 的每个 (x, y, z) 的 Biome
 *       改为 SBiome.targetBiome()</li>
 *   <li>用 PDC 标记已处理的 chunk，避免重复处理</li>
 *   <li>追踪玩家所在 chunk，触发 onPlayerEnter / onPlayerLeave / onPerSecond 钩子</li>
 * </ul>
 *
 * <p>触发时机：监听 {@link org.bukkit.event.world.ChunkPopulateEvent} ——
 * 该事件在 chunk <b>第一次被生成完成时</b>触发，已经生成过的 chunk 不会再触发。
 * 这正好对应「存档已存在但未探索的区块」：玩家靠近、服务端首次生成该 chunk
 * 时就会调到 {@link #handleChunkPopulate(World, int, int)}，可以在此插入
 * 新生物群系。</p>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
public class BiomeManager {

    /** 已注册的 SBiome，key = id() */
    private final Map<String, SBiome> registry = new HashMap<>();

    /** 玩家上一次所在的 chunk，用于触发 onPlayerLeave */
    private final Map<UUID, Long> playerLastChunkKey = new HashMap<>();

    /** 玩家上一次所在的 SBiome id，用于触发 onPlayerEnter / onPlayerLeave */
    private final Map<UUID, String> playerLastBiome = new HashMap<>();

    /** PDC key：标记 chunk 已被某 SBiome 处理 */
    private final org.bukkit.NamespacedKey processedKey;

    private final SF sf;

    public BiomeManager(SF sf) {
        this.sf = sf;
        this.processedKey = new org.bukkit.NamespacedKey(sf.plugin(), "biome_processed");
    }

    // ==================== 注册 / 查询 ====================

    public void register(SBiome biome) {
        String id = biome.id();
        if (registry.containsKey(id)) {
            throw new IllegalStateException("Biome already registered: " + id);
        }
        registry.put(id, biome);
        sf.info("[Biome] Registered: " + id + " (" + biome.displayName() + ") target="
                + biome.targetBiome() + " replaces=" + biome.replaces());
    }

    /** 不存在才注册；已存在静默返回 false */
    public boolean registerIfAbsent(SBiome biome) {
        if (registry.containsKey(biome.id())) return false;
        register(biome);
        return true;
    }

    public SBiome get(String id) {
        return registry.get(id);
    }

    public Collection<SBiome> all() {
        return registry.values();
    }

    public void unregister(String id) {
        registry.remove(id);
    }

    public void unregisterAll() {
        registry.clear();
        playerLastChunkKey.clear();
        playerLastBiome.clear();
    }

    // ==================== chunk 处理 ====================

    /**
     * 处理一个刚生成的 chunk：根据所有已注册 SBiome 的条件，决定是否替换 biome
     *
     * <p>调用时机：BiomeListener 在 ChunkPopulateEvent 中调用</p>
     *
     * @param world  当前世界
     * @param chunkX chunk X 坐标
     * @param chunkZ chunk Z 坐标
     * @return 被应用的 SBiome（如果有），否则 null
     */
    public SBiome handleChunkPopulate(World world, int chunkX, int chunkZ) {
        if (registry.isEmpty()) return null;

        // 1) 取 chunk 中心点的当前 biome 作为参考
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        Biome currentBiome;
        try {
            currentBiome = world.getBiome(blockX, 0, blockZ);
        } catch (Throwable t) {
            // 某些 Paper 版本要求 biome 调用必须在主线程，ChunkPopulateEvent 是主线程
            // 这里 catch 防御性编程
            sf.error("[Biome] getBiome failed at (" + blockX + ", 0, " + blockZ + ")", t);
            return null;
        }

        // 2) 遍历所有 SBiome，找到第一个匹配的（按 weight 倒序、首个匹配胜出）
        SBiome picked = null;
        double pickedWeight = -1;
        for (SBiome b : registry.values()) {
            if (b.shouldReplace(world, chunkX, chunkZ, currentBiome)) {
                if (b.weight() > pickedWeight) {
                    picked = b;
                    pickedWeight = b.weight();
                }
            }
        }
        if (picked == null) return null;

        // 3) 检查 chunk 是否已被处理过（PDC 标记）
        //    Chunk 是 PersistentDataHolder，可以挂 PDC
        boolean loaded = world.isChunkLoaded(chunkX, chunkZ);
        if (!loaded) {
            // ChunkPopulateEvent 时 chunk 还没"load"完，但 chunk 对象已存在
            // 我们在 chunk load 完成后再处理；这里先记下 chunk 坐标
            // 实际上 ChunkPopulateEvent 触发时 chunk 对象已经可用
        }

        Chunk chunk;
        try {
            chunk = world.getChunkAt(chunkX, chunkZ);
        } catch (Throwable t) {
            sf.error("[Biome] getChunkAt failed at (" + chunkX + ", " + chunkZ + ")", t);
            return null;
        }

        // 4) 用 PDC 检查是否已处理（防止重复处理）
        if (isChunkProcessed(chunk, picked.id())) {
            return null;  // 已经被该 SBiome 处理过了
        }

        // 5) 应用 biome 替换：把 chunk 范围内的每个 y/x/z 都改成 picked.targetBiome()
        Biome target = picked.targetBiome();
        int[] yr = picked.yRange();
        int minY = Math.max(yr[0], world.getMinHeight());
        int maxY = Math.min(yr[1], world.getMaxHeight());
        try {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int wx = chunkX * 16 + x;
                    int wz = chunkZ * 16 + z;
                    for (int y = minY; y <= maxY; y += 4) {
                        // 每 4 格采样一次（biome 是 4x4x4 的网格），节省时间
                        // Paper 26.1.2 接受 setBiome 任意精度但内部按 4x4 处理
                        world.setBiome(wx, y, wz, target);
                    }
                }
            }
        } catch (Throwable t) {
            sf.error("[Biome] setBiome failed for " + picked.id()
                    + " at chunk (" + chunkX + ", " + chunkZ + ")", t);
            // 即使 setBiome 失败也继续触发 onChunkPopulate，让用户的钩子能跑
        }

        // 6) 标记 chunk 已被该 SBiome 处理
        markChunkProcessed(chunk, picked.id());

        // 7) 触发 SBiome 的 chunk 钩子
        try {
            Random random = new Random(chunkX * 341873128712L + chunkZ * 132897987541L);
            picked.onChunkPopulate(world, chunkX, chunkZ, random);
        } catch (Throwable t) {
            sf.error("[Biome] onChunkPopulate error for " + picked.id(), t);
        }

        sf.info("[Biome] Applied " + picked.id() + " to chunk (" + chunkX + ", " + chunkZ
                + ") world=" + world.getName());
        return picked;
    }

    /**
     * 处理玩家所在的 chunk（在 PlayerMoveEvent 或 PlayerChunkEvent 中调用）
     * 触发 onPlayerEnter / onPlayerLeave / onPerSecond
     */
    public void handlePlayerLocation(org.bukkit.entity.Player player) {
        if (player == null || !player.isOnline()) return;
        Chunk chunk = player.getLocation().getChunk();
        if (chunk == null) return;

        // 取 chunk 中心 biome 看是否被某 SBiome 覆盖（按 PDC）
        String currentBiomeId = getChunkBiomeId(chunk);
        String lastBiomeId = playerLastBiome.get(player.getUniqueId());

        // 切换 biome
        if (!java.util.Objects.equals(currentBiomeId, lastBiomeId)) {
            // 离开旧 biome
            if (lastBiomeId != null) {
                SBiome last = registry.get(lastBiomeId);
                if (last != null) {
                    try { last.onPlayerLeave(player, chunk); }
                    catch (Throwable t) { sf.error("[Biome] onPlayerLeave error", t); }
                }
            }
            // 进入新 biome
            if (currentBiomeId != null) {
                SBiome cur = registry.get(currentBiomeId);
                if (cur != null) {
                    try { cur.onPlayerEnter(player, chunk); }
                    catch (Throwable t) { sf.error("[Biome] onPlayerEnter error", t); }
                }
            }
            playerLastBiome.put(player.getUniqueId(), currentBiomeId);
        }

        // 每秒 tick 钩子（在 BiomeListener 用 SFTick 节流）
        if (currentBiomeId != null) {
            SBiome cur = registry.get(currentBiomeId);
            if (cur != null) {
                try { cur.onPerSecond(player, player.getLocation()); }
                catch (Throwable t) { sf.error("[Biome] onPerSecond error", t); }
            }
        }
    }

    // ==================== PDC 标记 ====================

    /** chunk 是否已被某 SBiome 处理过（按 id 区分） */
    public boolean isChunkProcessed(Chunk chunk, String biomeId) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String v = pdc.get(processedKey, PersistentDataType.STRING);
        return biomeId.equals(v);
    }

    /** 标记 chunk 已被某 SBiome 处理 */
    public void markChunkProcessed(Chunk chunk, String biomeId) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(processedKey, PersistentDataType.STRING, biomeId);
    }

    /** 查询 chunk 上次被哪个 SBiome 处理（null = 没被处理过） */
    public String getChunkBiomeId(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        return pdc.get(processedKey, PersistentDataType.STRING);
    }
}
