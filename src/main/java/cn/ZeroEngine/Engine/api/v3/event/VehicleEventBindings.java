package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.*;

import java.util.function.Consumer;

public final class VehicleEventBindings {

    private final EventRegistrar reg;

    VehicleEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public VehicleEventBindings create(Consumer<VehicleCreateEvent> h) {
        reg.register(VehicleCreateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings destroy(Consumer<VehicleDestroyEvent> h) {
        reg.register(VehicleDestroyEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings damage(Consumer<VehicleDamageEvent> h) {
        reg.register(VehicleDamageEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings enter(Consumer<VehicleEnterEvent> h) {
        reg.register(VehicleEnterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings exit(Consumer<VehicleExitEvent> h) {
        reg.register(VehicleExitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings move(Consumer<VehicleMoveEvent> h) {
        reg.register(VehicleMoveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings update(Consumer<VehicleUpdateEvent> h) {
        reg.register(VehicleUpdateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings collision(Consumer<VehicleCollisionEvent> h) {
        reg.register(VehicleCollisionEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public VehicleEventBindings entityCollision(Consumer<VehicleEntityCollisionEvent> h) {
        reg.register(VehicleEntityCollisionEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
