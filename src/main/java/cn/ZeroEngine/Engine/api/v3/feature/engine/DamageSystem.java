package cn.ZeroEngine.Engine.api.v3.feature.engine;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;
import java.util.function.BiFunction;

public interface DamageSystem {

    interface DamageContext {
        LivingEntity attacker();

        LivingEntity victim();

        double rawDamage();

        EntityDamageEvent.DamageCause cause();

        boolean isCritical();

        void setDamage(double damage);

        void setCancelled(boolean cancelled);

        boolean isCancelled();
    }

    void registerDamageModifier(String name, int priority, BiFunction<DamageContext, Double, Double> modifier);

    void unregisterDamageModifier(String name);

    double calculateDamage(LivingEntity attacker, LivingEntity victim, double rawDamage, EntityDamageEvent.DamageCause cause);

    void setPvpEnabled(boolean enabled);

    boolean isPvpEnabled();

    void setPvpEnabled(UUID worldId, boolean enabled);

    boolean isPvpEnabled(UUID worldId);

    void setDamageMultiplier(EntityDamageEvent.DamageCause cause, double multiplier);

    double getDamageMultiplier(EntityDamageEvent.DamageCause cause);

    void resetDamageMultiplier(EntityDamageEvent.DamageCause cause);

    void setArmorPenetration(double percent);

    double getArmorPenetration();

    void setCustomDamage(LivingEntity attacker, LivingEntity victim, double damage);

    void clearCustomDamage(LivingEntity attacker);
}
