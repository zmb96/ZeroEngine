package cn.ZeroEngine.Engine.api.v3.feature.entity;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {

    private final Map<String, SEntity> registry = new HashMap<>();
    private final Map<UUID, SEntity> active = new ConcurrentHashMap<>();

    public EntityManager register(SEntity entity) {
        SF sf = SF.sf();
        String id = entity.id();
        if (registry.containsKey(id)) {
            throw new IllegalStateException("Entity already registered: " + id);
        }
        registry.put(id, entity);
        sf.info("[Entity] Registered: " + id + " (" + entity.displayName() + " type=" + entity.entityType() + ")");
        return this;
    }

    public boolean registerIfAbsent(SEntity entity) {
        if (registry.containsKey(entity.id())) return false;
        try {
            register(entity);
            return true;
        } catch (IllegalStateException ignore) {
            return false;
        }
    }

    public EntityManager registerAll(SEntity... entities) {
        for (SEntity e : entities) register(e);
        return this;
    }

    public void unregister(String id) {
        registry.remove(id);
    }

    public void unregisterAll() {
        registry.clear();
    }

    public SEntity get(String id) {
        return registry.get(id);
    }

    public Collection<SEntity> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    
    public SEntity find(LivingEntity entity) {
        if (entity == null) return null;
        for (SEntity e : registry.values()) {
            if (e.is(entity)) return e;
        }
        return null;
    }

    public boolean isCustom(LivingEntity entity) {
        return find(entity) != null;
    }

    
    public int activeCount() {
        return active.size();
    }

    
    public int activeCount(String id) {
        int c = 0;
        for (SEntity e : active.values()) if (e.id().equals(id)) c++;
        return c;
    }

    public Map<UUID, SEntity> activeMap() {
        return Collections.unmodifiableMap(active);
    }

    
    public SEntity removeActive(UUID entityId) {
        return active.remove(entityId);
    }

    
    public void clearActive() {
        active.clear();
    }

    
    public LivingEntity spawn(String id, Location loc) {
        SF sf = SF.sf();
        SEntity e = registry.get(id);
        if (e == null) {
            sf.warn("[Entity] spawn: not registered: " + id);
            return null;
        }
        if (loc == null || loc.getWorld() == null) {
            sf.warn("[Entity] spawn: invalid location");
            return null;
        }

        EntityType type = e.entityType();
        if (type == null || !type.isSpawnable() || !type.isAlive()) {
            sf.warn("[Entity] spawn: invalid EntityType " + type);
            return null;
        }

        Entity raw = loc.getWorld().spawnEntity(loc, type, CreatureSpawnEvent.SpawnReason.CUSTOM);
        if (!(raw instanceof LivingEntity living)) {
            raw.remove();
            sf.warn("[Entity] spawn: " + type + " is not LivingEntity");
            return null;
        }

        apply(e, living, loc, CreatureSpawnEvent.SpawnReason.CUSTOM);
        active.put(living.getUniqueId(), e);
        return living;
    }

    
    public LivingEntity trySpawn(String id, Location loc) {
        if (loc == null) return null;
        SEntity e = registry.get(id);
        if (e == null) return null;
        if (!e.spawnCondition().matches(loc)) return null;
        return spawn(id, loc);
    }

    
    public LivingEntity convert(SEntity e, LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        if (e == null || entity == null) return null;
        apply(e, entity, loc, reason);
        active.put(entity.getUniqueId(), e);
        return entity;
    }

    
    private void apply(SEntity e, LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        
        e.tag(entity);
        
        e.applyAttributes(entity);
        
        List<SEntity.EquipmentEntry> eq = e.equipment();
        if (eq != null && !eq.isEmpty()) {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                for (SEntity.EquipmentEntry entry : eq) {
                    entry.applyTo(equipment);
                }
            }
        }
        
        try {
            e.onSpawn(entity, loc, reason);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onSpawn error: " + e.id(), t);
        }
    }

    
    public void onEntityDeath(UUID entityId) {
        active.remove(entityId);
    }

    
    public void cleanup() {
        SF sf = SF.sf();
        Iterator<Map.Entry<UUID, SEntity>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, SEntity> en = it.next();
            Entity e = sf.bukkit().getEntity(en.getKey());
            if (e == null || e.isDead() || !e.isValid()) {
                it.remove();
            }
        }
    }
}
