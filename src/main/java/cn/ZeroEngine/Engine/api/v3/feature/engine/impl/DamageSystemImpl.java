package cn.ZeroEngine.Engine.api.v3.feature.engine.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v3.feature.engine.DamageSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class DamageSystemImpl implements DamageSystem, Listener {

    private final JavaPlugin plugin;
    private final Map<String, ModifierEntry> modifiers = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> pvpWorlds = new ConcurrentHashMap<>();
    private final Map<EntityDamageEvent.DamageCause, Double> damageMultipliers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Double>> customDamage = new ConcurrentHashMap<>();
    private boolean globalPvp = true;
    private double armorPenetration = 0;

    private record ModifierEntry(int priority, BiFunction<DamageContext, Double, Double> fn) {}

    private static class CtxImpl implements DamageContext {
        private final LivingEntity attacker;
        private final LivingEntity victim;
        private final double raw;
        private final EntityDamageEvent.DamageCause cause;
        private final boolean crit;
        private double damage;
        private boolean cancelled;

        CtxImpl(LivingEntity attacker, LivingEntity victim, double raw, EntityDamageEvent.DamageCause cause, boolean crit) {
            this.attacker = attacker;
            this.victim = victim;
            this.raw = raw;
            this.cause = cause;
            this.crit = crit;
            this.damage = raw;
        }

        @Override public LivingEntity attacker() { return attacker; }
        @Override public LivingEntity victim() { return victim; }
        @Override public double rawDamage() { return raw; }
        @Override public EntityDamageEvent.DamageCause cause() { return cause; }
        @Override public boolean isCritical() { return crit; }
        @Override public void setDamage(double d) { damage = d; }
        @Override public void setCancelled(boolean c) { cancelled = c; }
        @Override public boolean isCancelled() { return cancelled; }
        double getDamage() { return damage; }
    }

    public DamageSystemImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void registerDamageModifier(String name, int priority, BiFunction<DamageContext, Double, Double> modifier) {
        modifiers.put(name, new ModifierEntry(priority, modifier));
    }

    @Override
    public void unregisterDamageModifier(String name) {
        modifiers.remove(name);
    }

    @Override
    public double calculateDamage(LivingEntity attacker, LivingEntity victim, double rawDamage, EntityDamageEvent.DamageCause cause) {
        CtxImpl ctx = new CtxImpl(attacker, victim, rawDamage, cause, false);
        List<ModifierEntry> sorted = new ArrayList<>(modifiers.values());
        sorted.sort(Comparator.comparingInt(ModifierEntry::priority));
        double result = rawDamage;
        for (ModifierEntry e : sorted) {
            result = e.fn.apply(ctx, result);
            if (ctx.isCancelled()) return 0;
        }
        Double mul = damageMultipliers.get(cause);
        if (mul != null) result *= mul;
        return result;
    }

    @Override
    public void setPvpEnabled(boolean enabled) { globalPvp = enabled; }

    @Override
    public boolean isPvpEnabled() { return globalPvp; }

    @Override
    public void setPvpEnabled(UUID worldId, boolean enabled) { pvpWorlds.put(worldId, enabled); }

    @Override
    public boolean isPvpEnabled(UUID worldId) {
        return pvpWorlds.getOrDefault(worldId, globalPvp);
    }

    @Override
    public void setDamageMultiplier(EntityDamageEvent.DamageCause cause, double multiplier) {
        damageMultipliers.put(cause, multiplier);
    }

    @Override
    public double getDamageMultiplier(EntityDamageEvent.DamageCause cause) {
        return damageMultipliers.getOrDefault(cause, 1.0);
    }

    @Override
    public void resetDamageMultiplier(EntityDamageEvent.DamageCause cause) {
        damageMultipliers.remove(cause);
    }

    @Override
    public void setArmorPenetration(double percent) { armorPenetration = Math.max(0, Math.min(1, percent)); }

    @Override
    public double getArmorPenetration() { return armorPenetration; }

    @Override
    public void setCustomDamage(LivingEntity attacker, LivingEntity victim, double damage) {
        customDamage.computeIfAbsent(attacker.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(victim.getUniqueId(), damage);
    }

    @Override
    public void clearCustomDamage(LivingEntity attacker) {
        customDamage.remove(attacker.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        if (attacker.getType() == org.bukkit.entity.EntityType.PLAYER && victim.getType() == org.bukkit.entity.EntityType.PLAYER) {
            if (!isPvpEnabled(victim.getWorld().getUID())) {
                e.setCancelled(true);
                return;
            }
        }

        Map<UUID, Double> targets = customDamage.get(attacker.getUniqueId());
        if (targets != null && targets.containsKey(victim.getUniqueId())) {
            e.setDamage(targets.get(victim.getUniqueId()));
            return;
        }

        double raw = e.getDamage();
        double result = calculateDamage(attacker, victim, raw, e.getCause());

        if (armorPenetration > 0 && victim.getType() != org.bukkit.entity.EntityType.PLAYER) {
            double armor = victim.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR) != null
                    ? victim.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR).getValue() : 0;
            double reduction = armor * 0.04 * (1 - armorPenetration);
            result = result / (1 + reduction) + result * reduction * armorPenetration / (1 + reduction);
        }

        e.setDamage(result);
    }
}
