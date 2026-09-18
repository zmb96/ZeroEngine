package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.HashMap;
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
        boolean resultWasBukkit = result != null && !result.getType().isAir();
        if (!resultWasBukkit) {
            result = target.clone();
        } else {
            for (Map.Entry<SEnchantment, Integer> te : manager.getOn(target).entrySet()) {
                if (!te.getKey().isOn(result)) {
                    te.getKey().setLevel(result, te.getValue());
                }
            }
        }

        boolean sfChanged = false;
        int extraCost = 0;

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
                if (!conflict) conflict = conflictsVanilla(enchant, result);
                if (conflict) continue;

                int current = enchant.getLevel(result);
                final int newLevel;

                if (current == 0) {
                    newLevel = Math.min(bookLevel, enchant.maxLevel());
                } else if (current == bookLevel) {
                    if (bookLevel >= enchant.maxLevel()) continue;
                    newLevel = bookLevel + 1;
                } else if (bookLevel > current) {
                    newLevel = Math.min(bookLevel, enchant.maxLevel());
                } else {
                    continue;
                }

                enchant.setLevel(result, newLevel);
                extraCost += enchant.anvilCost() * newLevel;
                sfChanged = true;
            }
        }

        if (!resultWasBukkit) {
            for (Map.Entry<SEnchantment, Integer> te : manager.getOn(target).entrySet()) {
                int resultLvl = te.getKey().getLevel(result);
                if (resultLvl != te.getValue()) {
                    sfChanged = true;
                    break;
                }
            }
        }

        if (sfChanged || extraCost > 0) {
            try {
                ItemMeta meta = result.getItemMeta();
                if (meta instanceof Repairable rep) {
                    rep.setRepairCost(rep.getRepairCost() + extraCost);
                    result.setItemMeta(meta);
                } else if (extraCost > 0) {
                    inv.setRepairCost(Math.max(inv.getRepairCost(), extraCost));
                }
            } catch (Throwable ignoreDeprecated) {
                if (extraCost > 0) {
                    try { inv.setRepairCost(Math.max(inv.getRepairCost(), extraCost)); } catch (Throwable ignore) {}
                }
            }
            e.setResult(result);
        }
    }

    static boolean conflictsVanilla(SEnchantment ench, ItemStack item) {
        if (ench.conflicts().isEmpty()) return false;
        Map<Enchantment, Integer> vanilla = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            vanilla.putAll(meta.getEnchants());
            if (meta instanceof EnchantmentStorageMeta sm) {
                vanilla.putAll(sm.getStoredEnchants());
            }
        }
        for (String confId : ench.conflicts()) {
            if (confId == null) continue;
            Enchantment ve = resolveVanillaEnchant(confId);
            if (ve != null && vanilla.containsKey(ve)) return true;
        }
        return false;
    }

    private static Enchantment resolveVanillaEnchant(String idOrKey) {
        if (idOrKey == null) return null;
        String up = idOrKey.toUpperCase();
        String lower = up.toLowerCase();
        for (Enchantment e : Registry.ENCHANTMENT) {
            if (e.getKey().getKey().equalsIgnoreCase(lower)) return e;
            try {
                NamespacedKey nk = NamespacedKey.minecraft(lower);
                if (e.getKey().equals(nk)) return e;
            } catch (Throwable ignore) {}
        }
        return null;
    }
}
