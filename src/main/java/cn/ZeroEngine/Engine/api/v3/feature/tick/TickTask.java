package cn.ZeroEngine.Engine.api.v3.feature.tick;

@FunctionalInterface
public interface TickTask {
    void tick(long sfTick);
}
