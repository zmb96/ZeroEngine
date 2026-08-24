package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SFAttr {

    // ===================== 玩家通用属性 =====================
    public static final String MAX_HEALTH                    = "MAX_HEALTH";
    public static final String FOLLOW_RANGE                  = "FOLLOW_RANGE";
    public static final String KNOCKBACK_RESISTANCE          = "KNOCKBACK_RESISTANCE";
    public static final String MOVEMENT_SPEED                = "MOVEMENT_SPEED";
    public static final String FLYING_SPEED                  = "FLYING_SPEED";
    public static final String ATTACK_DAMAGE                 = "ATTACK_DAMAGE";
    public static final String ATTACK_KNOCKBACK              = "ATTACK_KNOCKBACK";
    public static final String ATTACK_SPEED                  = "ATTACK_SPEED";
    public static final String ARMOR                         = "ARMOR";
    public static final String ARMOR_TOUGHNESS               = "ARMOR_TOUGHNESS";
    public static final String FALL_DAMAGE_MULTIPLIER        = "FALL_DAMAGE_MULTIPLIER";
    public static final String LUCK                          = "LUCK";
    public static final String MAX_ABSORPTION                = "MAX_ABSORPTION";
    public static final String BLOCK_INTERACTION_RANGE       = "BLOCK_INTERACTION_RANGE";
    public static final String ENTITY_INTERACTION_RANGE      = "ENTITY_INTERACTION_RANGE";
    public static final String GRAVITY                       = "GRAVITY";
    public static final String SAFE_FALL_DISTANCE            = "SAFE_FALL_DISTANCE";
    public static final String BURNING_TIME                  = "BURNING_TIME";
    public static final String MOVEMENT_EFFICIENCY           = "MOVEMENT_EFFICIENCY";
    public static final String OXYGEN_BONUS                  = "OXYGEN_BONUS";
    public static final String WATER_MOVEMENT_EFFICIENCY     = "WATER_MOVEMENT_EFFICIENCY";
    public static final String ATTACK_TIME                   = "ATTACK_TIME";
    public static final String MINING_EFFICIENCY             = "MINING_EFFICIENCY";
    public static final String SNEAKING_SPEED                = "SNEAKING_SPEED";
    public static final String SUBMERGED_MINING_SPEED        = "SUBMERGED_MINING_SPEED";
    public static final String SWEEPING_DAMAGE_RATIO         = "SWEEPING_DAMAGE_RATIO";
    public static final String TEMPT_RANGE                   = "TEMPT_RANGE";
    public static final String SCALE                         = "SCALE";
    public static final String STEP_HEIGHT                   = "STEP_HEIGHT";
    public static final String EXPLOSION_KNOCKBACK_REDUCTION = "EXPLOSION_KNOCKBACK_REDUCTION";
    public static final String EXPLOSION_KNOCKBACK_RESISTANCE = "EXPLOSION_KNOCKBACK_RESISTANCE"; // 1.21+ 正确名（REDUCTION 为旧错名兼容保留）
    public static final String BLOCK_BREAK_SPEED              = "BLOCK_BREAK_SPEED";
    public static final String JUMP_STRENGTH                   = "JUMP_STRENGTH";

    // ===================== 生物专属属性 =====================
    public static final String SPAWN_REINFORCEMENTS           = "SPAWN_REINFORCEMENTS";

    // ===================== 旧版 GENERIC_ 兼容名 =====================
    public static final String GENERIC_MAX_HEALTH                    = "GENERIC_MAX_HEALTH";
    public static final String GENERIC_FOLLOW_RANGE                  = "GENERIC_FOLLOW_RANGE";
    public static final String GENERIC_KNOCKBACK_RESISTANCE          = "GENERIC_KNOCKBACK_RESISTANCE";
    public static final String GENERIC_MOVEMENT_SPEED                = "GENERIC_MOVEMENT_SPEED";
    public static final String GENERIC_FLYING_SPEED                  = "GENERIC_FLYING_SPEED";
    public static final String GENERIC_ATTACK_DAMAGE                 = "GENERIC_ATTACK_DAMAGE";
    public static final String GENERIC_ATTACK_KNOCKBACK              = "GENERIC_ATTACK_KNOCKBACK";
    public static final String GENERIC_ATTACK_SPEED                  = "GENERIC_ATTACK_SPEED";
    public static final String GENERIC_ARMOR                         = "GENERIC_ARMOR";
    public static final String GENERIC_ARMOR_TOUGHNESS               = "GENERIC_ARMOR_TOUGHNESS";
    public static final String GENERIC_FALL_DAMAGE_MULTIPLIER        = "GENERIC_FALL_DAMAGE_MULTIPLIER";
    public static final String GENERIC_LUCK                          = "GENERIC_LUCK";
    public static final String GENERIC_MAX_ABSORPTION                = "GENERIC_MAX_ABSORPTION";
    public static final String GENERIC_BLOCK_INTERACTION_RANGE       = "GENERIC_BLOCK_INTERACTION_RANGE";
    public static final String GENERIC_ENTITY_INTERACTION_RANGE      = "GENERIC_ENTITY_INTERACTION_RANGE";
    public static final String GENERIC_GRAVITY                       = "GENERIC_GRAVITY";
    public static final String GENERIC_SAFE_FALL_DISTANCE            = "GENERIC_SAFE_FALL_DISTANCE";
    public static final String GENERIC_BURNING_TIME                  = "GENERIC_BURNING_TIME";
    public static final String GENERIC_MOVEMENT_EFFICIENCY           = "GENERIC_MOVEMENT_EFFICIENCY";
    public static final String GENERIC_OXYGEN_BONUS                  = "GENERIC_OXYGEN_BONUS";
    public static final String GENERIC_WATER_MOVEMENT_EFFICIENCY     = "GENERIC_WATER_MOVEMENT_EFFICIENCY";
    public static final String GENERIC_ATTACK_TIME                   = "GENERIC_ATTACK_TIME";
    public static final String GENERIC_MINING_EFFICIENCY             = "GENERIC_MINING_EFFICIENCY";
    public static final String GENERIC_SNEAKING_SPEED                = "GENERIC_SNEAKING_SPEED";
    public static final String GENERIC_SUBMERGED_MINING_SPEED        = "GENERIC_SUBMERGED_MINING_SPEED";
    public static final String GENERIC_SWEEPING_DAMAGE_RATIO         = "GENERIC_SWEEPING_DAMAGE_RATIO";
    public static final String GENERIC_TEMPT_RANGE                   = "GENERIC_TEMPT_RANGE";
    public static final String GENERIC_SCALE                         = "GENERIC_SCALE";
    public static final String GENERIC_STEP_HEIGHT                   = "GENERIC_STEP_HEIGHT";
    public static final String GENERIC_EXPLOSION_KNOCKBACK_REDUCTION = "GENERIC_EXPLOSION_KNOCKBACK_REDUCTION";
    public static final String GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE = "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE";
    public static final String GENERIC_BLOCK_BREAK_SPEED              = "GENERIC_BLOCK_BREAK_SPEED";
    public static final String GENERIC_JUMP_STRENGTH                   = "GENERIC_JUMP_STRENGTH";
    public static final String ZOMBIE_SPAWN_REINFORCEMENTS           = "ZOMBIE_SPAWN_REINFORCEMENTS";

    private static final Map<String, Attribute> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, String> DISPLAY = new LinkedHashMap<>();
    private static boolean init = false;
    public static final SFAttr INSTANCE = new SFAttr();

    static {
        DISPLAY.put(MAX_HEALTH,                    "最大生命");
        DISPLAY.put(FOLLOW_RANGE,                  "追踪范围");
        DISPLAY.put(KNOCKBACK_RESISTANCE,          "击退抗性");
        DISPLAY.put(MOVEMENT_SPEED,                "移动速度");
        DISPLAY.put(FLYING_SPEED,                  "飞行速度");
        DISPLAY.put(ATTACK_DAMAGE,                 "攻击伤害");
        DISPLAY.put(ATTACK_KNOCKBACK,              "攻击击退");
        DISPLAY.put(ATTACK_SPEED,                  "攻击速度");
        DISPLAY.put(ARMOR,                         "护甲");
        DISPLAY.put(ARMOR_TOUGHNESS,               "护甲韧性");
        DISPLAY.put(FALL_DAMAGE_MULTIPLIER,        "坠落伤害倍率");
        DISPLAY.put(LUCK,                          "幸运");
        DISPLAY.put(MAX_ABSORPTION,                "最大吸收值");
        DISPLAY.put(BLOCK_INTERACTION_RANGE,       "方块交互距离");
        DISPLAY.put(ENTITY_INTERACTION_RANGE,      "实体交互距离");
        DISPLAY.put(GRAVITY,                       "重力");
        DISPLAY.put(SAFE_FALL_DISTANCE,            "安全坠落距离");
        DISPLAY.put(BURNING_TIME,                  "燃烧时间");
        DISPLAY.put(MOVEMENT_EFFICIENCY,           "移动效率");
        DISPLAY.put(OXYGEN_BONUS,                  "氧气加成");
        DISPLAY.put(WATER_MOVEMENT_EFFICIENCY,     "水中移动效率");
        DISPLAY.put(ATTACK_TIME,                   "攻击冷却");
        DISPLAY.put(MINING_EFFICIENCY,             "挖掘效率");
        DISPLAY.put(SNEAKING_SPEED,                "潜行速度");
        DISPLAY.put(SUBMERGED_MINING_SPEED,        "水下挖掘速度");
        DISPLAY.put(SWEEPING_DAMAGE_RATIO,         "横扫伤害比率");
        DISPLAY.put(TEMPT_RANGE,                   "吸引范围");
        DISPLAY.put(SCALE,                         "实体缩放");
        DISPLAY.put(STEP_HEIGHT,                   "台阶高度");
        DISPLAY.put(EXPLOSION_KNOCKBACK_REDUCTION, "爆炸击退减免");
        DISPLAY.put(EXPLOSION_KNOCKBACK_RESISTANCE, "爆炸击退抗性");
        DISPLAY.put(BLOCK_BREAK_SPEED,              "方块破坏速度");
        DISPLAY.put(JUMP_STRENGTH,                  "跳跃强度");
        DISPLAY.put(SPAWN_REINFORCEMENTS,          "僵尸增援率");
    }

    public static synchronized void ensureLoaded() {
        if (init) return;
        try {
            Attribute[] values = Attribute.values();
            for (Attribute a : values) REGISTRY.put(a.name(), a);
            SF sf = SF.sf();
            if (sf != null) {
                sf.info("[SFAttr] Loaded " + REGISTRY.size() + " Bukkit attributes");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        init = true;
    }

    public static Attribute get(String name) {
        ensureLoaded();
        if (name == null) return null;
        return REGISTRY.get(name.toUpperCase());
    }

    public static boolean exists(String name) {
        ensureLoaded();
        if (name == null) return false;
        return REGISTRY.containsKey(name.toUpperCase());
    }

    public static Set<String> allNames() {
        ensureLoaded();
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    public static Collection<Attribute> all() {
        ensureLoaded();
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static int count() {
        ensureLoaded();
        return REGISTRY.size();
    }

    public static String display(String name) {
        if (name == null) return "";
        String core = name.toUpperCase().replaceAll("^(GENERIC_|PLAYER_|ZOMBIE_)", "");
        String d = DISPLAY.get(core);
        return d != null ? d : name;
    }

    // ==================== AttributeBonus 快捷构造（仅实例方法，支持 sf().attr().xxx()） ====================

    public SEnchantment.AttributeBonus add(String name, String attr, double base, double perLevel) {
        return SEnchantment.AttributeBonus.add(name, attr, base, perLevel);
    }

    public SEnchantment.AttributeBonus add(String name, String attr, double base, double perLevel, AttributeModifier.Operation op, String slot) {
        return new SEnchantment.AttributeBonus(name, attr, base, perLevel, op, slot);
    }

    public SEnchantment.AttributeBonus multiply(String name, String attr, double base, double perLevel) {
        return SEnchantment.AttributeBonus.multiply(name, attr, base, perLevel);
    }

    public SEnchantment.AttributeBonus maxHealth(double base, double perLevel) {
        return add("最大生命", MAX_HEALTH, base, perLevel);
    }
    public SEnchantment.AttributeBonus attackDamage(double base, double perLevel) {
        return add("攻击伤害", ATTACK_DAMAGE, base, perLevel);
    }
    public SEnchantment.AttributeBonus attackSpeed(double base, double perLevel) {
        return add("攻击速度", ATTACK_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus attackKnockback(double base, double perLevel) {
        return add("攻击击退", ATTACK_KNOCKBACK, base, perLevel);
    }
    public SEnchantment.AttributeBonus movementSpeed(double base, double perLevel) {
        return add("移动速度", MOVEMENT_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus flyingSpeed(double base, double perLevel) {
        return add("飞行速度", FLYING_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus knockbackResistance(double base, double perLevel) {
        return add("击退抗性", KNOCKBACK_RESISTANCE, base, perLevel);
    }
    public SEnchantment.AttributeBonus armor(double base, double perLevel) {
        return add("护甲", ARMOR, base, perLevel);
    }
    public SEnchantment.AttributeBonus armorToughness(double base, double perLevel) {
        return add("护甲韧性", ARMOR_TOUGHNESS, base, perLevel);
    }
    public SEnchantment.AttributeBonus luck(double base, double perLevel) {
        return add("幸运", LUCK, base, perLevel);
    }
    public SEnchantment.AttributeBonus maxAbsorption(double base, double perLevel) {
        return add("最大吸收", MAX_ABSORPTION, base, perLevel);
    }
    public SEnchantment.AttributeBonus blockRange(double base, double perLevel) {
        return add("方块距离", BLOCK_INTERACTION_RANGE, base, perLevel);
    }
    public SEnchantment.AttributeBonus entityRange(double base, double perLevel) {
        return add("实体距离", ENTITY_INTERACTION_RANGE, base, perLevel);
    }
    public SEnchantment.AttributeBonus followRange(double base, double perLevel) {
        return add("追踪范围", FOLLOW_RANGE, base, perLevel);
    }
    public SEnchantment.AttributeBonus fallDamageMul(double base, double perLevel) {
        return multiply("坠落伤害", FALL_DAMAGE_MULTIPLIER, base, perLevel);
    }
    public SEnchantment.AttributeBonus gravity(double base, double perLevel) {
        return add("重力", GRAVITY, base, perLevel);
    }
    public SEnchantment.AttributeBonus safeFallDistance(double base, double perLevel) {
        return add("安全坠落", SAFE_FALL_DISTANCE, base, perLevel);
    }
    public SEnchantment.AttributeBonus scale(double base, double perLevel) {
        return add("缩放", SCALE, base, perLevel);
    }
    public SEnchantment.AttributeBonus stepHeight(double base, double perLevel) {
        return add("台阶高度", STEP_HEIGHT, base, perLevel);
    }
    public SEnchantment.AttributeBonus miningEfficiency(double base, double perLevel) {
        return add("挖掘效率", MINING_EFFICIENCY, base, perLevel);
    }
    public SEnchantment.AttributeBonus sweepingDamage(double base, double perLevel) {
        return add("横扫伤害", SWEEPING_DAMAGE_RATIO, base, perLevel);
    }
    public SEnchantment.AttributeBonus sneakSpeed(double base, double perLevel) {
        return add("潜行速度", SNEAKING_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus submergedMining(double base, double perLevel) {
        return add("水下挖掘", SUBMERGED_MINING_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus waterMoveEff(double base, double perLevel) {
        return add("水中移效", WATER_MOVEMENT_EFFICIENCY, base, perLevel);
    }
    public SEnchantment.AttributeBonus oxygenBonus(double base, double perLevel) {
        return add("氧气加成", OXYGEN_BONUS, base, perLevel);
    }
    public SEnchantment.AttributeBonus moveEfficiency(double base, double perLevel) {
        return add("移动效率", MOVEMENT_EFFICIENCY, base, perLevel);
    }
    public SEnchantment.AttributeBonus burningTime(double base, double perLevel) {
        return add("燃烧时间", BURNING_TIME, base, perLevel);
    }
    public SEnchantment.AttributeBonus attackTime(double base, double perLevel) {
        return add("攻击冷却", ATTACK_TIME, base, perLevel);
    }
    public SEnchantment.AttributeBonus temptRange(double base, double perLevel) {
        return add("吸引范围", TEMPT_RANGE, base, perLevel);
    }
    public SEnchantment.AttributeBonus explosionKnockbackReduction(double base, double perLevel) {
        return add("爆炸击退减免", EXPLOSION_KNOCKBACK_REDUCTION, base, perLevel);
    }
    public SEnchantment.AttributeBonus explosionKnockbackResistance(double base, double perLevel) {
        return add("爆炸击退抗性", EXPLOSION_KNOCKBACK_RESISTANCE, base, perLevel);
    }
    public SEnchantment.AttributeBonus blockBreakSpeed(double base, double perLevel) {
        return add("方块破坏速度", BLOCK_BREAK_SPEED, base, perLevel);
    }
    public SEnchantment.AttributeBonus jumpStrength(double base, double perLevel) {
        return add("跳跃强度", JUMP_STRENGTH, base, perLevel);
    }
    public SEnchantment.AttributeBonus spawnReinforcements(double base, double perLevel) {
        return add("僵尸增援率", SPAWN_REINFORCEMENTS, base, perLevel);
    }

    private SFAttr() {}
}
