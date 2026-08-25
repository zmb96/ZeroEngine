package cn.ZeroEngine.Engine.api.v3.feature.biome;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MysticForestBiome extends SBiome {

    

    @Override
    public String id() {
        return "mystic_forest";  
    }

    @Override
    public String displayName() {
        return "§5§l神秘森林";
    }

    @Override
    public Biome targetBiome() {
        
        
        
        
        return Biome.DARK_FOREST;
    }

    

    @Override
    public List<String> worlds() {
        return Arrays.asList("world");  
    }

    @Override
    public List<Biome> replaces() {
        
        return Arrays.asList(Biome.PLAINS, Biome.FOREST);
    }

    @Override
    public double weight() {
        return 1.0;  
    }

    @Override
    public double noiseThreshold() {
        return 0.4;  
    }

    @Override
    public double noiseScale() {
        return 0.04;  
    }

    @Override
    public int[] yRange() {
        
        return new int[]{60, 200};
    }

    

    
    @Override
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        
        int flowerCount = 4 + random.nextInt(5);
        for (int i = 0; i < flowerCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z);
            if (y < world.getMinHeight()) continue;
            
            Material ground = world.getBlockAt(x, y, z).getType();
            if (ground == Material.GRASS_BLOCK || ground == Material.DIRT
                    || ground == Material.PODZOL || ground == Material.COARSE_DIRT) {
                
                world.getBlockAt(x, y + 1, z).setType(Material.GLOW_BERRIES);
            }
        }

        
        if (random.nextDouble() < 0.15) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z);
            Material ground = world.getBlockAt(x, y, z).getType();
            if (ground == Material.GRASS_BLOCK || ground == Material.DIRT) {
                world.getBlockAt(x, y + 1, z).setType(Material.WITHER_ROSE);
            }
        }

        
        if (random.nextDouble() < 0.01) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z) + 1;
            world.getBlockAt(x, y, z).setType(Material.CHEST);
            var state = world.getBlockAt(x, y, z).getState();
            if (state instanceof org.bukkit.block.Chest chest) {
                var inv = chest.getBlockInventory();
                inv.setItem(13, new ItemStack(Material.DIAMOND, 1 + random.nextInt(2)));
                inv.setItem(15, new ItemStack(Material.EXPERIENCE_BOTTLE, 3 + random.nextInt(5)));
                chest.update();
            }
        }
    }

    
    @Override
    public void onPlayerEnter(Player player, Chunk chunk) {
        player.sendMessage("§5§l你进入了神秘森林...");
        player.sendMessage("§7『传说这里藏有发光浆果和凋零玫瑰。』");

        
        var glow = org.bukkit.Registry.EFFECT.get(
                org.bukkit.NamespacedKey.minecraft("glowing"));
        if (glow != null) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    glow, 600, 0, false, false, true));
        }

        
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 0.5f);
    }

    
    @Override
    public void onPlayerLeave(Player player, Chunk chunk) {
        player.sendMessage("§7你离开了神秘森林...");
        
        player.removePotionEffect(org.bukkit.Registry.EFFECT.get(
                org.bukkit.NamespacedKey.minecraft("glowing")));
    }

    
    @Override
    public void onPerSecond(Player player, Location loc) {
        if (player.getFoodLevel() < 20) {
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
        }
        
        if (Math.random() < 0.2) {
            loc.getWorld().spawnParticle(org.bukkit.Particle.DUST,
                    loc.clone().add(0, 1, 0), 1, 0.5, 1, 0.5, 0,
                    new org.bukkit.Particle.DustOptions(
                            org.bukkit.Color.fromRGB(160, 80, 255), 1.5f));
        }
    }

    

    
}

