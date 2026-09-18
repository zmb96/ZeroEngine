package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.function.Consumer;

public final class WeatherEventBindings {

    private final EventRegistrar reg;

    WeatherEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public WeatherEventBindings weather(Consumer<WeatherChangeEvent> h) {
        reg.register(WeatherChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WeatherEventBindings thunder(Consumer<ThunderChangeEvent> h) {
        reg.register(ThunderChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WeatherEventBindings lightningStrike(Consumer<LightningStrikeEvent> h) {
        reg.register(LightningStrikeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
