package cn.ZeroEngine.Engine.api.v3.feature.entity;

/**
 * 自定义生物的敌对阵营
 */
public enum Hostility {

    /** 敌对：主动攻击玩家 */
    HOSTILE,

    /** 中立：被攻击后才反击 */
    NEUTRAL,

    /** 和平：永不攻击玩家 */
    PASSIVE;

    public boolean isHostile() { return this == HOSTILE; }
    public boolean isNeutral() { return this == NEUTRAL; }
    public boolean isPassive() { return this == PASSIVE; }
}
