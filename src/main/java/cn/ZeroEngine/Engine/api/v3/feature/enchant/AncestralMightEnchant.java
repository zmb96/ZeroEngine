package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

// 祖宗之力附魔 —— 继承先祖之力，全方位强化盔甲
public class AncestralMightEnchant extends SEnchantment {

    // 附魔唯一标识符（用于配置文件、PDC 存储、命令调用）
    @Override
    public String id() { return "ancestral_might"; }

    // 在游戏内显示的名称（Lore 中显示）
    @Override
    public String displayName() { return "祖宗之力"; }

    // 附魔最大等级
    @Override
    public int maxLevel() { return 3; }

    // 附魔描述（/sfenchant info 中展示）
    @Override
    public String description() {
        return "继承先祖之力，获得全方位属性强化";
    }

    // 可附魔的物品类型 —— 所有盔甲部位（简写：HELMET 匹配 *_HELMET 等，6 种材质全命中）
    @Override
    public Set<String> applicableItems() {
        return new HashSet<>(Arrays.asList("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"));
    }

    // 冲突组 —— 同组的附魔互斥（防止玩家把多个同类型增益附魔叠在一件装备上）
    @Override
    public Set<String> conflictGroups() {
        Set<String> g = new HashSet<>();
        g.add("armor_ancestral");
        return g;
    }

    // 铁砧应用附魔时消耗的经验等级倍率（4 = 每级附魔消耗 4 级经验）
    @Override
    public int anvilCost() { return 4; }

    // 属性加成列表 —— 装备时自动应用，卸下时自动移除
    // AttributeBonus.add(name, 属性, 基础值, 每级增量)   → ADD_NUMBER：直接加数值
    // AttributeBonus.multiply(name, 属性, 基础值, 每级增量) → MULTIPLY_SCALAR_1：百分比乘算
    @Override
    public List<AttributeBonus> attributes() {
        return Arrays.asList(
                // 最大生命值：基础+4.0，每级+2.0 → Lv1=+4, Lv2=+6, Lv3=+8（4颗心）
                AttributeBonus.add("max_health", "GENERIC_MAX_HEALTH", 4.0, 2.0),
                // 攻击伤害：基础+2.0，每级+1.0 → Lv1=+2, Lv2=+3, Lv3=+4
                AttributeBonus.add("attack_damage", "GENERIC_ATTACK_DAMAGE", 2.0, 1.0),
                // 护甲：基础+2.0，每级+1.0
                AttributeBonus.add("armor", "GENERIC_ARMOR", 2.0, 1.0),
                // 护甲韧性：基础+1.0，每级+0.5
                AttributeBonus.add("armor_toughness", "GENERIC_ARMOR_TOUGHNESS", 1.0, 0.5),
                // 击退抗性：基础+0.1，每级+0.05（1.0=完全免疫击退）
                AttributeBonus.add("knockback_resistance", "GENERIC_KNOCKBACK_RESISTANCE", 0.1, 0.05),
                // 移动速度：基础+5%，每级+2%（乘算）
                AttributeBonus.multiply("movement_speed", "GENERIC_MOVEMENT_SPEED", 0.05, 0.02)
        );
    }

    // 受伤时触发 —— Lv2+ 有概率回血并播放不死图腾粒子特效
    @Override
    public void onDamaged(EnchantContext ctx) {
        if (ctx.level() < 2) return;
        if (Math.random() < 0.05 * ctx.level()) {
            Player p = ctx.player();
            p.setHealth(Math.min(p.getHealth() + 2.0,
                    p.getAttribute(findAttribute("GENERIC_MAX_HEALTH")).getValue()));
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    p.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.5f);
        }
    }

    // 装备时触发 —— 播放信标激活音效 + 末地烛粒子（视觉反馈：先祖之力已激活）
    @Override
    public void onEquip(Player player, ItemStack item, int level) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 0.8f);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
    }

    // 卸下时触发 —— 播放信标关闭音效（视觉反馈：先祖之力已消退）
    @Override
    public void onUnequip(Player player, ItemStack item, int level) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 0.8f);
    }

    // 每隔一段时间触发（由 EnchantAttributeListener 的 tick 任务调用，默认每 2 秒一次）
    // Lv3+ 有 2% 概率散发附魔粒子（持续视觉反馈：先祖之力在闪耀）
    @Override
    public void onTick(Player player, ItemStack item, int level) {
        if (level >= 3 && Math.random() < 0.02) {
            player.getWorld().spawnParticle(Particle.ENCHANT,
                    player.getLocation().add(0, 1.5, 0), 3, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
