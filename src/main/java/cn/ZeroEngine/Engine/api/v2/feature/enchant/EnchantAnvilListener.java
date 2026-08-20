package cn.ZeroEngine.Engine.api.v2.feature.enchant;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantAnvilListener implements Listener {

    private final EnchantManager manager;

    public EnchantAnvilListener(EnchantManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepare(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack target = inv.getItem(0);
        ItemStack sacrifice = inv.getItem(1);

        if (target == null || target.getType().isAir()) return;

        ItemStack result = e.getResult();
        if (result == null || result.getType().isAir()) result = target.clone();

        int totalCost = 0;

        if (sacrifice != null && !sacrifice.getType().isAir()) {
            Map<SEnchantment, Integer> sourceEnchants = manager.getOn(sacrifice);

            for (Map.Entry<SEnchantment, Integer> entry : sourceEnchants.entrySet()) {
                SEnchantment enchant = entry.getKey();
                int bookLevel = entry.getValue();

                if (!enchant.canEnchantItem(result)) continue;

                boolean conflict = false;
                for (Map.Entry<SEnchantment, Integer> existing : manager.getOn(result).entrySet()) {
                    if (!existing.getKey().id().equals(enchant.id()) && enchant.conflictsWith(existing.getKey())) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict) continue;

                int current = enchant.getLevel(result);
                int newLevel;
                if (current == bookLevel) {
                    newLevel = Math.min(current + 1, enchant.maxLevel());
                } else {
                    newLevel = Math.max(current, bookLevel);
                }

                enchant.setLevel(result, newLevel);
                totalCost += enchant.anvilCost() * newLevel;
            }
        }

        if (totalCost > 0) {
            inv.setRepairCost(Math.max(inv.getRepairCost(), totalCost));
            e.setResult(result);
        }
    }
}
