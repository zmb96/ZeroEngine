package cn.ZeroEngine.Engine.api.v3.feature.entity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import cn.ZeroEngine.Engine.api.v3.feature.enchant.SFAttr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShadowStalkerEntity extends SEntity {

    

    @Override
    public String id() { return "shadow_stalker"; }

    @Override
    public String displayName() { return "§5暗影猎手"; }

    @Override
    public EntityType entityType() { return EntityType.HUSK; }

    

    @Override
    public double maxHealth()           { return 40.0; }
    @Override
    public double attackDamage()        { return 6.0; }
    @Override
    public double attackSpeed()         { return 2.0; }
    @Override
    public double movementSpeed()       { return 0.35; }
    @Override
    public double armor()               { return 4.0; }
    @Override
    public double armorToughness()      { return 2.0; }
    @Override
    public double knockbackResistance() { return 0.5; }
    @Override
    public double followRange()         { return 24.0; }

    

    @Override
    public Hostility hostility() { return Hostility.HOSTILE; }

    

    @Override
    public SpawnCondition spawnCondition() {
        return new SpawnCondition()
                .chance(0.2)            
                .nightOnly()            
                .burnInDay()            
                .light(0, 7);           
    }

    

    @Override
    public List<EquipmentEntry> equipment() {
        return Arrays.asList(
                
                new EquipmentEntry(
                        new ItemStack(Material.IRON_SWORD),
                        0.5,
                        EquipmentSlot.HAND,
                        true,
                        0.05
                ),
                
                new EquipmentEntry(
                        new ItemStack(Material.IRON_HELMET),
                        0.3,
                        EquipmentSlot.HEAD,
                        true,
                        0.10
                )
        );
    }

    @Override
    public List<ItemStack> deathDrops() {
        
        return Collections.singletonList(new ItemStack(Material.WITHER_ROSE, 1));
    }

    

    
    @Override
    public void onSpawn(LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        entity.getWorld().playSound(loc, Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.5f, 0.5f);
        entity.getWorld().spawnParticle(Particle.PORTAL,
                loc.clone().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.2);
    }

    
    @Override
    public void onAttack(LivingEntity attacker, LivingEntity target, double damage, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player p)) return;
        try {
            
            var pe = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("poison"));
            if (pe != null) {
                p.addPotionEffect(new PotionEffect(pe, 80, 2, false, true, true));
            }
            p.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                    p.getLocation().add(0, 1, 0), 15, 0.3, 0.6, 0.3, 0.05);
        } catch (Throwable t) {
            
        }
    }

    
    @Override
    public void onDamaged(LivingEntity entity, EntityDamageEvent event) {
        entity.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                entity.getLocation().add(0, 1, 0), 8, 0.3, 0.6, 0.3, 0.05);
    }

    
    @Override
    public void onDeath(LivingEntity entity, EntityDeathEvent event) {
        Location loc = entity.getLocation();
        entity.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);
        entity.getWorld().spawnParticle(Particle.SOUL,
                loc.clone().add(0, 1, 0), 25, 0.5, 0.8, 0.5, 0.1);
    }

    
    @Override
    public void onTarget(EntityTargetEvent event) {
        
    }

    
    @Override
    public void onTick(LivingEntity entity, long sfTick) {
        if (sfTick % 20 != 0) return;
        Location loc = entity.getLocation().add(0, 1.2, 0);
        entity.getWorld().spawnParticle(Particle.DUST,
                loc, 5, 0.3, 0.5, 0.3, 0.01,
                new Particle.DustOptions(Color.fromRGB(80, 0, 100), 1.2f));
    }

    
    @Override
    public void onPerSecond(LivingEntity entity, long sfTick) {
        if (Math.random() > 0.01) return;
        Attribute attr = SFAttr.get(SFAttr.MAX_HEALTH);
        if (attr == null) return;
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst == null) return;
        double max = inst.getValue();
        double cur = entity.getHealth();
        if (cur >= max) return;
        entity.setHealth(Math.min(max, cur + 1.0));
        entity.getWorld().spawnParticle(Particle.HEART,
                entity.getLocation().add(0, 2.2, 0), 3, 0.3, 0.2, 0.3, 0.0);
    }
}
