package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.HashMap;
import java.util.Map;

/**
 * 铁砧 SF 自定义附魔监听。
 *
 * 原则：
 *  1) 不覆盖其他插件/原版已写入的 result（先 e.getResult() 再叠加我们的附魔）
 *  2) 只改"SF 自定义附魔"相关字段，原版附魔、其他插件写进 meta/PDC 的数据原样保留
 *  3) 修复/重命名/合并物品也走本逻辑；只要铁砧没被 cancel，我们就在 result 上正确保留
 */
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

        // 保留其他插件/原版/玩家改好的 result（比如其他插件已写经验消耗或自定义附魔）
        ItemStack result = e.getResult();
        if (result == null || result.getType().isAir()) {
            // 没 result（例如只是"重命名物品"或"合并两本附魔书"，Bukkit 有时还没生成 result）
            // 则基于 target 克隆一份；后续我们的合并逻辑只叠加 SF 附魔
            result = target.clone();
        }

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
                if (conflict) continue;

                int current = enchant.getLevel(result);
                final int newLevel;

                if (current == 0) {
                    // 首次附魔：直接写 bookLevel
                    newLevel = Math.min(bookLevel, enchant.maxLevel());
                } else if (current == bookLevel) {
                    // 同等级合并：升级 +1，但最高等级不允许再合并
                    if (bookLevel >= enchant.maxLevel()) continue;
                    newLevel = bookLevel + 1;
                } else {
                    // 不同等级：跳过（不覆盖）
                    continue;
                }

                enchant.setLevel(result, newLevel);
                extraCost += enchant.anvilCost() * newLevel;
            }
        }

        if (extraCost > 0) {
            // 把经验消耗叠加到已有的 repairCost 上（不覆盖其他插件设置好的消耗）
            try {
                ItemMeta meta = result.getItemMeta();
                if (meta instanceof Repairable rep) {
                    rep.setRepairCost(rep.getRepairCost() + extraCost);
                    result.setItemMeta(meta);
                } else {
                    inv.setRepairCost(Math.max(inv.getRepairCost(), extraCost));
                }
            } catch (Throwable ignoreDeprecated) {
                try { inv.setRepairCost(Math.max(inv.getRepairCost(), extraCost)); } catch (Throwable ignore) {}
            }
            e.setResult(result);
        }
        // 注意：如果 extraCost == 0，绝不调用 setResult —— 避免把其他插件/原版已经写好的 result 被
        //       我们的 target.clone() 版本覆盖（尽管上面优先使用 e.getResult() 已降低此风险）。
    }
}
