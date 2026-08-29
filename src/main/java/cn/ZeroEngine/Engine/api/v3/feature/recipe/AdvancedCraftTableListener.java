package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import cn.ZeroEngine.Engine.api.v3.feature.gui.SChestGUI;

public class AdvancedCraftTableListener implements Listener {

    private final RecipeManager manager;

    public AdvancedCraftTableListener(RecipeManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClickTable(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = e.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.CRAFTING_TABLE) return;

        Block below = clicked.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.DISPENSER) return;
        if (!(below.getState() instanceof Dispenser dispenser)) return;

        Player p = e.getPlayer();
        e.setCancelled(true);

        Inventory inv = dispenser.getInventory();
        AdvancedCraftTable table = manager.findTableAt(clicked);

        if (table != null) {
            SChestGUI gui = table.onRightChest();
            table.onOpenChest(p, clicked, below, inv);
            if (gui != null) {
                gui.open(p);
                return;
            }
            p.openInventory(inv);
            return;
        }

        SRecipe recipe = manager.craftAtInventory(inv);
        if (recipe != null) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.4f);
            p.sendMessage("§a合成成功 §7→ §f" + recipe.id());
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.6f);
        } else {
            p.openInventory(inv);
        }
    }
}
