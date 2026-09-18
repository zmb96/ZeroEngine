package cn.ZeroEngine.Engine.api.v2.feature.tick;

@FunctionalInterface
public interface TickTask {
    void tick(long sfTick);
}
