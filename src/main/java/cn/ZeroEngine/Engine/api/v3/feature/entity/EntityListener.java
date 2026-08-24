package cn.ZeroEngine.Engine.api.v3.feature.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 自定义生物事件监听器 —— 处理生成/攻击/死亡/燃烧/目标/tick
 *
 * 装配时：
 *   EntityListener listener = new EntityListener(manager);
 *   sf.regEvent(listener, plugin);
 *   listener.startTick(plugin, sf);   // 启动 SFTick 调度
 */
public class EntityListener implements Listener {

    private final EntityManager manager;
    private BukkitTask tickTask;
    private BukkitTask perSecondTask;

    public EntityListener(EntityManager manager) {
        this.manager = manager;
    }

    // ==================== 生成拦截 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        // 已经是自定义生物（来自 manager.spawn），不拦截
        if (manager.isCustom(living)) return;

        // 对原版同类型生物，按 replaceVanillaSpawns + chance 转换
        for (SEntity def : manager.all()) {
            if (!def.spawnCondition().replaceVanillaSpawns) continue;
            if (def.entityType() != living.getType()) continue;
            if (!def.spawnCondition().matches(e.getLocation())) continue;

            // 转换：打标签 + 应用属性装备
            manager.convert(def, living, e.getLocation(), e.getSpawnReason());
            return;
        }
    }

    // ==================== 攻击玩家监听 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity attacker)) return;
        SEntity def = manager.find(attacker);
        if (def == null) return;

        // 敌对生物才会触发 onAttack
        if (e.getEntity() instanceof Player) {
            try {
                def.onAttack(attacker, e.getEntity() instanceof LivingEntity ? (LivingEntity) e.getEntity() : null,
                        e.getDamage(), e);
            } catch (Throwable t) {
                SF.sf().error("[Entity] onAttack error: " + def.id(), t);
            }
        }
    }

    // ==================== 受伤监听 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        SEntity def = manager.find(living);
        if (def == null) return;

        try {
            def.onDamaged(living, e);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onDamaged error: " + def.id(), t);
        }
    }

    // ==================== 死亡掉落 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity living = e.getEntity();
        SEntity def = manager.find(living);
        if (def == null) return;

        // 追加额外掉落
        List<ItemStack> extra = def.deathDrops();
        if (extra != null && !extra.isEmpty()) {
            e.getDrops().addAll(extra);
        }

        // 清理活动表 + 触发回调
        manager.onEntityDeath(living.getUniqueId());
        try {
            def.onDeath(living, e);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onDeath error: " + def.id(), t);
        }
    }

    // ==================== 目标 / 阵营 ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        SEntity def = manager.find(living);
        if (def == null) return;

        // 中立 / 和平生物：除非被攻击，否则取消目标
        switch (def.hostility()) {
            case PASSIVE -> {
                if (e.getTarget() instanceof Player) e.setCancelled(true);
            }
            case NEUTRAL -> {
                // 只允许在受伤后追踪（Player最近攻击过）—— 简化：仅取消自然生成导致的追踪
                if (e.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER
                        || e.getReason() == EntityTargetEvent.TargetReason.RANDOM_TARGET) {
                    e.setCancelled(true);
                }
            }
            case HOSTIVE -> { /* 敌对：让原版逻辑跑 */ }
            default -> {}
        }

        try {
            def.onTarget(e);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onTarget error: " + def.id(), t);
        }
    }

    // ==================== 光照燃烧（怕光照） ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        SEntity def = manager.find(living);
        if (def == null) return;

        // burnInDaylight=false 的自定义生物：取消燃烧
        if (!def.spawnCondition().burnInDaylight) {
            e.setCancelled(true);
        }
    }

    // ==================== SFTick 调度 ====================

    /**
     * 启动周期调度
     * @param sfTicks 每 N 个 SFTick 触发一次 onTick（默认 5 = 1 Bukkit tick / 50ms）
     * @param perSecondTicks 每 N 个 SFTick 触发一次 onPerSecond（默认 100 = 1 秒）
     */
    public void startTick(JavaPlugin plugin, SF sf, long sfTicks, long perSecondTicks) {
        long bukkitTick = Math.max(1, sfTicks / 5);
        long bukkitSecond = Math.max(1, perSecondTicks / 5);

        // onTick —— 每 bukkitTick 个 Bukkit tick 跑一次
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, SEntity>> it = manager.activeMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, SEntity> en = it.next();
                Entity ent = sf.bukkit().getEntity(en.getKey());
                if (ent == null || ent.isDead() || !ent.isValid() || !(ent instanceof LivingEntity living)) {
                    it.remove();
                    continue;
                }
                try {
                    en.getValue().onTick(living, sf.tick().now());
                } catch (Throwable t) {
                    sf.error("[Entity] onTick error: " + en.getValue().id(), t);
                }
            }
        }, 1L, bukkitTick);

        // onPerSecond —— 每 bukkitSecond 个 Bukkit tick 跑一次
        perSecondTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, SEntity> en : manager.activeMap().entrySet()) {
                Entity ent = sf.bukkit().getEntity(en.getKey());
                if (ent == null || ent.isDead() || !ent.isValid() || !(ent instanceof LivingEntity living)) continue;
                try {
                    en.getValue().onPerSecond(living, sf.tick().now());
                } catch (Throwable t) {
                    sf.error("[Entity] onPerSecond error: " + en.getValue().id(), t);
                }
            }
        }, 20L, bukkitSecond);
    }

    /** 默认调度：onTick 每 5 SFTick（1 Bukkit tick），onPerSecond 每 100 SFTick（1 秒） */
    public void startTick(JavaPlugin plugin, SF sf) {
        startTick(plugin, sf, 5L, 100L);
    }

    public void shutdown() {
        if (tickTask != null) tickTask.cancel();
        if (perSecondTask != null) perSecondTask.cancel();
    }
}
