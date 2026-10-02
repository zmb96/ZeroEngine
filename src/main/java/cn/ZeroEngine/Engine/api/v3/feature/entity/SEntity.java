package cn.ZeroEngine.Engine.api.v3.feature.entity;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.SFAttr;

import java.util.*;

public abstract class SEntity {

    private static Plugin plugin;

    public static void init(Plugin p) {
        plugin = p;
        SFAttr.ensureLoaded();
    }

    

    
    public abstract String id();

    
    public abstract String displayName();

    
    public abstract EntityType entityType();

    

    public double maxHealth()            { return 20.0; }
    public double attackDamage()         { return 2.0; }
    public double attackSpeed()          { return 4.0; }
    public double movementSpeed()        { return 0.3; }
    public double knockbackResistance()  { return 0.0; }
    public double armor()                { return 0.0; }
    public double armorToughness()       { return 0.0; }
    public double followRange()         { return 16.0; }
    public double flyingSpeed()         { return 0.4; }

    
    public void applyAttributes(LivingEntity entity) {
        set(entity, SFAttr.MAX_HEALTH, maxHealth());
        set(entity, SFAttr.ATTACK_DAMAGE, attackDamage());
        set(entity, SFAttr.ATTACK_SPEED, attackSpeed());
        set(entity, SFAttr.MOVEMENT_SPEED, movementSpeed());
        set(entity, SFAttr.KNOCKBACK_RESISTANCE, knockbackResistance());
        set(entity, SFAttr.ARMOR, armor());
        set(entity, SFAttr.ARMOR_TOUGHNESS, armorToughness());
        set(entity, SFAttr.FOLLOW_RANGE, followRange());
        set(entity, SFAttr.FLYING_SPEED, flyingSpeed());
        if (maxHealth() > 0) {
            entity.setHealth(maxHealth());
        }
    }

    private void set(LivingEntity entity, String attrName, double value) {
        try {
            Attribute attr = SFAttr.get(attrName);
            if (attr == null) return;
            AttributeInstance inst = entity.getAttribute(attr);
            if (inst == null) return;
            inst.setBaseValue(value);
        } catch (Throwable t) {
            SF.sf().warn("[Entity] set attribute failed: " + attrName + " on " + id(), t);
        }
    }

    

    
    public Hostility hostility() { return Hostility.HOSTILE; }

    

    public SpawnCondition spawnCondition() { return new SpawnCondition(); }

    

    
    public List<EquipmentEntry> equipment() { return Collections.emptyList(); }

    
    public List<ItemStack> deathDrops() { return Collections.emptyList(); }

    

    public void onSpawn(LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {}
    public void onDeath(LivingEntity entity, EntityDeathEvent event) {}
    public void onAttack(LivingEntity attacker, LivingEntity target, double damage, EntityDamageByEntityEvent event) {}
    public void onDamaged(LivingEntity entity, EntityDamageEvent event) {}
    public void onTarget(EntityTargetEvent event) {}

    
    public void onTick(LivingEntity entity, long sfTick) {}

    
    public void onPerSecond(LivingEntity entity, long sfTick) {}

    

    private NamespacedKey dataKey() {
        return new NamespacedKey(plugin, "sf_entity_id");
    }

    
    public void tag(LivingEntity entity) {
        if (entity == null) return;
        entity.getPersistentDataContainer().set(dataKey(), PersistentDataType.STRING, id());
        entity.setCustomName(displayName());
        entity.setCustomNameVisible(true);
    }

    public boolean is(LivingEntity entity) {
        if (entity == null) return false;
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        String val = pdc.get(dataKey(), PersistentDataType.STRING);
        return id().equals(val);
    }

    

    
    public static class SpawnCondition {
        
        public double chance = 1.0;
        
        public Set<String> worlds = new HashSet<>();
        
        public Set<org.bukkit.block.Biome> biomes = new HashSet<>();
        public int minY = -64;
        public int maxY = 320;
        
        public int minLight = 0;
        
        public int maxLight = 15;
        
        public boolean burnInDaylight = false;
        
        public boolean onlyAtNight = false;
        
        public int spawnLimitPerChunk = 4;
        
        public boolean replaceVanillaSpawns = false;

        public SpawnCondition chance(double c) { this.chance = c; return this; }
        public SpawnCondition world(String w) { this.worlds.add(w); return this; }
        public SpawnCondition biome(org.bukkit.block.Biome b) { this.biomes.add(b); return this; }
        public SpawnCondition light(int min, int max) { this.minLight = min; this.maxLight = max; return this; }
        public SpawnCondition burnInDay() { this.burnInDaylight = true; return this; }
        public SpawnCondition nightOnly() { this.onlyAtNight = true; return this; }

        
        public boolean matches(Location loc) {
            if (loc == null || loc.getWorld() == null) return false;
            if (!worlds.isEmpty() && !worlds.contains(loc.getWorld().getName())) return false;
            if (loc.getY() < minY || loc.getY() > maxY) return false;
            if (!biomes.isEmpty()) {
                org.bukkit.block.Biome b = loc.getBlock().getBiome();
                if (!biomes.contains(b)) return false;
            }
            int light = loc.getBlock().getLightLevel();
            if (light < minLight || light > maxLight) return false;
            if (onlyAtNight) {
                long time = loc.getWorld().getTime();
                if (time < 13000 && time > 23000) return false;
            }
            if (Math.random() > chance) return false;
            return true;
        }
    }

    
    public static class EquipmentEntry {
        public final ItemStack item;
        
        public final double chance;
        public final EquipmentSlot slot;
        
        public final boolean dropOnDeath;
        
        public final double dropChance;

        public EquipmentEntry(ItemStack item, double chance, EquipmentSlot slot, boolean dropOnDeath, double dropChance) {
            this.item = item;
            this.chance = chance;
            this.slot = slot;
            this.dropOnDeath = dropOnDeath;
            this.dropChance = dropChance;
        }

        public EquipmentEntry(ItemStack item, double chance, EquipmentSlot slot) {
            this(item, chance, slot, false, 0.0);
        }

        
        public void applyTo(EntityEquipment eq) {
            if (eq == null || item == null) return;
            if (Math.random() > chance) return;
            eq.setItem(slot, item.clone(), true);
            if (dropOnDeath) {
                switch (slot) {
                    case HEAD -> eq.setHelmetDropChance((float) dropChance);
                    case CHEST -> eq.setChestplateDropChance((float) dropChance);
                    case LEGS -> eq.setLeggingsDropChance((float) dropChance);
                    case FEET -> eq.setBootsDropChance((float) dropChance);
                    case HAND -> eq.setItemInMainHandDropChance((float) dropChance);
                    case OFF_HAND -> eq.setItemInOffHandDropChance((float) dropChance);
                }
            }
        }
    }
}
