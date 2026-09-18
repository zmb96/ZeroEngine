package cn.ZeroEngine.Engine.api.v3.feature.engine.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v3.feature.engine.SpawnControl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SpawnControlImpl implements SpawnControl, Listener {

    private final JavaPlugin plugin;
    private final Map<String, SpawnRule> rules = new ConcurrentHashMap<>();
    private final Map<String, Set<EntityType>> blacklist = new ConcurrentHashMap<>();
    private final Map<UUID, Map<EntityType, Integer>> spawnCaps = new ConcurrentHashMap<>();
    private final List<Predicate<EntityType>> typeFilters = new ArrayList<>();
    private final List<BiPredicate<EntityType, Location>> locationFilters = new ArrayList<>();

    private static class SpawnRuleImpl implements SpawnRule {
        private final String name;
        private final EntityType type;
        private double chance;
        private int maxPerChunk;
        private final List<String> worlds;
        private boolean enabled;

        SpawnRuleImpl(String name, EntityType type, double chance, int maxPerChunk, List<String> worlds) {
            this.name = name;
            this.type = type;
            this.chance = chance;
            this.maxPerChunk = maxPerChunk;
            this.worlds = worlds;
            this.enabled = true;
        }

        @Override public String name() { return name; }
        @Override public EntityType type() { return type; }
        @Override public double chance() { return chance; }
        @Override public int maxPerChunk() { return maxPerChunk; }
        @Override public List<String> worlds() { return worlds; }
        @Override public boolean enabled() { return enabled; }
        @Override public void setChance(double c) { chance = c; }
        @Override public void setMaxPerChunk(int max) { maxPerChunk = max; }
        @Override public void setEnabled(boolean e) { enabled = e; }
    }

    public SpawnControlImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public SpawnRule createRule(String name, EntityType type, double chance, int maxPerChunk, List<String> worlds) {
        return new SpawnRuleImpl(name, type, chance, maxPerChunk, worlds);
    }

    @Override
    public void registerRule(SpawnRule rule) { rules.put(rule.name(), rule); }

    @Override
    public void unregisterRule(String name) { rules.remove(name); }

    @Override
    public SpawnRule getRule(String name) { return rules.get(name); }

    @Override
    public List<SpawnRule> allRules() { return new ArrayList<>(rules.values()); }

    @Override
    public void blacklistEntity(EntityType type, List<String> worlds) {
        for (String w : worlds) {
            blacklist.computeIfAbsent(w, k -> ConcurrentHashMap.newKeySet()).add(type);
        }
    }

    @Override
    public void unblacklistEntity(EntityType type, List<String> worlds) {
        for (String w : worlds) {
            Set<EntityType> set = blacklist.get(w);
            if (set != null) set.remove(type);
        }
    }

    @Override
    public boolean isBlacklisted(EntityType type, String worldName) {
        Set<EntityType> set = blacklist.get(worldName);
        return set != null && set.contains(type);
    }

    @Override
    public void setSpawnCap(UUID worldId, EntityType type, int cap) {
        spawnCaps.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>()).put(type, cap);
    }

    @Override
    public int getSpawnCap(UUID worldId, EntityType type) {
        Map<EntityType, Integer> caps = spawnCaps.get(worldId);
        return caps != null ? caps.getOrDefault(type, -1) : -1;
    }

    @Override
    public void registerSpawnFilter(Predicate<EntityType> filter) { typeFilters.add(filter); }

    @Override
    public void unregisterSpawnFilter(Predicate<EntityType> filter) { typeFilters.remove(filter); }

    @Override
    public void registerLocationFilter(BiPredicate<EntityType, Location> filter) { locationFilters.add(filter); }

    @Override
    public void unregisterLocationFilter(BiPredicate<EntityType, Location> filter) { locationFilters.remove(filter); }

    @Override
    public boolean forceSpawn(EntityType type, Location location, int count) {
        for (int i = 0; i < count; i++) {
            location.getWorld().spawnEntity(location, type);
        }
        return true;
    }

    @Override
    public void clearEntities(UUID worldId, EntityType type) {
        Bukkit.getWorld(worldId).getEntities().stream()
                .filter(e -> e.getType() == type)
                .forEach(Entity::remove);
    }

    @Override
    public Map<EntityType, Integer> getEntityCounts(UUID worldId) {
        Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);
        for (Entity e : Bukkit.getWorld(worldId).getEntities()) {
            counts.merge(e.getType(), 1, Integer::sum);
        }
        return counts;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        EntityType type = e.getEntityType();
        String worldName = e.getLocation().getWorld().getName();

        if (isBlacklisted(type, worldName)) {
            e.setCancelled(true);
            return;
        }

        for (Predicate<EntityType> f : typeFilters) {
            if (!f.test(type)) {
                e.setCancelled(true);
                return;
            }
        }

        for (BiPredicate<EntityType, Location> f : locationFilters) {
            if (!f.test(type, e.getLocation())) {
                e.setCancelled(true);
                return;
            }
        }

        UUID worldId = e.getLocation().getWorld().getUID();
        int cap = getSpawnCap(worldId, type);
        if (cap >= 0) {
            long current = Bukkit.getWorld(worldId).getEntities().stream()
                    .filter(ent -> ent.getType() == type)
                    .count();
            if (current >= cap) {
                e.setCancelled(true);
                return;
            }
        }

        for (SpawnRule rule : rules.values()) {
            if (!rule.enabled() || rule.type() != type) continue;
            if (!rule.worlds().isEmpty() && !rule.worlds().contains(worldName)) continue;
            if (Math.random() > rule.chance()) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
