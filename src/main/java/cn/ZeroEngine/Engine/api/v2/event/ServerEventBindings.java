package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.server.*;

import java.util.function.Consumer;

public final class ServerEventBindings {

    private final EventRegistrar reg;

    ServerEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public ServerEventBindings command(Consumer<ServerCommandEvent> h) {
        reg.register(ServerCommandEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings remoteCommand(Consumer<RemoteServerCommandEvent> h) {
        reg.register(RemoteServerCommandEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings tabComplete(Consumer<TabCompleteEvent> h) {
        reg.register(TabCompleteEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings load(Consumer<ServerLoadEvent> h) {
        reg.register(ServerLoadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings pluginEnable(Consumer<PluginEnableEvent> h) {
        reg.register(PluginEnableEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings pluginDisable(Consumer<PluginDisableEvent> h) {
        reg.register(PluginDisableEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings serviceRegister(Consumer<ServiceRegisterEvent> h) {
        reg.register(ServiceRegisterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public ServerEventBindings serviceUnregister(Consumer<ServiceUnregisterEvent> h) {
        reg.register(ServiceUnregisterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
