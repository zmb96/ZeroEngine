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

/**
 * 自定义生物抽象基类 —— 镜像 SItem / SEnchantment 的契约模式
 *
 * 用法：
 *   sf.entities().register(new MyMob());
 *   sf.entities().spawn("my_mob", location);
 *
 * 识别靠 PDC：key = "sf_entity_id"，value = id()
 */
public abstract class SEntity {

    private static Plugin plugin;

    public static void init(Plugin p) {
        plugin = p;
        SFAttr.ensureLoaded();
    }

    // ==================== 必须实现 ====================

    /** 唯一标识符，用于命令、PDC 存储、查表 */
    public abstract String id();

    /** 显示名（实体的 customName） */
    public abstract String displayName();

    /** 基础材质 —— EntityType（如 ZOMBIE / SKELETON / WITHER_SKELETON ...） */
    public abstract EntityType entityType();

    // ==================== 属性（带默认值，可重写） ====================

    public double maxHealth()            { return 20.0; }
    public double attackDamage()         { return 2.0; }
    public double attackSpeed()          { return 4.0; }
    public double movementSpeed()        { return 0.3; }
    public double knockbackResistance()  { return 0.0; }
    public double armor()                { return 0.0; }
    public double armorToughness()       { return 0.0; }
    public double followRange()         { return 16.0; }
    public double flyingSpeed()         { return 0.4; }

    /** 应用全部属性到实体（在生成后调用） */
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

    // ==================== 阵营 ====================

    /** 敌对 / 中立 / 和平 */
    public Hostility hostility() { return Hostility.HOSTILE; }

    // ==================== 生成条件 ====================

    public SpawnCondition spawnCondition() { return new SpawnCondition(); }

    // ==================== 装备与掉落 ====================

    /** 生成时身上有几率带的物品或盔甲 */
    public List<EquipmentEntry> equipment() { return Collections.emptyList(); }

    /** 死亡时额外掉落（除了装备里的） */
    public List<ItemStack> deathDrops() { return Collections.emptyList(); }

    // ==================== 事件钩子 ====================

    public void onSpawn(LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {}
    public void onDeath(LivingEntity entity, EntityDeathEvent event) {}
    public void onAttack(LivingEntity attacker, LivingEntity target, double damage, EntityDamageByEntityEvent event) {}
    public void onDamaged(LivingEntity entity, EntityDamageEvent event) {}
    public void onTarget(EntityTargetEvent event) {}

    /**
     * 每 SF Tick 调用一次（实际通过 TickManager 调度，可能合批到主线程）。
     * 在这里写生物的 AI / 周期性效果（如再生、粒子、追踪等）。
     */
    public void onTick(LivingEntity entity, long sfTick) {}

    /** 每秒调用一次（= 100 SFTick），方便做不需要那么高频的逻辑 */
    public void onPerSecond(LivingEntity entity, long sfTick) {}

    // ==================== PDC 标识 ====================

    private NamespacedKey dataKey() {
        return new NamespacedKey(plugin, "sf_entity_id");
    }

    /** 给生成的实体打上自定义生物 PDC 标签 */
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

    // ==================== 内部类 ====================

    /**
     * 生成条件
     */
    public static class SpawnCondition {
        /** 生成几率 0.0~1.0（默认 1.0=100%） */
        public double chance = 1.0;
        /** 允许生成的世界名（null/空=所有世界） */
        public Set<String> worlds = new HashSet<>();
        /** 允许的生物群系（null/空=所有群系） */
        public Set<org.bukkit.block.Biome> biomes = new HashSet<>();
        public int minY = -64;
        public int maxY = 320;
        /** 生成所需的最低光照（实际光照 >= minLight 才生成） */
        public int minLight = 0;
        /** 生成所需的最高光照（实际光照 <= maxLight 才生成） */
        public int maxLight = 15;
        /** 怕光照 —— 白天太阳下燃烧（如僵尸/骷髅） */
        public boolean burnInDaylight = false;
        /** 只在夜晚生成（世界时间 >= 13000） */
        public boolean onlyAtNight = false;
        /** 每区块最大数量 */
        public int spawnLimitPerChunk = 4;
        /** 是否替换原版同类型生物生成（false=只在主动 spawn 命令/调度时生成） */
        public boolean replaceVanillaSpawns = false;

        public SpawnCondition chance(double c) { this.chance = c; return this; }
        public SpawnCondition world(String w) { this.worlds.add(w); return this; }
        public SpawnCondition biome(org.bukkit.block.Biome b) { this.biomes.add(b); return this; }
        public SpawnCondition light(int min, int max) { this.minLight = min; this.maxLight = max; return this; }
        public SpawnCondition burnInDay() { this.burnInDaylight = true; return this; }
        public SpawnCondition nightOnly() { this.onlyAtNight = true; return this; }

        /** 校验某位置是否满足生成条件 */
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

    /**
     * 装备条目 —— 生成时有几率给生物穿某件物品
     */
    public static class EquipmentEntry {
        public final ItemStack item;
        /** 掉落几率 0.0~1.0（生成时穿戴的几率） */
        public final double chance;
        public final EquipmentSlot slot;
        /** 死亡时是否掉落 */
        public final boolean dropOnDeath;
        /** 死亡时掉落的几率（0=不掉，1=必掉） */
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

        /** 应用到实体装备栏（按 chance 概率穿戴） */
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
