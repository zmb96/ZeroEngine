package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;

public class ItemChestListener implements Listener {

    private final ItemManager itemManager;
    private final Set<String> blacklistWorlds = new HashSet<>();
    private final Map<String, Double> lootChances = new HashMap<>();
    private double defaultChance = 0.03;
    private int maxLootPerChest = 1;
    private final Set<String> lootedChests = Collections.synchronizedSet(new HashSet<>());
    /** 全局掉率缩放因子（1.0=不变，越小越稀有），由外部插件按装备等级动态调整 */
    private static double chanceScale = 1.0;

    public ItemChestListener(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChestOpen(PlayerInteractEvent e) {
        if (!e.getAction().equals(org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Chest)) return;
        if (e.isCancelled()) return;

        if (blacklistWorlds.contains(block.getWorld().getName())) return;

        String chestKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        if (lootedChests.contains(chestKey)) return;

        List<SItem> available = new ArrayList<>(itemManager.all());
        if (available.isEmpty()) return;

        Collections.shuffle(available);

        Chest chest = (Chest) state;
        Inventory inv = chest.getInventory();

        int lootCount = 0;
        for (SItem item : available) {
            if (lootCount >= maxLootPerChest) break;

            double chance = lootChances.getOrDefault(item.id(), defaultChance);
            if (Math.random() > chance * chanceScale) continue;

            ItemStack stack = item.create(1);
            inv.addItem(stack);

            lootCount++;
        }

        if (lootCount > 0) {
            lootedChests.add(chestKey);
        }
    }

    public void setDefaultChance(double chance) {
        this.defaultChance = chance;
    }

    public void setItemChance(String itemId, double chance) {
        lootChances.put(itemId, chance);
    }

    public void setMaxLootPerChest(int max) {
        this.maxLootPerChest = max;
    }

    public void addBlacklistWorld(String worldName) {
        blacklistWorlds.add(worldName);
    }

    public void removeBlacklistWorld(String worldName) {
        blacklistWorlds.remove(worldName);
    }

    public void resetLootCache() {
        lootedChests.clear();
    }

    /** 设置全局掉率缩放（1.0=不变，0.1=十分之一），供外部插件按装备等级动态调整 */
    public static void setChanceScale(double scale) {
        chanceScale = Math.max(0.01, Math.min(1.0, scale));
    }

    public static double getChanceScale() {
        return chanceScale;
    }
}
