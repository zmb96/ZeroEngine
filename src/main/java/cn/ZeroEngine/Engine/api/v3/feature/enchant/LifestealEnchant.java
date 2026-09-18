package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LifestealEnchant extends SEnchantment {

    @Override
    public String id() { return "lifesteal"; }

    @Override
    public String displayName() { return "生命窃取"; }

    @Override
    public int maxLevel() { return 3; }

    @Override
    public Set<String> applicableItems() {
        return new HashSet<>(Arrays.asList("SWORD"));   // 简写：匹配所有以 _SWORD 结尾的材质（6 种剑）
    }

    @Override
    public Set<String> conflicts() {
        Set<String> c = new HashSet<>();
        c.add("DAMAGE_ALL");
        return c;
    }

    @Override
    public String description() {
        return "攻击时回复生命值";
    }

    @Override
    public void onAttack(EnchantContext ctx) {
        if (ctx.target() == null) return;
        double healPercent = 0.05 + 0.05 * ctx.level();
        double heal = ctx.damage() * healPercent;
        double newHealth = Math.min(ctx.player().getHealth() + heal,
                ctx.player().getAttribute(findAttribute("GENERIC_MAX_HEALTH")).getValue());
        ctx.player().setHealth(newHealth);
        ctx.player().getWorld().spawnParticle(Particle.HEART,
                ctx.player().getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.01);
    }
}
