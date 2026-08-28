package cn.ZeroEngine.Engine.api.v3.main;

import java.util.Random;


public class PerlinNoise {

    private final int[] perm = new int[512];
    private final long seed;
    private final double scale;
    private final int octaves;
    private final double persistence;  
    private final double lacunarity;   

    
    public PerlinNoise(long seed, double scale) {
        this(seed, scale, 4, 0.5, 2.0);
    }

    
    public PerlinNoise(long seed, double scale, int octaves, double persistence, double lacunarity) {
        this.seed = seed;
        this.scale = scale;
        this.octaves = Math.max(1, Math.min(8, octaves));
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        Random rnd = new Random(seed);
        
        for (int i = 255; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    

    
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
        return lerp(x1, x2, v);  
    }

    
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
        return total / maxAmp;  
    }

    
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

    

    
    public double normalized2D(double x, double z) {
        return (fbm2D(x, z) + 1.0) * 0.5;
    }

    
    public double normalized3D(double x, double y, double z) {
        return (fbm3D(x, y, z) + 1.0) * 0.5;
    }

    
    public long getSeed() { return seed; }
    public double getScale() { return scale; }
    public int getOctaves() { return octaves; }

    

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    
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

