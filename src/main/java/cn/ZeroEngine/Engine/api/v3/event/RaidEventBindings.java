package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

import java.util.function.Consumer;

public final class RaidEventBindings {

    private final EventRegistrar reg;

    RaidEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public RaidEventBindings trigger(Consumer<RaidTriggerEvent> h) {
        reg.register(RaidTriggerEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public RaidEventBindings finish(Consumer<RaidFinishEvent> h) {
        reg.register(RaidFinishEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public RaidEventBindings stop(Consumer<RaidStopEvent> h) {
        reg.register(RaidStopEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public RaidEventBindings spawnWave(Consumer<RaidSpawnWaveEvent> h) {
        reg.register(RaidSpawnWaveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
