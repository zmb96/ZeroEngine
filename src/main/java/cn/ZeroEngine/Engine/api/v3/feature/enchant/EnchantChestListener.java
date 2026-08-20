package cn.ZeroEngine.Engine.api.v3.feature.enchant;

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

public class EnchantChestListener implements Listener {

    private final EnchantManager enchantManager;
    private final Set<String> blacklistWorlds = new HashSet<>();
    private final Map<String, Double> lootChances = new HashMap<>();
    private double defaultChance = 0.05;
    private int maxLootPerChest = 2;
    private final Set<String> lootedChests = Collections.synchronizedSet(new HashSet<>());

    public EnchantChestListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
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

        List<SEnchantment> available = new ArrayList<>(enchantManager.all());
        if (available.isEmpty()) return;

        Collections.shuffle(available);

        Chest chest = (Chest) state;
        Inventory inv = chest.getInventory();

        int lootCount = 0;
        for (SEnchantment enchant : available) {
            if (lootCount >= maxLootPerChest) break;

            double chance = lootChances.getOrDefault(enchant.id(), defaultChance);
            if (Math.random() > chance) continue;

            int level = Math.max(1, (int) (Math.random() * enchant.maxLevel()) + 1);
            ItemStack book = enchantManager.createBook(enchant, level);

            Map<Integer, ItemStack> leftover = inv.addItem(book);

            lootCount++;
        }

        if (lootCount > 0) {
            lootedChests.add(chestKey);
        }
    }

    public void setDefaultChance(double chance) {
        this.defaultChance = chance;
    }

    public void setLootChance(String enchantId, double chance) {
        lootChances.put(enchantId, chance);
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
}
