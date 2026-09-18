package cn.ZeroEngine.Engine.api.v3.feature.main;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReachManager {

    private static final UUID BLOCK_MOD = UUID.nameUUIDFromBytes("sf_reach_block".getBytes());
    private static final UUID ENTITY_MOD = UUID.nameUUIDFromBytes("sf_reach_entity".getBytes());

    private final ConcurrentHashMap<UUID, double[]> customReach = new ConcurrentHashMap<>();
    private final Attribute blockAttr;
    private final Attribute entityAttr;

    public ReachManager() {
        this.blockAttr = findAttribute("BLOCK_INTERACTION_RANGE");
        this.entityAttr = findAttribute("ENTITY_INTERACTION_RANGE");
    }

    private Attribute findAttribute(String suffix) {
        String[] candidates = {
                "GENERIC_" + suffix,
                "PLAYER_" + suffix,
                suffix
        };
        for (String name : candidates) {
            try {
                Field f = Attribute.class.getField(name);
                Object val = f.get(null);
                if (val instanceof Attribute a) return a;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
        return null;
    }

    public boolean isSupported() {
        return blockAttr != null && entityAttr != null;
    }

    public double getBlockReach(Player p) {
        if (blockAttr == null) return 4.5;
        AttributeInstance inst = p.getAttribute(blockAttr);
        return inst != null ? inst.getValue() : 4.5;
    }

    public double getEntityReach(Player p) {
        if (entityAttr == null) return 3.0;
        AttributeInstance inst = p.getAttribute(entityAttr);
        return inst != null ? inst.getValue() : 3.0;
    }

    public void setBlockReach(Player p, double range) {
        if (blockAttr == null) return;
        AttributeInstance inst = p.getAttribute(blockAttr);
        if (inst == null) return;
        inst.setBaseValue(range);
        customReach.computeIfAbsent(p.getUniqueId(), k -> new double[]{4.5, 3.0})[0] = range;
    }

    public void setEntityReach(Player p, double range) {
        if (entityAttr == null) return;
        AttributeInstance inst = p.getAttribute(entityAttr);
        if (inst == null) return;
        inst.setBaseValue(range);
        customReach.computeIfAbsent(p.getUniqueId(), k -> new double[]{4.5, 3.0})[1] = range;
    }

    public void addBlockReach(Player p, double bonus) {
        if (blockAttr == null) return;
        AttributeInstance inst = p.getAttribute(blockAttr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(BLOCK_MOD);
        if (existing != null) inst.removeModifier(existing);
        inst.addModifier(new AttributeModifier(BLOCK_MOD, "sf_reach_block",
                bonus, AttributeModifier.Operation.ADD_NUMBER));
    }

    public void addEntityReach(Player p, double bonus) {
        if (entityAttr == null) return;
        AttributeInstance inst = p.getAttribute(entityAttr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(ENTITY_MOD);
        if (existing != null) inst.removeModifier(existing);
        inst.addModifier(new AttributeModifier(ENTITY_MOD, "sf_reach_entity",
                bonus, AttributeModifier.Operation.ADD_NUMBER));
    }

    public void removeBlockReachBonus(Player p) {
        if (blockAttr == null) return;
        AttributeInstance inst = p.getAttribute(blockAttr);
        if (inst == null) return;
        AttributeModifier m = inst.getModifier(BLOCK_MOD);
        if (m != null) inst.removeModifier(m);
    }

    public void removeEntityReachBonus(Player p) {
        if (entityAttr == null) return;
        AttributeInstance inst = p.getAttribute(entityAttr);
        if (inst == null) return;
        AttributeModifier m = inst.getModifier(ENTITY_MOD);
        if (m != null) inst.removeModifier(m);
    }

    public void resetBlockReach(Player p) {
        if (blockAttr == null) return;
        AttributeInstance inst = p.getAttribute(blockAttr);
        if (inst == null) return;
        inst.setBaseValue(4.5);
        AttributeModifier m = inst.getModifier(BLOCK_MOD);
        if (m != null) inst.removeModifier(m);
    }

    public void resetEntityReach(Player p) {
        if (entityAttr == null) return;
        AttributeInstance inst = p.getAttribute(entityAttr);
        if (inst == null) return;
        inst.setBaseValue(3.0);
        AttributeModifier m = inst.getModifier(ENTITY_MOD);
        if (m != null) inst.removeModifier(m);
    }

    public void reset(Player p) {
        resetBlockReach(p);
        resetEntityReach(p);
        customReach.remove(p.getUniqueId());
    }

    public boolean hasCustom(Player p) {
        return customReach.containsKey(p.getUniqueId());
    }
}
