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


public abstract class SBiome {

    
    private PerlinNoise cachedNoise;

    

    
    public abstract String id();

    
    public abstract String displayName();

    
    public abstract Biome targetBiome();

    

    
    public List<String> worlds() {
        return Arrays.asList("world");
    }

    
    public List<Biome> replaces() {
        return Arrays.asList(Biome.PLAINS);
    }

    
    public double weight() {
        return 1.0;
    }

    
    public double noiseThreshold() {
        return 0.5;
    }

    
    public long noiseSeed() {
        return id().hashCode();
    }

    
    public double noiseScale() {
        return 0.05;
    }

    
    public int noiseOctaves() {
        return 4;
    }

    
    public double noisePersistence() {
        return 0.5;
    }

    
    public double noiseLacunarity() {
        return 2.0;
    }

    
    public PerlinNoise createNoise() {
        if (cachedNoise == null) {
            cachedNoise = new PerlinNoise(noiseSeed(), noiseScale(),
                    noiseOctaves(), noisePersistence(), noiseLacunarity());
        }
        return cachedNoise;
    }

    

    
    public int[] yRange() {
        return new int[]{-64, 320};
    }

    

    
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        
    }

    
    public void onPlayerEnter(Player player, Chunk chunk) {
        
    }

    
    public void onPlayerLeave(Player player, Chunk chunk) {
        
    }

    
    public void onPerSecond(Player player, Location loc) {
        
    }

    

    
    public boolean shouldReplace(World world, int chunkX, int chunkZ, Biome current) {
        
        if (!worlds().contains(world.getName())) return false;
        
        if (!replaces().contains(current)) return false;
        
        double n = createNoise().normalized2D(chunkX, chunkZ);  
        return n > noiseThreshold();
    }

    
    public double sampleNoiseAt(int chunkX, int chunkZ) {
        return createNoise().normalized2D(chunkX, chunkZ);
    }

    
    public double sampleNoise3D(int x, int y, int z) {
        return createNoise().normalized3D(x, y, z);
    }
}


