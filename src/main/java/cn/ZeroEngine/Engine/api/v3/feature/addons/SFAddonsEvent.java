package cn.ZeroEngine.Engine.api.v3.feature.addons;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SFAddonsEvent extends Event {

    public static final String UNLOAD = "unload";
    public static final String LOAD = "load";
    public static final String RELOAD = "reload";

    private final String action;
    private static final HandlerList handlers = new HandlerList();

    public SFAddonsEvent(String action) {
        this.action = action;
    }

    public String getAction() { return action; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
