package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import java.util.function.Consumer;

public final class HangingEventBindings {

    private final EventRegistrar reg;

    HangingEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public HangingEventBindings place(Consumer<HangingPlaceEvent> h) {
        reg.register(HangingPlaceEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public HangingEventBindings break_(Consumer<HangingBreakEvent> h) {
        reg.register(HangingBreakEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
