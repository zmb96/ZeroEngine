package cn.ZeroEngine.Engine.api.v3.feature.biome;

import cn.ZeroEngine.Engine.api.v3.main.PerlinNoise;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 自定义生物群系抽象基类 —— 用户继承此类、重写方法，实现自己的生物群系
 *
 * <p>本系统采用「覆盖式」实现：用原版 {@link Biome} 作为「视觉底子」
 * （客户端显示草色、水色、雾色都按这个底子走），通过 SBiome 的各种钩子
 * 注入自定义的怪物生成、装饰物、特效、玩家进入/退出事件等。</p>
 *
 * <p>触发时机：监听 {@link org.bukkit.event.world.ChunkPopulateEvent}。
 * 该事件在 chunk 第一次被生成完成时触发，<b>已经生成过的 chunk 不会再触发</b>
 * —— 这正好对应「存档已存在但未探索的区块」：玩家靠近、服务端首次生成该
 * chunk 时就会调到 {@link #onChunkPopulate(World, int, int, Random)}，可以
 * 在此时插入新生物群系。</p>
 *
 * <p>分布策略：使用 {@link PerlinNoise}（Perlin 噪声 + fbm 多 octave 叠加）
 * 决定哪些 chunk 替换。如果噪声值 > {@link #noiseThreshold()}，则该 chunk
 * 会被覆盖为目标 biome。这避免了「整个世界都是自定义 biome」的极端情况，
 * 让分布更自然。子类可以重写 {@link #createNoise()} 自定义噪声参数。</p>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
public abstract class SBiome {

    /** 缓存的 PerlinNoise 实例（懒加载）*/
    private PerlinNoise cachedNoise;

    // ==================== 基础元信息 ====================

    /**
     * 自定义生物群系唯一 id（用于 PDC 标记 chunk）
     * 必须是 [a-z0-9_] 字符（用于 NamespacedKey）
     */
    public abstract String id();

    /**
     * 中文名（用于命令显示、玩家消息）
     */
    public abstract String displayName();

    /**
     * 用哪个原版 Biome 作为「视觉底子」
     * 例如 Biome.MUSHROOM_FIELDS 会让客户端显示菌丝草色 + 紫色水
     *
     * <p>如果想做"完全不像原版"的群系，可以选一个视觉相近的底子，
     * 然后在 {@link #onChunkPopulate} 里大量覆盖方块、装饰、特效，
     * 让玩家几乎认不出原版底子。</p>
     */
    public abstract Biome targetBiome();

    // ==================== 分布条件 ====================

    /**
     * 在哪些世界生效（默认主世界）
     * 例如 Arrays.asList("world", "world_world")
     */
    public List<String> worlds() {
        return Arrays.asList("world");
    }

    /**
     * 在哪些原版 biome 上替换
     * 例如 Arrays.asList(Biome.PLAINS, Biome.FOREST)
     * 只有该 chunk 原本就是这些 biome 之一，才会被替换
     */
    public List<Biome> replaces() {
        return Arrays.asList(Biome.PLAINS);
    }

    /**
     * 替换权重：多个 SBiome 竞争同一 chunk 时的优先级
     * 数值越大越优先（默认 1.0）
     */
    public double weight() {
        return 1.0;
    }

    /**
     * 噪声阈值（0.0 - 1.0）
     * 当 PerlinNoise.normalized2D(chunkX, chunkZ) > 此阈值时，才替换为自定义 biome
     * - 0.0：所有匹配的 chunk 都替换（与 replaces 匹配的）
     * - 0.5：约一半匹配的 chunk 替换（默认）
     * - 0.9：只有少数 chunk 替换（更稀有）
     */
    public double noiseThreshold() {
        return 0.5;
    }

    /**
     * 噪声种子偏移（让多个 SBiome 的分布互相错开）
     * 默认用 id().hashCode()，可以自定义
     */
    public long noiseSeed() {
        return id().hashCode();
    }

    /**
     * 噪声采样尺度（值越大 = 每个 chunk 都独立判断，分布更碎；
     *              值越小 = 大块连续区域，分布更整块）
     * 默认 0.05 ≈ 每 20 个 chunk 一个周期
     */
    public double noiseScale() {
        return 0.05;
    }

    /**
     * 噪声 octave 数量（1-8）
     * 1 = 单层噪声，分布简单粗糙
     * 4 = 推荐，自然有层次
     * 8 = 极致细节，但 CPU 略高
     */
    public int noiseOctaves() {
        return 4;
    }

    /**
     * 噪声 persistence（每个 octave 的振幅衰减，0~1）
     * 0.5 = 推荐，每层振幅减半
     * 0.3 = 高频细节少，分布更平滑
     * 0.7 = 高频细节多，分布更碎
     */
    public double noisePersistence() {
        return 0.5;
    }

    /**
     * 噪声 lacunarity（每个 octave 的频率倍增，通常 2.0）
     */
    public double noiseLacunarity() {
        return 2.0;
    }

    /**
     * 创建 PerlinNoise 实例（懒加载，缓存）
     * 子类可以重写此方法自定义噪声参数（如改用 3D 噪声）
     */
    public PerlinNoise createNoise() {
        if (cachedNoise == null) {
            cachedNoise = new PerlinNoise(noiseSeed(), noiseScale(),
                    noiseOctaves(), noisePersistence(), noiseLacunarity());
        }
        return cachedNoise;
    }

    // ==================== Y 范围 ====================

    /**
     * 应用 biome 的 Y 范围（biome 是 3D 的）
     * 默认覆盖 -64 到 320（全高度）
     * 可以重写为只覆盖地表：Arrays.asList(new int[]{64, 128})
     */
    public int[] yRange() {
        return new int[]{-64, 320};
    }

    // ==================== 自定义内容钩子 ====================

    /**
     * 新区块生成时的钩子 —— 此处 chunk 已生成、biome 已替换完成
     * 可以在这里塞自定义的方块装饰、宝箱、生成怪物等
     *
     * @param world  世界
     * @param chunkX chunk X 坐标（chunk 单位，1 chunk = 16 block）
     * @param chunkZ chunk Z 坐标
     * @param random 该 chunk 的随机数（保证同一 chunk 多次调用结果一致）
     */
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        // 默认：啥也不做。子类按需重写。
    }

    /**
     * 玩家进入此 biome 区域（chunk 切换为自定义 biome）时触发
     * 可以发欢迎消息、上药水效果、播放音效等
     */
    public void onPlayerEnter(Player player, Chunk chunk) {
        // 默认：啥也不做。
    }

    /**
     * 玩家离开此 biome 区域时触发
     */
    public void onPlayerLeave(Player player, Chunk chunk) {
        // 默认：啥也不做。
    }

    /**
     * 每秒触发（在已被此 biome 覆盖的 chunk 内的玩家）
     * 适合做持续效果（如毒气、加血等）
     */
    public void onPerSecond(Player player, Location loc) {
        // 默认：啥也不做。
    }

    // ==================== 工具方法 ====================

    /**
     * 判断某 chunk 是否应该被此 SBiome 替换
     * 由 BiomeManager 调用，子类一般不需要重写
     *
     * @param world    当前世界
     * @param chunkX   chunk X 坐标
     * @param chunkZ   chunk Z 坐标
     * @param current  当前 chunk 中心的 biome
     * @return true = 应该替换
     */
    public boolean shouldReplace(World world, int chunkX, int chunkZ, Biome current) {
        // 1) 世界匹配
        if (!worlds().contains(world.getName())) return false;
        // 2) 当前 biome 在 replaces 列表内
        if (!replaces().contains(current)) return false;
        // 3) PerlinNoise 超过阈值
        double n = createNoise().normalized2D(chunkX, chunkZ);  // [0, 1]
        return n > noiseThreshold();
    }

    /**
     * 采样 chunk 位置的归一化噪声值 [0, 1]
     * 子类在 onChunkPopulate 里可以用这个做地表起伏、装饰密度等
     */
    public double sampleNoiseAt(int chunkX, int chunkZ) {
        return createNoise().normalized2D(chunkX, chunkZ);
    }

    /**
     * 3D 噪声采样 [0, 1]，用于在 chunk 内做 3D 装饰分布
     */
    public double sampleNoise3D(int x, int y, int z) {
        return createNoise().normalized3D(x, y, z);
    }
}

