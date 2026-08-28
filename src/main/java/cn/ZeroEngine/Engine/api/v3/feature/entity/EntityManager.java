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

/**
 * 自定义生物注册中心 + 生成 API
 *
 * 装配：sf.entities().register(new MyMob())
 * 查询：sf.entities().get("my_mob")
 * 生成：sf.entities().spawn("my_mob", location)
 * 反查：sf.entities().find(livingEntity)
 */
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

    /** 通过 PDC 反查自定义生物类型（null=不是自定义生物） */
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

    /** 当前活动实例数 */
    public int activeCount() {
        return active.size();
    }

    /** 单个类型当前实例数 */
    public int activeCount(String id) {
        int c = 0;
        for (SEntity e : active.values()) if (e.id().equals(id)) c++;
        return c;
    }

    public Map<UUID, SEntity> activeMap() {
        return Collections.unmodifiableMap(active);
    }

    /**
     * 从活动实例追踪表移除指定实体（用于 EntityListener 在 tick 调度中清理已死亡实体）
     * 必须通过此方法移除，因为 activeMap() 返回的是 UnmodifiableMap，不支持迭代器 remove。
     * @return 被移除的 SEntity 定义，或 null（不存在）
     */
    public SEntity removeActive(UUID entityId) {
        return active.remove(entityId);
    }

    /**
     * 清空所有活动实例追踪（用于 shutdown）
     */
    public void clearActive() {
        active.clear();
    }

    /**
     * 在指定位置生成自定义生物（不校验 SpawnCondition.matches，强制生成）
     * @return 生成的实体，失败返回 null
     */
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

    /**
     * 尝试在指定位置自然生成（会校验 SpawnCondition.matches，失败返回 null）
     */
    public LivingEntity trySpawn(String id, Location loc) {
        if (loc == null) return null;
        SEntity e = registry.get(id);
        if (e == null) return null;
        if (!e.spawnCondition().matches(loc)) return null;
        return spawn(id, loc);
    }

    /**
     * 把一个已存在的 LivingEntity 转换为自定义生物（拦截原版生物生成时用）
     */
    public LivingEntity convert(SEntity e, LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        if (e == null || entity == null) return null;
        apply(e, entity, loc, reason);
        active.put(entity.getUniqueId(), e);
        return entity;
    }

    /** 应用全部自定义生物属性到实体 */
    private void apply(SEntity e, LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        // 1. 打标签
        e.tag(entity);
        // 2. 应用属性
        e.applyAttributes(entity);
        // 3. 应用装备
        List<SEntity.EquipmentEntry> eq = e.equipment();
        if (eq != null && !eq.isEmpty()) {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                for (SEntity.EquipmentEntry entry : eq) {
                    entry.applyTo(equipment);
                }
            }
        }
        // 4. 触发生成回调
        try {
            e.onSpawn(entity, loc, reason);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onSpawn error: " + e.id(), t);
        }
    }

    /** 死亡时清理活动表 */
    public void onEntityDeath(UUID entityId) {
        active.remove(entityId);
    }

    /** 启动时清理无效引用（实体已被卸载） */
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
