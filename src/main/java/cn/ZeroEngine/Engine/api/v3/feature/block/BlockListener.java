package cn.ZeroEngine.Engine.api.v3.feature.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义方块事件分发器。
 *
 * 放置：玩家手里是 SBlock 物品形式 -> BlockPlaceEvent -> manager.placeAt() + onPlace()
 * 破坏：BlockBreakEvent -> findAt() -> onBreak() + 按 dropMode() 处理掉落 + removeAt()
 * 红石：每秒扫描所有已放置且 redstoneRadius()>0 的方块，状态变化触发 onRedstonePowered/Unpowered
 */
public class BlockListener implements Listener {

    private final Plugin plugin;
    private final BlockManager manager;
    private final SF sf;

    private final Set<Location> redstoneWatch = ConcurrentHashMap.newKeySet();
    private final Map<Location, Boolean> redstoneLast = new ConcurrentHashMap<>();
    private final Map<Location, SBlock> redstoneDefs = new ConcurrentHashMap<>();

    private int taskId = -1;

    public BlockListener(Plugin plugin, BlockManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.sf = SF.sf();
        startRedstoneTick();
    }

    public void startRedstoneTick() {
        if (taskId != -1) return;
        Runnable task = this::scanRedstone;
        try {
            org.bukkit.scheduler.BukkitTask t = sf.bukkit().getScheduler().runTaskTimer(plugin, task, 20L, 20L);
            taskId = t.getTaskId();
        } catch (Throwable ignore) { taskId = -1; }
    }

    private void scanRedstone() {
        if (redstoneWatch.isEmpty()) return;
        for (Location loc : redstoneWatch) {
            SBlock def = redstoneDefs.get(loc);
            if (def == null) continue;
            Block block = loc.getBlock();
            if (manager.findAt(block) != def) {
                redstoneWatch.remove(loc);
                redstoneLast.remove(loc);
                redstoneDefs.remove(loc);
                continue;
            }
            boolean powered = isAnyPowered(block, def.redstoneRadius());
            Boolean last = redstoneLast.get(loc);
            if (last == null) {
                redstoneLast.put(loc, powered);
                if (powered) def.onRedstonePowered(block, currentPower(block, def.redstoneRadius()));
            } else if (last != powered) {
                redstoneLast.put(loc, powered);
                if (powered) {
                    def.onRedstonePowered(block, currentPower(block, def.redstoneRadius()));
                } else {
                    def.onRedstoneUnpowered(block);
                }
            }
        }
    }

    private boolean isAnyPowered(Block center, int radius) {
        if (radius <= 0) return center.isBlockPowered();
        World w = center.getWorld();
        if (w == null) return false;
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = w.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (b.isBlockPowered() || b.isBlockIndirectlyPowered()) return true;
                }
            }
        }
        return false;
    }

    private int currentPower(Block center, int radius) {
        int max = 0;
        World w = center.getWorld();
        if (w == null) return 0;
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        int r = Math.max(0, radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block b = w.getBlockAt(cx + dx, cy + dy, cz + dz);
                    max = Math.max(max, b.getBlockPower());
                }
            }
        }
        return max;
    }

    private void watchForRedstone(Block block, SBlock def) {
        if (def.redstoneRadius() <= 0) return;
        Location loc = block.getLocation();
        redstoneWatch.add(loc);
        redstoneDefs.put(loc, def);
    }

    private void unwatchRedstone(Block block) {
        Location loc = block.getLocation();
        redstoneWatch.remove(loc);
        redstoneLast.remove(loc);
        redstoneDefs.remove(loc);
    }

    // ==================== 放置 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Block placed = e.getBlockPlaced();
        ItemStack hand = e.getItemInHand();
        if (hand == null || hand.getType().isAir()) return;
        SBlock def = manager.findItem(hand);
        if (def == null) return;

        Block against = e.getBlockAgainst();
        BlockFace face = against.getFace(placed);
        if (face == null) face = BlockFace.UP;
        if (!def.canPlaceAt(placed, face)) {
            e.setCancelled(true);
            sf.msg(e.getPlayer(), "§c此处无法放置 " + def.displayName());
            return;
        }

        def.onPlace(e);
        if (e.isCancelled()) return;

        manager.placeAt(placed, def);
        watchForRedstone(placed, def);
    }

    // ==================== 破坏 + 掉落 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block broken = e.getBlock();
        SBlock def = manager.findAt(broken);
        if (def == null) return;

        def.onBreak(e);

        SBlock.DropMode mode = def.dropMode();
        if (mode == SBlock.DropMode.NONE) {
            e.setDropItems(false);
        } else if (mode == SBlock.DropMode.CUSTOM) {
            e.setDropItems(false);
            List<ItemStack> drops = def.drops();
            if (drops != null && !drops.isEmpty()) {
                Location dropLoc = broken.getLocation().add(0.5, 0.5, 0.5);
                World w = broken.getWorld();
                for (ItemStack d : drops) {
                    if (d != null && !d.getType().isAir()) w.dropItemNaturally(dropLoc, d);
                }
            }
        }

        if (def.expDrop() > 0) {
            e.setExpToDrop(def.expDrop());
        }

        unwatchRedstone(broken);
        manager.removeAt(broken);
    }

    // ==================== 右键 / 左键方块 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        SBlock def = manager.findAt(clicked);
        if (def == null) return;

        boolean handled;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handled = def.onBlockRightClick(e);
        } else {
            handled = def.onBlockLeftClick(e);
        }
        if (handled) e.setCancelled(true);
    }

    // ==================== 通用方块事件分发 ====================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(BlockDamageEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onBlockDamage(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onBurn(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onIgnite(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onPhysics(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFade(BlockFadeEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onFade(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForm(BlockFormEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onForm(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onSpread(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFromTo(BlockFromToEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onFromTo(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onGrow(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onPistonExtend(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onPistonRetract(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onDispense(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(BlockExplodeEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onExplode(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onLeavesDecay(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onMoistureChange(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFluidLevelChange(FluidLevelChangeEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onFluidLevelChange(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onEntityChangeBlock(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onSignChange(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNotePlay(NotePlayEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onNotePlay(e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpDrop(BlockExpEvent e) {
        SBlock def = manager.findAt(e.getBlock());
        if (def != null) def.onExpDrop(e);
    }

    public void shutdown() {
        if (taskId != -1) {
            try { sf.bukkit().getScheduler().cancelTask(taskId); } catch (Throwable ignore) {}
            taskId = -1;
        }
        redstoneWatch.clear();
        redstoneLast.clear();
        redstoneDefs.clear();
    }
}
