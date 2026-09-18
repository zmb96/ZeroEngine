package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SFEvents implements EventRegistrar {

    private final JavaPlugin plugin;
    private final List<Listener> listeners = new ArrayList<>();

    private final PlayerEventBindings player;
    private final BlockEventBindings block;
    private final EntityEventBindings entity;
    private final InventoryEventBindings inventory;
    private final ServerEventBindings server;
    private final WorldEventBindings world;
    private final WeatherEventBindings weather;
    private final VehicleEventBindings vehicle;
    private final HangingEventBindings hanging;
    private final RaidEventBindings raid;
    private final PaperEventBindings paper;

    public SFEvents(JavaPlugin plugin) {
        this.plugin = plugin;
        this.player = new PlayerEventBindings(this);
        this.block = new BlockEventBindings(this);
        this.entity = new EntityEventBindings(this);
        this.inventory = new InventoryEventBindings(this);
        this.server = new ServerEventBindings(this);
        this.world = new WorldEventBindings(this);
        this.weather = new WeatherEventBindings(this);
        this.vehicle = new VehicleEventBindings(this);
        this.hanging = new HangingEventBindings(this);
        this.raid = new RaidEventBindings(this);
        this.paper = new PaperEventBindings(this);
    }

    @Override
    public <T extends Event> void register(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler) {
        Listener listener = new Listener() {};
        plugin.getServer().getPluginManager().registerEvent(
                eventClass, listener, priority,
                (l, e) -> {
                    try {
                        handler.accept((T) e);
                    } catch (Throwable t) {
                        SF sf = SF.sf();
                        sf.error("Event handler error: " + eventClass.getSimpleName(), t);
                    }
                },
                plugin, ignoreCancelled
        );
        listeners.add(listener);
    }

    public <T extends Event> SFEvents on(Class<T> eventClass, Consumer<T> handler) {
        register(eventClass, EventPriority.NORMAL, false, handler);
        return this;
    }

    public <T extends Event> SFEvents on(Class<T> eventClass, EventPriority priority, Consumer<T> handler) {
        register(eventClass, priority, false, handler);
        return this;
    }

    public <T extends Event> SFEvents on(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler) {
        register(eventClass, priority, ignoreCancelled, handler);
        return this;
    }

    public PlayerEventBindings player() {
        return player;
    }

    public BlockEventBindings block() {
        return block;
    }

    public EntityEventBindings entity() {
        return entity;
    }

    public InventoryEventBindings inventory() {
        return inventory;
    }

    public ServerEventBindings server() {
        return server;
    }

    public WorldEventBindings world() {
        return world;
    }

    public WeatherEventBindings weather() {
        return weather;
    }

    public VehicleEventBindings vehicle() {
        return vehicle;
    }

    public HangingEventBindings hanging() {
        return hanging;
    }

    public RaidEventBindings raid() {
        return raid;
    }

    public PaperEventBindings paper() {
        return paper;
    }

    public void unregisterAll() {
        for (Listener l : listeners) {
            HandlerList.unregisterAll(l);
        }
        listeners.clear();
    }

    public int count() {
        return listeners.size();
    }
}
