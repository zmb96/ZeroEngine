package cn.ZeroEngine.Engine.api.v3.feature.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import io.papermc.paper.connection.PlayerCommonConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClickContext {

    private final String action;
    private final PlayerCommonConnection connection;
    private final DialogResponseView response;
    private final CompletableFuture<Boolean> future;
    private final AtomicBoolean resolved = new AtomicBoolean(false);

    public ClickContext(String action, PlayerCommonConnection connection, CompletableFuture<Boolean> future) {
        this(action, connection, null, future);
    }

    public ClickContext(String action, PlayerCommonConnection connection, DialogResponseView response, CompletableFuture<Boolean> future) {
        this.action = action;
        this.connection = connection;
        this.response = response;
        this.future = future;
    }

    public String action() { return action; }

    public PlayerCommonConnection connection() { return connection; }

    public DialogResponseView response() { return response; }

    public PlayerConfigurationConnection configurationConnection() {
        return connection instanceof PlayerConfigurationConnection
                ? (PlayerConfigurationConnection) connection : null;
    }

    public PlayerGameConnection gameConnection() {
        return connection instanceof PlayerGameConnection
                ? (PlayerGameConnection) connection : null;
    }

    public Player player() {
        if (connection instanceof PlayerGameConnection gc) {
            try { return gc.getPlayer(); } catch (Throwable ignore) {}
        }
        return null;
    }

    public UUID playerId() {
        if (connection instanceof PlayerGameConnection gc) {
            try {
                Player p = gc.getPlayer();
                return p == null ? null : p.getUniqueId();
            } catch (Throwable ignore) { return null; }
        }
        if (connection instanceof PlayerConfigurationConnection cc) {
            try { return cc.getProfile().getId(); } catch (Throwable ignore) { return null; }
        }
        return null;
    }

    public String inputText(String key) {
        return response == null ? null : response.getText(key);
    }

    public Boolean inputBool(String key) {
        return response == null ? null : response.getBoolean(key);
    }

    public Float inputFloat(String key) {
        return response == null ? null : response.getFloat(key);
    }

    public boolean isResolved() { return resolved.get(); }

    public boolean isInGame() { return connection instanceof PlayerGameConnection; }

    public boolean isInConfiguration() { return connection instanceof PlayerConfigurationConnection; }

    public void accept() {
        if (!resolved.compareAndSet(false, true)) return;
        if (connection instanceof PlayerGameConnection gc) {
            try {
                Player p = gc.getPlayer();
                if (p != null) p.closeDialog();
            } catch (Throwable ignore) {}
        }
        if (future != null) future.complete(true);
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
