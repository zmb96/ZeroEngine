package cn.ZeroEngine.Engine.api.v3.feature.entity;

public enum Hostility {

    
    HOSTILE,

    
    NEUTRAL,

    
    PASSIVE;

    public boolean isHostile() { return this == HOSTILE; }
    public boolean isNeutral() { return this == NEUTRAL; }
    public boolean isPassive() { return this == PASSIVE; }
}
