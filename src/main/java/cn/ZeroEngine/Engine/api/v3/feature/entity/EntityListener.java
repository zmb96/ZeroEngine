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

public class EntityListener implements Listener {

    private final EntityManager manager;
    private BukkitTask tickTask;
    private BukkitTask perSecondTask;

    public EntityListener(EntityManager manager) {
        this.manager = manager;
    }

    

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        
        if (manager.isCustom(living)) return;

        
        for (SEntity def : manager.all()) {
            if (!def.spawnCondition().replaceVanillaSpawns) continue;
            if (def.entityType() != living.getType()) continue;
            if (!def.spawnCondition().matches(e.getLocation())) continue;

            
            manager.convert(def, living, e.getLocation(), e.getSpawnReason());
            return;
        }
    }

    

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity attacker)) return;
        SEntity def = manager.find(attacker);
        if (def == null) return;

        
        if (e.getEntity() instanceof Player) {
            try {
                def.onAttack(attacker, e.getEntity() instanceof LivingEntity ? (LivingEntity) e.getEntity() : null,
                        e.getDamage(), e);
            } catch (Throwable t) {
                SF.sf().error("[Entity] onAttack error: " + def.id(), t);
            }
        }
    }

    

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

    

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity living = e.getEntity();
        SEntity def = manager.find(living);
        if (def == null) return;

        
        List<ItemStack> extra = def.deathDrops();
        if (extra != null && !extra.isEmpty()) {
            e.getDrops().addAll(extra);
        }

        
        manager.onEntityDeath(living.getUniqueId());
        try {
            def.onDeath(living, e);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onDeath error: " + def.id(), t);
        }
    }

    

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        SEntity def = manager.find(living);
        if (def == null) return;

        
        switch (def.hostility()) {
            case PASSIVE -> {
                if (e.getTarget() instanceof Player) e.setCancelled(true);
            }
            case NEUTRAL -> {
                
                if (e.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER
                        || e.getReason() == EntityTargetEvent.TargetReason.RANDOM_TARGET) {
                    e.setCancelled(true);
                }
            }
            case HOSTILE -> {  }
            default -> {}
        }

        try {
            def.onTarget(e);
        } catch (Throwable t) {
            SF.sf().error("[Entity] onTarget error: " + def.id(), t);
        }
    }

    

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombust(EntityCombustEvent e) {
        if (!(e.getEntity() instanceof LivingEntity living)) return;
        SEntity def = manager.find(living);
        if (def == null) return;

        
        if (!def.spawnCondition().burnInDaylight) {
            e.setCancelled(true);
        }
    }

    

    
    public void startTick(JavaPlugin plugin, SF sf, long sfTicks, long perSecondTicks) {
        long bukkitTick = Math.max(1, sfTicks / 5);
        long bukkitSecond = Math.max(1, perSecondTicks / 5);

        
        
        
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (UUID id : new java.util.ArrayList<>(manager.activeMap().keySet())) {
                Entity ent = sf.bukkit().getEntity(id);
                if (ent == null || ent.isDead() || !ent.isValid() || !(ent instanceof LivingEntity living)) {
                    manager.removeActive(id);
                    continue;
                }
                SEntity def = manager.activeMap().get(id);
                if (def == null) continue;
                try {
                    def.onTick(living, sf.tick().now());
                } catch (Throwable t) {
                    sf.error("[Entity] onTick error: " + def.id(), t);
                }
            }
        }, 1L, bukkitTick);

        
        perSecondTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (UUID id : new java.util.ArrayList<>(manager.activeMap().keySet())) {
                Entity ent = sf.bukkit().getEntity(id);
                if (ent == null || ent.isDead() || !ent.isValid() || !(ent instanceof LivingEntity living)) {
                    manager.removeActive(id);
                    continue;
                }
                SEntity def = manager.activeMap().get(id);
                if (def == null) continue;
                try {
                    def.onPerSecond(living, sf.tick().now());
                } catch (Throwable t) {
                    sf.error("[Entity] onPerSecond error: " + def.id(), t);
                }
            }
        }, 20L, bukkitSecond);
    }

    
    public void startTick(JavaPlugin plugin, SF sf) {
        startTick(plugin, sf, 5L, 100L);
    }

    public void shutdown() {
        if (tickTask != null) tickTask.cancel();
        if (perSecondTask != null) perSecondTask.cancel();
    }
}
