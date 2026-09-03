package cn.ZeroEngine.Engine.api.v3.feature.crop;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.Random;

public class CropListener implements Listener {

    private final Plugin plugin;
    private final CropManager manager;
    private final ItemManager itemManager;
    private final Random random = new Random();

    public CropListener(Plugin plugin, CropManager manager, ItemManager itemManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.itemManager = itemManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        Player p = e.getPlayer();
        ItemStack hand = e.getItem();

        if (isFarmland(clicked) && hand != null && !hand.getType().isAir()) {
            SItem seed = itemManager.find(hand);
            if (seed instanceof SCrop crop) {
                e.setCancelled(true);
                Block above = clicked.getRelative(BlockFace.UP);
                if (!above.getType().isAir()) return;
                if (!crop.canGrowAt(above)) {
                    p.sendMessage("§c此条件无法种植 " + crop.displayName());
                    return;
                }
                if (manager.placeAt(above, crop, 0)) {
                    crop.onPlant(above, p);
                    consumeOne(p, hand);
                    p.playSound(above.getLocation(), Sound.BLOCK_CROP_BREAK, 1f, 1f);
                }
                return;
            }
        }

        if (clicked.getBlockData() instanceof Ageable || isStageCrop(clicked)) {
            SCrop crop = manager.findAt(clicked);
            if (crop == null) crop = findMatureFallback(clicked);
            if (crop == null) return;
            if (crop.isMature(clicked)) {
                e.setCancelled(true);
                harvest(clicked, crop, p);
            }
        }
    }

    private boolean isStageCrop(Block b) {
        SCrop c = manager.findAt(b);
        return c != null && c.isStageMode();
    }

    private SCrop findMatureFallback(Block b) {
        if (b == null) return null;
        SCrop fb = manager.findFallback(b);
        if (fb == null) return null;
        if (fb.isStageMode()) {
            java.util.List<Material> stages = fb.stages();
            return b.getType() == stages.get(stages.size() - 1) ? fb : null;
        }
        if (!(b.getBlockData() instanceof Ageable ageable)) return null;
        int max = Math.min(ageable.getMaximumAge(), fb.maxStage());
        if (ageable.getAge() < max) return null;
        return fb;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent e) {
        Block b = e.getBlock();
        SCrop crop = manager.findAt(b);
        if (crop == null) return;
        if (b.getBlockData() instanceof Ageable ageable) {
            int max = Math.min(ageable.getMaximumAge(), crop.maxStage());
            if (ageable.getAge() >= max) {
                e.setCancelled(true);
                return;
            }
        }
        if (!crop.canGrowAt(b)) {
            e.setCancelled(true);
            return;
        }
        if (random.nextDouble() >= crop.growthChance()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent e) {
        Block b = e.getBlock();
        SCrop crop = manager.findAt(b);
        if (crop == null) return;
        if (!crop.onBonemeal(b)) {
            e.setCancelled(true);
            return;
        }
        if (crop.isStageMode()) {
            if (crop.isMature(b)) e.setCancelled(true);
            else { e.setCancelled(true); crop.growOneStep(b); }
            return;
        }
        if (b.getBlockData() instanceof Ageable ageable) {
            int max = Math.min(ageable.getMaximumAge(), crop.maxStage());
            if (ageable.getAge() >= max) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk chunk = e.getChunk();
        manager.scanChunk(chunk);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        SCrop crop = manager.findAt(b);
        if (crop == null) crop = findMatureFallback(b);
        if (crop == null) return;
        Player p = e.getPlayer();
        e.setDropItems(false);
        manager.removeAt(b);
        for (ItemStack drop : crop.harvestDrops()) {
            if (drop != null) b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), drop);
        }
        int seeds = crop.minSeedsOnHarvest() + random.nextInt(Math.max(1, crop.maxSeedsOnHarvest() - crop.minSeedsOnHarvest() + 1));
        if (seeds > 0) b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), crop.create(seeds));
        crop.onHarvest(b, p);
    }

    private void harvest(Block block, SCrop crop, Player p) {
        block.setType(Material.AIR);
        manager.removeAt(block);
        for (ItemStack drop : crop.harvestDrops()) {
            if (drop != null) block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
        }
        int seeds = crop.minSeedsOnHarvest() + random.nextInt(Math.max(1, crop.maxSeedsOnHarvest() - crop.minSeedsOnHarvest() + 1));
        if (seeds > 0) block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), crop.create(seeds));
        crop.onHarvest(block, p);
        p.playSound(block.getLocation(), Sound.BLOCK_CROP_BREAK, 1f, 1f);
    }

    private boolean isFarmland(Block b) {
        return b.getType() == Material.FARMLAND;
    }

    private void consumeOne(Player p, ItemStack hand) {
        if (hand.getAmount() <= 1) {
            p.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
    }

    public void shutdown() {}
}
