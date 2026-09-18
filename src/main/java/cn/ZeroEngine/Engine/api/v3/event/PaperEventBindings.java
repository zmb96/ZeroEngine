package cn.ZeroEngine.Engine.api.v3.event;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;

public final class PaperEventBindings {

    private final EventRegistrar reg;

    PaperEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public PaperEventBindings asyncChat(Consumer<AsyncChatEvent> h) {
        reg.register(AsyncChatEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PaperEventBindings chatDecorate(Consumer<AsyncChatDecorateEvent> h) {
        reg.register(AsyncChatDecorateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PaperEventBindings signOpen(Consumer<PlayerOpenSignEvent> h) {
        reg.register(PlayerOpenSignEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
