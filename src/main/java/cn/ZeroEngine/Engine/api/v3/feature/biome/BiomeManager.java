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


public class BiomeManager {

    
    private final Map<String, SBiome> registry = new HashMap<>();

    
    private final Map<UUID, Long> playerLastChunkKey = new HashMap<>();

    
    private final Map<UUID, String> playerLastBiome = new HashMap<>();

    
    private final org.bukkit.NamespacedKey processedKey;

    private final SF sf;

    public BiomeManager(SF sf) {
        this.sf = sf;
        this.processedKey = new org.bukkit.NamespacedKey(sf.plugin(), "biome_processed");
    }

    

    public void register(SBiome biome) {
        String id = biome.id();
        if (registry.containsKey(id)) {
            throw new IllegalStateException("Biome already registered: " + id);
        }
        registry.put(id, biome);
        sf.info("[Biome] Registered: " + id + " (" + biome.displayName() + ") target="
                + biome.targetBiome() + " replaces=" + biome.replaces());
    }

    
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

    

    
    public SBiome handleChunkPopulate(World world, int chunkX, int chunkZ) {
        if (registry.isEmpty()) return null;

        
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        Biome currentBiome;
        try {
            currentBiome = world.getBiome(blockX, 0, blockZ);
        } catch (Throwable t) {
            
            
            sf.error("[Biome] getBiome failed at (" + blockX + ", 0, " + blockZ + ")", t);
            return null;
        }

        
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

        
        
        boolean loaded = world.isChunkLoaded(chunkX, chunkZ);
        if (!loaded) {
            
            
            
        }

        Chunk chunk;
        try {
            chunk = world.getChunkAt(chunkX, chunkZ);
        } catch (Throwable t) {
            sf.error("[Biome] getChunkAt failed at (" + chunkX + ", " + chunkZ + ")", t);
            return null;
        }

        
        if (isChunkProcessed(chunk, picked.id())) {
            return null;  
        }

        
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
                        
                        
                        world.setBiome(wx, y, wz, target);
                    }
                }
            }
        } catch (Throwable t) {
            sf.error("[Biome] setBiome failed for " + picked.id()
                    + " at chunk (" + chunkX + ", " + chunkZ + ")", t);
            
        }

        
        markChunkProcessed(chunk, picked.id());

        
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

    
    public void handlePlayerLocation(org.bukkit.entity.Player player) {
        if (player == null || !player.isOnline()) return;
        Chunk chunk = player.getLocation().getChunk();
        if (chunk == null) return;

        
        String currentBiomeId = getChunkBiomeId(chunk);
        String lastBiomeId = playerLastBiome.get(player.getUniqueId());

        
        if (!java.util.Objects.equals(currentBiomeId, lastBiomeId)) {
            
            if (lastBiomeId != null) {
                SBiome last = registry.get(lastBiomeId);
                if (last != null) {
                    try { last.onPlayerLeave(player, chunk); }
                    catch (Throwable t) { sf.error("[Biome] onPlayerLeave error", t); }
                }
            }
            
            if (currentBiomeId != null) {
                SBiome cur = registry.get(currentBiomeId);
                if (cur != null) {
                    try { cur.onPlayerEnter(player, chunk); }
                    catch (Throwable t) { sf.error("[Biome] onPlayerEnter error", t); }
                }
            }
            playerLastBiome.put(player.getUniqueId(), currentBiomeId);
        }

        
        if (currentBiomeId != null) {
            SBiome cur = registry.get(currentBiomeId);
            if (cur != null) {
                try { cur.onPerSecond(player, player.getLocation()); }
                catch (Throwable t) { sf.error("[Biome] onPerSecond error", t); }
            }
        }
    }

    

    
    public boolean isChunkProcessed(Chunk chunk, String biomeId) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String v = pdc.get(processedKey, PersistentDataType.STRING);
        return biomeId.equals(v);
    }

    
    public void markChunkProcessed(Chunk chunk, String biomeId) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        pdc.set(processedKey, PersistentDataType.STRING, biomeId);
    }

    
    public String getChunkBiomeId(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        return pdc.get(processedKey, PersistentDataType.STRING);
    }
}

