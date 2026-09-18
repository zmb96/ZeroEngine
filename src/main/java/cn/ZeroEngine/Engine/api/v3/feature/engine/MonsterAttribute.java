package cn.ZeroEngine.Engine.api.v3.feature.engine;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;

public interface MonsterAttribute {

    void setBaseDamage(LivingEntity entity, double damage);

    void setBaseHealth(LivingEntity entity, double health);

    void setBaseSpeed(LivingEntity entity, double speed);

    void setBaseKnockbackResistance(LivingEntity entity, double resistance);

    void setBaseArmor(LivingEntity entity, double armor);

    void setBaseArmorToughness(LivingEntity entity, double toughness);

    void scale(LivingEntity entity, double healthMul, double damageMul, double speedMul);

    void reset(LivingEntity entity);

    void applyPersistent(UUID entityId, Map<Attribute, Double> modifiers);

    Map<Attribute, Double> getPersistent(UUID entityId);

    void clearPersistent(UUID entityId);

    double get(Attribute attribute, LivingEntity entity);

    void set(Attribute attribute, LivingEntity entity, double value);

    void addModifier(Attribute attribute, LivingEntity entity, String name, double amount, org.bukkit.attribute.AttributeModifier.Operation operation);

    void removeModifier(Attribute attribute, LivingEntity entity, String name);

    default AttributeInstance getInstance(LivingEntity entity, Attribute attribute) {
        AttributeInstance inst = entity.getAttribute(attribute);
        if (inst == null) {
            try {
                inst = entity.getAttribute(org.bukkit.Registry.ATTRIBUTE.getOrThrow(attribute.getKey()));
            } catch (Exception ignored) {
            }
        }
        return inst;
    }
}
