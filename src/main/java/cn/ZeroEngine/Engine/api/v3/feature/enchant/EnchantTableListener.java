package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;

public class EnchantTableListener implements Listener {

    private final EnchantManager enchantManager;
    private final Map<String, Double> enchantChances = new HashMap<>();
    private double baseChance = 0.15;
    private double perLevelBonus = 0.05;
    private final Set<String> blacklistWorlds = new HashSet<>();

    public EnchantTableListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchant(InventoryClickEvent e) {
        SF sf = SF.sf();
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals("§r")) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        String world = p.getWorld().getName();
        if (blacklistWorlds.contains(world)) return;

        int bonus = 0;
        ItemStack[] contents = e.getInventory().getContents();
        if (contents.length >= 2) {
            ItemStack lapis = contents[1];
            if (lapis != null && !lapis.getType().isAir()) {
                bonus = Math.min(lapis.getAmount(), 30);
            }
        }

        List<SEnchantment> applicable = new ArrayList<>();
        for (SEnchantment en : enchantManager.all()) {
            if (en.canEnchantItem(item) && !en.isTreasure()) {
                boolean conflict = false;
                for (Map.Entry<SEnchantment, Integer> existing : enchantManager.getOn(item).entrySet()) {
                    if (!existing.getKey().id().equals(en.id()) && en.conflictsWith(existing.getKey())) {
                        conflict = true;
                        break;
                    }
                }
                if (!conflict) conflict = EnchantAnvilListener.conflictsVanilla(en, item);
                if (!conflict) applicable.add(en);
            }
        }

        if (applicable.isEmpty()) return;

        Collections.shuffle(applicable);

        for (SEnchantment enchant : applicable) {
            double chance = enchantChances.getOrDefault(enchant.id(), baseChance);
            chance += bonus * perLevelBonus;
            chance = Math.min(chance, 0.95);

            if (Math.random() <= chance) {
                int level = Math.max(1, (int) (Math.random() * enchant.maxLevel()) + 1);
                int currentLevel = enchant.getLevel(item);

                if (currentLevel > 0) {
                    level = Math.min(currentLevel + 1, enchant.maxLevel());
                }

                enchant.setLevel(item, level);

                sf.msg(p, ChatColor.LIGHT_PURPLE + "✨ 附魔台为你附上了 "
                        + enchant.displayName() + " " + SEnchantment.roman(level) + "!");

                p.playSound(p.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.8f, 1.0f);
                break;
            }
        }
    }

    public void setBaseChance(double chance) {
        this.baseChance = chance;
    }

    public void setPerLevelBonus(double bonus) {
        this.perLevelBonus = bonus;
    }

    public void setEnchantChance(String enchantId, double chance) {
        enchantChances.put(enchantId, chance);
    }

    public void addBlacklistWorld(String worldName) {
        blacklistWorlds.add(worldName);
    }

    public void removeBlacklistWorld(String worldName) {
        blacklistWorlds.remove(worldName);
    }
}
