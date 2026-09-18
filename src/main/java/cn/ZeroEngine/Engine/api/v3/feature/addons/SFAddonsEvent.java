package cn.ZeroEngine.Engine.api.v3.feature.addons;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SFAddons 热加载事件。ZeroEngine 的 /sfaddons 命令触发，
 * 第三方插件监听此事件完成自身的 load / reload 注册。
 */
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
