package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Objects;

public final class DropSource {

    public enum Type {
        BLOCK_BREAK,
        ENTITY_DEATH,
        FISHING,
        CHEST_LOOT
    }

    public final Type type;
    public final double chance;
    public final Object target;
    public final int minAmount;
    public final int maxAmount;

    public DropSource(Type type, double chance, Object target, int minAmount, int maxAmount) {
        this.type = Objects.requireNonNull(type, "type");
        if (chance < 0) chance = 0;
        if (chance > 1) chance = 1;
        this.chance = chance;
        this.target = target;
        if (minAmount < 1) minAmount = 1;
        if (maxAmount < minAmount) maxAmount = minAmount;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public static DropSource block(Material block, double chance) {
        return new DropSource(Type.BLOCK_BREAK, chance, block, 1, 1);
    }

    public static DropSource block(Material block, double chance, int min, int max) {
        return new DropSource(Type.BLOCK_BREAK, chance, block, min, max);
    }

    public static DropSource mob(EntityType mob, double chance) {
        return new DropSource(Type.ENTITY_DEATH, chance, mob, 1, 1);
    }

    public static DropSource mob(EntityType mob, double chance, int min, int max) {
        return new DropSource(Type.ENTITY_DEATH, chance, mob, min, max);
    }

    public static DropSource fishing(double chance) {
        return new DropSource(Type.FISHING, chance, null, 1, 1);
    }

    public static DropSource fishing(double chance, int min, int max) {
        return new DropSource(Type.FISHING, chance, null, min, max);
    }

    public static DropSource chest(double chance) {
        return new DropSource(Type.CHEST_LOOT, chance, null, 1, 1);
    }

    public static DropSource chest(double chance, int min, int max) {
        return new DropSource(Type.CHEST_LOOT, chance, null, min, max);
    }

    public int rollAmount(java.util.Random random) {
        if (minAmount == maxAmount) return minAmount;
        return minAmount + random.nextInt(maxAmount - minAmount + 1);
    }

    public boolean roll(java.util.Random random) {
        if (chance >= 1.0) return true;
        if (chance <= 0.0) return false;
        return random.nextDouble() < chance;
    }
}
