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

/**
 * 暗影猎手 —— SEntity 示例实现，演示全部特性：
 *
 *  - 血量 40 / 攻击 6 / 速度 / 护甲 / 击退抗性
 *  - 阵营：HOSTILE（主动追玩家）
 *  - 生成条件：夜晚 + 光照 0~7 + 怕光照（白天燃烧）+ 20% 几率
 *  - 基础材质：HUSK（尸壳）
 *  - 装备：50% 概率带铁剑、30% 概率带铁头盔，死亡时有几率掉落
 *  - onAttack：攻击玩家时附带中毒 III
 *  - onSpawn：生成时音效 + 粒子爆裂
 *  - onDeath：死亡掉凋零玫瑰 + 灵魂粒子
 *  - onTick：每 20 SFTick 拖尾暗影粒子
 *  - onPerSecond：1% 几率回 1 血
 */
public class ShadowStalkerEntity extends SEntity {

    // ==================== 基础信息 ====================

    @Override
    public String id() { return "shadow_stalker"; }

    @Override
    public String displayName() { return "§5暗影猎手"; }

    @Override
    public EntityType entityType() { return EntityType.HUSK; }

    // ==================== 属性 ====================

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

    // ==================== 阵营 ====================

    @Override
    public Hostility hostility() { return Hostility.HOSTILE; }

    // ==================== 生成条件 ====================

    @Override
    public SpawnCondition spawnCondition() {
        return new SpawnCondition()
                .chance(0.2)            // 20% 几率
                .nightOnly()            // 仅夜晚
                .burnInDay()            // 白天燃烧
                .light(0, 7);           // 仅在光照 0~7 的位置生成
    }

    // ==================== 装备与掉落 ====================

    @Override
    public List<EquipmentEntry> equipment() {
        return Arrays.asList(
                // 50% 几率拿铁剑，死亡 5% 几率掉
                new EquipmentEntry(
                        new ItemStack(Material.IRON_SWORD),
                        0.5,
                        EquipmentSlot.HAND,
                        true,
                        0.05
                ),
                // 30% 几率戴铁头盔，死亡 10% 几率掉
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
        // 死亡必掉 1 朵凋零玫瑰
        return Collections.singletonList(new ItemStack(Material.WITHER_ROSE, 1));
    }

    // ==================== 事件钩子 ====================

    /** 生成时：低沉咆哮 + 紫色粒子爆裂 */
    @Override
    public void onSpawn(LivingEntity entity, Location loc, CreatureSpawnEvent.SpawnReason reason) {
        entity.getWorld().playSound(loc, Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.5f, 0.5f);
        entity.getWorld().spawnParticle(Particle.PORTAL,
                loc.clone().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.2);
    }

    /** 攻击玩家时：附带中毒 III 4秒 */
    @Override
    public void onAttack(LivingEntity attacker, LivingEntity target, double damage, EntityDamageByEntityEvent event) {
        if (!(target instanceof Player p)) return;
        try {
            // 1.20.5+ 用 Registry 取药水效果类型
            var pe = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("poison"));
            if (pe != null) {
                p.addPotionEffect(new PotionEffect(pe, 80, 2, false, true, true));
            }
            p.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                    p.getLocation().add(0, 1, 0), 15, 0.3, 0.6, 0.3, 0.05);
        } catch (Throwable t) {
            // Registry 取不到时静默
        }
    }

    /** 受伤时：黑色粒子溅射 */
    @Override
    public void onDamaged(LivingEntity entity, EntityDamageEvent event) {
        entity.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                entity.getLocation().add(0, 1, 0), 8, 0.3, 0.6, 0.3, 0.05);
    }

    /** 死亡时：灵魂粒子 + 爆炸音效 */
    @Override
    public void onDeath(LivingEntity entity, EntityDeathEvent event) {
        Location loc = entity.getLocation();
        entity.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.7f);
        entity.getWorld().spawnParticle(Particle.SOUL,
                loc.clone().add(0, 1, 0), 25, 0.5, 0.8, 0.5, 0.1);
    }

    /** 目标事件：直接放行（HOSTILE 让原版逻辑跑） */
    @Override
    public void onTarget(EntityTargetEvent event) {
        // 基类的阵营逻辑已经在 EntityListener 里处理了，这里不做额外干预
    }

    /**
     * 每 SF Tick 调用（实际每 5 SFTick = 1 Bukkit tick）
     * 每 20 SFTick 拖一次暗影粒子（约 4 次/秒）
     */
    @Override
    public void onTick(LivingEntity entity, long sfTick) {
        if (sfTick % 20 != 0) return;
        Location loc = entity.getLocation().add(0, 1.2, 0);
        entity.getWorld().spawnParticle(Particle.DUST,
                loc, 5, 0.3, 0.5, 0.3, 0.01,
                new Particle.DustOptions(Color.fromRGB(80, 0, 100), 1.2f));
    }

    /**
     * 每秒调用一次（= 100 SFTick）
     * 1% 几率回 1 血（不超过最大值）
     */
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
