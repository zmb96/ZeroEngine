package cn.ZeroEngine.Engine.api.v3.feature.biome;

import cn.ZeroEngine.Engine.api.v3.main.PerlinNoise;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class EmberWastesBiome extends SBiome {

    

    @Override
    public String id() {
        return "ember_wastes";  
    }

    @Override
    public String displayName() {
        return "§4§l炽焰荒原";
    }

    @Override
    public Biome targetBiome() {
        
        
        return Biome.NETHER_WASTES;
    }

    

    @Override
    public List<String> worlds() {
        return Arrays.asList("world");  
    }

    @Override
    public List<Biome> replaces() {
        
        return Arrays.asList(Biome.DESERT, Biome.PLAINS);
    }

    @Override
    public double weight() {
        return 1.2;  
    }

    @Override
    public double noiseThreshold() {
        return 0.65;  
    }

    @Override
    public double noiseScale() {
        return 0.03;  
    }

    @Override
    public int noiseOctaves() {
        return 5;  
    }

    @Override
    public double noisePersistence() {
        return 0.55;  
    }

    @Override
    public int[] yRange() {
        
        return new int[]{55, 100};
    }

    

    @Override
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = chunkX * 16 + x;
                int wz = chunkZ * 16 + z;
                int y = world.getHighestBlockYAt(wx, wz);
                if (y < 55) continue;  

                
                Block top = world.getBlockAt(wx, y, wz);
                if (top.getType() == Material.AIR) {
                    
                    while (y > 0 && world.getBlockAt(wx, y, wz).getType() == Material.AIR) y--;
                    if (y <= 0) continue;
                    top = world.getBlockAt(wx, y, wz);
                }
                
                if (top.getType() == Material.STONE
                        || top.getType() == Material.DEEPSLATE
                        || top.getType() == Material.BEDROCK) continue;

                
                double n3 = sampleNoise3D(wx, y, wz);  
                if (n3 > 0.7) {
                    
                    world.getBlockAt(wx, y + 1, wz).setType(Material.LAVA);
                    top.setType(Material.LAVA);
                } else if (n3 > 0.5) {
                    top.setType(Material.BLACKSTONE);  
                } else {
                    top.setType(Material.NETHERRACK);  
                }

                
                
                if (n3 > 0.7 && n3 < 0.72 && random.nextDouble() < 0.3) {
                    Block above = world.getBlockAt(wx, y + 2, wz);
                    if (above.getType() == Material.AIR) {
                        above.setType(Material.FIRE);
                    }
                }
            }
        }

        
        int spikeCount = 1 + random.nextInt(3);
        for (int i = 0; i < spikeCount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int baseY = world.getHighestBlockYAt(wx, wz);
            if (baseY < 55) continue;

            int height = 4 + random.nextInt(5);  
            
            for (int h = 0; h < height; h++) {
                world.getBlockAt(wx, baseY + 1 + h, wz).setType(Material.BASALT);
            }
            
            world.getBlockAt(wx, baseY + 1 + height, wz).setType(Material.MAGMA_BLOCK);
        }

        
        if (random.nextDouble() < 0.05) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int y = world.getHighestBlockYAt(wx, wz) + 1;
            Block b = world.getBlockAt(wx, y, wz);
            if (b.getType() == Material.AIR) {
                b.setType(Material.CAMPFIRE);
                
                
            }
        }

        
        if (random.nextDouble() < 0.005) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int y = world.getHighestBlockYAt(wx, wz) + 1;
            world.getBlockAt(wx, y, wz).setType(Material.CHEST);
            if (world.getBlockAt(wx, y, wz).getState() instanceof org.bukkit.block.Chest chest) {
                var inv = chest.getBlockInventory();
                inv.setItem(13, new org.bukkit.inventory.ItemStack(Material.BLAZE_POWDER, 5 + random.nextInt(10)));
                inv.setItem(11, new org.bukkit.inventory.ItemStack(Material.MAGMA_CREAM, 3 + random.nextInt(5)));
                inv.setItem(15, new org.bukkit.inventory.ItemStack(Material.FIRE_CHARGE, 2 + random.nextInt(8)));
                chest.update();
            }
        }
    }

    

    @Override
    public void onPlayerEnter(Player player, Chunk chunk) {
        player.sendMessage("§4§l┌─────────────────────────────────┐");
        player.sendMessage("§4§l│ §c你踏入了炽焰荒原... §4§l          │");
        player.sendMessage("§4§l│ §7『大地焦黑，岩浆在地表流淌。』 §4§l│");
        player.sendMessage("§4§l└─────────────────────────────────┘");

        
        PotionEffectType fireRes = Registry.EFFECT.get(NamespacedKey.minecraft("fire_resistance"));
        if (fireRes != null) {
            player.addPotionEffect(new PotionEffect(fireRes, 1200, 0, false, false, true));
        }
        PotionEffectType speed = Registry.EFFECT.get(NamespacedKey.minecraft("speed"));
        if (speed != null) {
            player.addPotionEffect(new PotionEffect(speed, 1200, 1, false, false, true));
        }

        
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
    }

    @Override
    public void onPlayerLeave(Player player, Chunk chunk) {
        player.sendMessage("§7你走出了炽焰荒原...");
        
        PotionEffectType fireRes = Registry.EFFECT.get(NamespacedKey.minecraft("fire_resistance"));
        PotionEffectType speed = Registry.EFFECT.get(NamespacedKey.minecraft("speed"));
        if (fireRes != null) player.removePotionEffect(fireRes);
        if (speed != null) player.removePotionEffect(speed);
    }

    @Override
    public void onPerSecond(Player player, Location loc) {
        
        loc.getWorld().spawnParticle(Particle.DRIPPING_LAVA,
                loc.clone().add(0, 0.1, 0), 3, 0.5, 0.1, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                loc.clone().add(0, 1.5, 0), 2, 0.3, 0.5, 0.3, 0.01);

        
        if (Math.random() < 0.1) {
            int dx = (int) (Math.random() * 6 - 3);
            int dz = (int) (Math.random() * 6 - 3);
            Block target = loc.getWorld().getBlockAt(
                    loc.getBlockX() + dx,
                    loc.getBlockY() - 1,
                    loc.getBlockZ() + dz);
            Block above = loc.getWorld().getBlockAt(
                    target.getX(), target.getY() + 1, target.getZ());
            
            if ((target.getType() == Material.NETHERRACK
                    || target.getType() == Material.BLACKSTONE
                    || target.getType() == Material.BASALT)
                    && above.getType() == Material.AIR) {
                above.setType(Material.FIRE);
            }
        }
    }
}

