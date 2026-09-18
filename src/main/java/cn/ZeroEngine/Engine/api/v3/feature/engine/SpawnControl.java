package cn.ZeroEngine.Engine.api.v3.feature.engine;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface SpawnControl {

    interface SpawnRule {
        String name();

        EntityType type();

        double chance();

        int maxPerChunk();

        List<String> worlds();

        boolean enabled();

        void setChance(double chance);

        void setMaxPerChunk(int max);

        void setEnabled(boolean enabled);
    }

    SpawnRule createRule(String name, EntityType type, double chance, int maxPerChunk, List<String> worlds);

    void registerRule(SpawnRule rule);

    void unregisterRule(String name);

    SpawnRule getRule(String name);

    List<SpawnRule> allRules();

    void blacklistEntity(EntityType type, List<String> worlds);

    void unblacklistEntity(EntityType type, List<String> worlds);

    boolean isBlacklisted(EntityType type, String worldName);

    void setSpawnCap(UUID worldId, EntityType type, int cap);

    int getSpawnCap(UUID worldId, EntityType type);

    void registerSpawnFilter(Predicate<EntityType> filter);

    void unregisterSpawnFilter(Predicate<EntityType> filter);

    void registerLocationFilter(BiPredicate<EntityType, Location> filter);

    void unregisterLocationFilter(BiPredicate<EntityType, Location> filter);

    boolean forceSpawn(EntityType type, Location location, int count);

    void clearEntities(UUID worldId, EntityType type);

    Map<EntityType, Integer> getEntityCounts(UUID worldId);
}
