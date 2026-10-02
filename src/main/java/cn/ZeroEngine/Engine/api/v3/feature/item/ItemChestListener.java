package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;

public class ItemChestListener implements Listener {

    private final ItemManager itemManager;
    private final Set<String> blacklistWorlds = new HashSet<>();
    private final Map<String, Double> lootChances = new HashMap<>();
    private double defaultChance = 0.03;
    private int maxLootPerChest = 1;
    private final Set<String> lootedChests = Collections.synchronizedSet(new HashSet<>());
    
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

        if (isPlayerPlaced((Chest) state)) return;

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

    
    public static void setChanceScale(double scale) {
        chanceScale = Math.max(0.01, Math.min(1.0, scale));
    }

    public static double getChanceScale() {
        return chanceScale;
    }

    private static final NamespacedKey PLAYER_PLACED_KEY =
            new NamespacedKey("zeroengine", "player_placed");

    private static boolean isPlayerPlaced(Chest chest) {
        try {
            return chest.getPersistentDataContainer()
                    .has(PLAYER_PLACED_KEY, PersistentDataType.BYTE);
        } catch (Throwable ignore) {
            return false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST) return;
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;
        try {
            chest.getPersistentDataContainer()
                    .set(PLAYER_PLACED_KEY, PersistentDataType.BYTE, (byte) 1);
            chest.update(false, false);
        } catch (Throwable ignore) {
        }
    }
}
