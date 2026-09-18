package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventRegistrar {
    <T extends Event> void register(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler);
}
