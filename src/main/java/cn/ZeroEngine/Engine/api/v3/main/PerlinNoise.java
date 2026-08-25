package cn.ZeroEngine.Engine.api.v3.main;

import java.util.Random;

/**
 * Perlin Noise 工具类（含 fractal Brownian Motion / 多 octave）
 *
 * <p>支持：</p>
 * <ul>
 *   <li>2D / 3D Perlin 噪声采样</li>
 *   <li>多 octave 叠加（fbm），让噪声更自然、有层次</li>
 *   <li>可调 seed / scale / octaves / persistence / lacunarity</li>
 *   <li>线程安全（无状态实例，可跨线程复用）</li>
 * </ul>
 *
 * <p>典型用途：</p>
 * <ul>
 *   <li>SBiome 模块：决定 chunk 是否替换为自定义生物群系</li>
 *   <li>SEntity 模块：自定义怪物的自然生成分布</li>
 *   <li>SItem 模块：自定义粒子分布</li>
 *   <li>地形改造：决定 chunk 内的方块高度起伏</li>
 * </ul>
 *
 * <p>用法：</p>
 * <pre>
 *   PerlinNoise noise = new PerlinNoise(12345L, 0.05, 4, 0.5, 2.0);
 *   double v = noise.noise2D(chunkX, chunkZ);          // 2D 采样，范围约 [-1, 1]
 *   double v3 = noise.noise3D(x, y, z);                 // 3D 采样
 *   double fbm = noise.fbm2D(x, z);                    // 2D fbm（更自然）
 *   double fbm3 = noise.fbm3D(x, y, z);                // 3D fbm
 * </pre>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
public class PerlinNoise {

    private final int[] perm = new int[512];
    private final long seed;
    private final double scale;
    private final int octaves;
    private final double persistence;  // 每个 octave 的振幅衰减
    private final double lacunarity;   // 每个 octave 的频率倍增

    /**
     * 默认构造：seed + scale + 4 octaves + persistence 0.5 + lacunarity 2.0
     */
    public PerlinNoise(long seed, double scale) {
        this(seed, scale, 4, 0.5, 2.0);
    }

    /**
     * 完整参数构造
     *
     * @param seed        随机种子
     * @param scale       采样尺度（值小 = 大块连续，值大 = 碎裂）
     * @param octaves     octave 数量（1-8 推荐，越多越细腻但越慢）
     * @param persistence 每个 octave 振幅衰减（0~1，0.5 = 每层振幅减半）
     * @param lacunarity  每个 octave 频率倍增（通常 2.0）
     */
    public PerlinNoise(long seed, double scale, int octaves, double persistence, double lacunarity) {
        this.seed = seed;
        this.scale = scale;
        this.octaves = Math.max(1, Math.min(8, octaves));
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        // 用 seed 初始化置换表（Ken Perlin 原版算法）
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        Random rnd = new Random(seed);
        // Fisher-Yates 洗牌
        for (int i = 255; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        // 复制到 512 长度的置换表（避免 mod 取负数）
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    // ==================== 单层采样 ====================

    /**
     * 2D Perlin Noise 单层采样，范围约 [-1, 1]
     */
    public double noise2D(double x, double z) {
        double xs = x * scale;
        double zs = z * scale;
        int X = (int) Math.floor(xs) & 255;
        int Z = (int) Math.floor(zs) & 255;
        double xf = xs - Math.floor(xs);
        double zf = zs - Math.floor(zs);
        double u = fade(xf);
        double v = fade(zf);
        int aa = perm[perm[X] + Z];
        int ab = perm[perm[X] + Z + 1];
        int ba = perm[perm[X + 1] + Z];
        int bb = perm[perm[X + 1] + Z + 1];
        double x1 = lerp(grad(aa, xf, zf),        grad(ba, xf - 1, zf),     u);
        double x2 = lerp(grad(ab, xf, zf - 1),    grad(bb, xf - 1, zf - 1), u);
        return lerp(x1, x2, v);  // 范围约 [-1, 1]
    }

    /**
     * 3D Perlin Noise 单层采样，范围约 [-1, 1]
     */
    public double noise3D(double x, double y, double z) {
        double xs = x * scale;
        double ys = y * scale;
        double zs = z * scale;
        int X = (int) Math.floor(xs) & 255;
        int Y = (int) Math.floor(ys) & 255;
        int Z = (int) Math.floor(zs) & 255;
        double xf = xs - Math.floor(xs);
        double yf = ys - Math.floor(ys);
        double zf = zs - Math.floor(zs);
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);
        int A  = perm[X] + Y;
        int AA = perm[A] + Z;
        int AB = perm[A + 1] + Z;
        int B  = perm[X + 1] + Y;
        int BA = perm[B] + Z;
        int BB = perm[B + 1] + Z;
        double x1 = lerp(grad(perm[AA], xf, yf, zf),     grad(perm[BA], xf - 1, yf, zf),     u);
        double x2 = lerp(grad(perm[AB], xf, yf - 1, zf),  grad(perm[BB], xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);
        double x3 = lerp(grad(perm[AA + 1], xf, yf, zf - 1),     grad(perm[BA + 1], xf - 1, yf, zf - 1),     u);
        double x4 = lerp(grad(perm[AB + 1], xf, yf - 1, zf - 1),grad(perm[BB + 1], xf - 1, yf - 1, zf - 1),u);
        double y2 = lerp(x3, x4, v);
        return lerp(y1, y2, w);
    }

    // ==================== 多 octave（fbm）====================

    /**
     * 2D fractal Brownian Motion（多 octave 叠加，更自然的噪声）
     * 范围约 [-1, 1]，但实际值会因为叠加衰减更集中在 [-0.5, 0.5]
     */
    public double fbm2D(double x, double z) {
        double total = 0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double maxAmp = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise2D(x * frequency, z * frequency) * amplitude;
            maxAmp += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxAmp;  // 归一化到 [-1, 1]
    }

    /**
     * 3D fractal Brownian Motion
     */
    public double fbm3D(double x, double y, double z) {
        double total = 0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double maxAmp = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise3D(x * frequency, y * frequency, z * frequency) * amplitude;
            maxAmp += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxAmp;
    }

    // ==================== 工具方法 ====================

    /** 把 [-1, 1] 映射到 [0, 1] */
    public double normalized2D(double x, double z) {
        return (fbm2D(x, z) + 1.0) * 0.5;
    }

    /** 把 [-1, 1] 映射到 [0, 1] */
    public double normalized3D(double x, double y, double z) {
        return (fbm3D(x, y, z) + 1.0) * 0.5;
    }

    /** 取种子 */
    public long getSeed() { return seed; }
    public double getScale() { return scale; }
    public int getOctaves() { return octaves; }

    // ==================== Perlin 原始工具 ====================

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /** 2D 梯度函数 */
    private static double grad(int hash, double x, double z) {
        switch (hash & 7) {
            case 0: return  x + z;
            case 1: return -x + z;
            case 2: return  x - z;
            case 3: return -x - z;
            case 4: return  x;
            case 5: return -x;
            case 6: return  z;
            default: return -z;
        }
    }

    /** 3D 梯度函数（Ken Perlin 12 边向量） */
    private static double grad(int hash, double x, double y, double z) {
        switch (hash & 15) {
            case 0: return  x + y + z;
            case 1: return -x + y + z;
            case 2: return  x - y + z;
            case 3: return -x - y + z;
            case 4: return  x + y - z;
            case 5: return -x + y - z;
            case 6: return  x - y - z;
            case 7: return -x - y - z;
            case 8: return  x + y;
            case 9: return -x + y;
            case 10:return  x - y;
            case 11:return -x - y;
            case 12:return  x + z;
            case 13:return -x + z;
            case 14:return  z - y;
            default:return y - z;
        }
    }
}
