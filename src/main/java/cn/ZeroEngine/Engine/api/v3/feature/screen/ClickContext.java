package cn.ZeroEngine.Engine.api.v3.feature.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import io.papermc.paper.connection.PlayerConfigurationConnection;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 按钮点击上下文。SScreen.onClick(ctx) 里调用 ctx.accept() 放行，ctx.deny(...) 踢出。
 * 多次调用只生效第一次。
 */
public class ClickContext {

    private final String action;
    private final PlayerConfigurationConnection connection;
    private final CompletableFuture<Boolean> future;
    private final AtomicBoolean resolved = new AtomicBoolean(false);

    public ClickContext(String action, PlayerConfigurationConnection connection, CompletableFuture<Boolean> future) {
        this.action = action;
        this.connection = connection;
        this.future = future;
    }

    public String action() { return action; }

    public PlayerConfigurationConnection connection() { return connection; }

    public UUID playerId() {
        try { return connection.getProfile().getId(); } catch (Throwable ignore) { return null; }
    }

    public boolean isResolved() { return resolved.get(); }

    public void accept() {
        if (resolved.compareAndSet(false, true) && future != null) {
            future.complete(true);
        }
    }

    public void deny() {
        deny(Component.text("你已被拒绝进入服务器").color(NamedTextColor.RED));
    }

    public void deny(Component kickMessage) {
        if (!resolved.compareAndSet(false, true)) return;
        try {
            if (connection != null && kickMessage != null) {
                connection.disconnect(kickMessage);
            }
        } catch (Throwable ignore) {}
        if (future != null) future.complete(false);
    }
}
