package cn.ZeroEngine.Engine.api.v3.feature.biome;

import cn.ZeroEngine.Engine.api.v3.SF;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class BiomeListener implements Listener {

    private final JavaPlugin plugin;
    private final SF sf;
    private final BiomeManager manager;
    private org.bukkit.scheduler.BukkitTask perSecondTask;

    public BiomeListener(JavaPlugin plugin, SF sf, BiomeManager manager) {
        this.plugin = plugin;
        this.sf = sf;
        this.manager = manager;
    }

    

    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChunkPopulate(ChunkPopulateEvent event) {
        World world = event.getWorld();
        int cx = event.getChunk().getX();
        int cz = event.getChunk().getZ();
        try {
            manager.handleChunkPopulate(world, cx, cz);
        } catch (Throwable t) {
            sf.error("[BiomeListener] onChunkPopulate error at chunk (" + cx + ", " + cz
                    + ") world=" + world.getName(), t);
        }
    }

    

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        try {
            manager.handlePlayerLocation(event.getPlayer());
        } catch (Throwable t) {
            sf.error("[BiomeListener] onPlayerMove error for " + event.getPlayer().getName(), t);
        }
    }

    

    
    public void startTick(JavaPlugin plugin, SF sf) {
        if (perSecondTask != null) return;  
        
        perSecondTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                try {
                    manager.handlePlayerLocation(p);
                } catch (Throwable t) {
                    sf.error("[BiomeListener] perSecond tick error for " + p.getName(), t);
                }
            }
        }, 20L, 20L);
    }

    public void shutdown() {
        if (perSecondTask != null) {
            perSecondTask.cancel();
            perSecondTask = null;
        }
        HandlerList.unregisterAll(this);
    }
}

