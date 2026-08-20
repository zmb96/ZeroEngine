package cn.ZeroEngine.Engine.api.v2.feature.engine.impl;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.feature.engine.MonsterAttribute;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MonsterAttributeImpl implements MonsterAttribute {

    private final JavaPlugin plugin;
    private final Map<UUID, Map<Attribute, Double>> persistent = new ConcurrentHashMap<>();

    public MonsterAttributeImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBaseDamage(LivingEntity entity, double damage) {
        set(Attribute.GENERIC_ATTACK_DAMAGE, entity, damage);
    }

    @Override
    public void setBaseHealth(LivingEntity entity, double health) {
        set(Attribute.GENERIC_MAX_HEALTH, entity, health);
        entity.setHealth(Math.min(entity.getHealth(), health));
    }

    @Override
    public void setBaseSpeed(LivingEntity entity, double speed) {
        set(Attribute.GENERIC_MOVEMENT_SPEED, entity, speed);
    }

    @Override
    public void setBaseKnockbackResistance(LivingEntity entity, double resistance) {
        set(Attribute.GENERIC_KNOCKBACK_RESISTANCE, entity, resistance);
    }

    @Override
    public void setBaseArmor(LivingEntity entity, double armor) {
        set(Attribute.GENERIC_ARMOR, entity, armor);
    }

    @Override
    public void setBaseArmorToughness(LivingEntity entity, double toughness) {
        set(Attribute.GENERIC_ARMOR_TOUGHNESS, entity, toughness);
    }

    @Override
    public void scale(LivingEntity entity, double healthMul, double damageMul, double speedMul) {
        AttributeInstance health = getInstance(entity, Attribute.GENERIC_MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(health.getBaseValue() * healthMul);
            entity.setHealth(Math.min(entity.getHealth(), health.getBaseValue()));
        }
        AttributeInstance damage = getInstance(entity, Attribute.GENERIC_ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damage.getBaseValue() * damageMul);
        }
        AttributeInstance speed = getInstance(entity, Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(speed.getBaseValue() * speedMul);
        }
    }

    @Override
    public void reset(LivingEntity entity) {
        for (Attribute attr : Attribute.values()) {
            AttributeInstance inst = getInstance(entity, attr);
            if (inst != null) {
                for (AttributeModifier mod : new ArrayList<>(inst.getModifiers())) {
                    inst.removeModifier(mod);
                }
                inst.setBaseValue(inst.getDefaultValue());
            }
        }
        clearPersistent(entity.getUniqueId());
    }

    @Override
    public void applyPersistent(UUID entityId, Map<Attribute, Double> modifiers) {
        persistent.put(entityId, new ConcurrentHashMap<>(modifiers));
    }

    @Override
    public Map<Attribute, Double> getPersistent(UUID entityId) {
        return persistent.getOrDefault(entityId, Collections.emptyMap());
    }

    @Override
    public void clearPersistent(UUID entityId) {
        persistent.remove(entityId);
    }

    @Override
    public double get(Attribute attribute, LivingEntity entity) {
        AttributeInstance inst = getInstance(entity, attribute);
        return inst != null ? inst.getValue() : 0;
    }

    @Override
    public void set(Attribute attribute, LivingEntity entity, double value) {
        AttributeInstance inst = getInstance(entity, attribute);
        if (inst != null) {
            inst.setBaseValue(value);
        }
    }

    @Override
    public void addModifier(Attribute attribute, LivingEntity entity, String name, double amount, AttributeModifier.Operation operation) {
        AttributeInstance inst = getInstance(entity, attribute);
        if (inst != null) {
            inst.addModifier(new AttributeModifier(
                    new UUID(name.hashCode(), name.hashCode() + 1),
                    name, amount, operation
            ));
        }
    }

    @Override
    public void removeModifier(Attribute attribute, LivingEntity entity, String name) {
        AttributeInstance inst = getInstance(entity, attribute);
        if (inst != null) {
            for (AttributeModifier mod : new ArrayList<>(inst.getModifiers())) {
                if (mod.getName().equals(name)) {
                    inst.removeModifier(mod);
                }
            }
        }
    }
}
