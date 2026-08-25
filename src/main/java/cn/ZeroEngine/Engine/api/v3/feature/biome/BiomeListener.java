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

/**
 * 自定义生物群系事件监听器
 *
 * <p>监听的事件：</p>
 * <ul>
 *   <li>{@link ChunkPopulateEvent} —— chunk 第一次生成完成时触发，调
 *       {@link BiomeManager#handleChunkPopulate(World, int, int)} 应用 SBiome 替换</li>
 *   <li>{@link PlayerMoveEvent} —— 玩家在 chunk 间移动时，触发 onPlayerEnter/Leave</li>
 *   <li>SFTick 调度 —— 每秒触发一次 onPerSecond 钩子</li>
 * </ul>
 *
 * <p>关键时机说明：ChunkPopulateEvent <b>只在 chunk 第一次生成时触发</b>，
 * 已经生成过的 chunk 不会再触发。这正好对应「存档已存在但未探索的区块」：
 * 玩家靠近、服务端首次生成该 chunk 时就会调用本监听器，可以在此插入
 * 新生物群系。</p>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
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

    // ==================== chunk 生成 ====================

    /**
     * Chunk 生成完成事件 —— 在此插入自定义 biome
     * 监听 LOW 优先级，让其他插件的高优先级监听有机会在 biome 替换后再做调整
     */
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

    // ==================== 玩家 chunk 切换 ====================

    /**
     * 玩家移动事件 —— 仅在跨 chunk 时触发 onPlayerEnter/Leave
     * 监控频率高，做最小化处理：只比较 chunk 坐标，相同就 return
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 只在玩家位置实际变化（不只是 head rotation）时处理
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

    // ==================== SFTick 调度 ====================

    /**
     * 启动每秒一次的 onPerSecond 钩子调度
     * 由 SF.biome() 在懒加载时调用
     */
    public void startTick(JavaPlugin plugin, SF sf) {
        if (perSecondTask != null) return;  // 防止重复启动
        // 每 20 个 Bukkit tick = 1 秒
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
