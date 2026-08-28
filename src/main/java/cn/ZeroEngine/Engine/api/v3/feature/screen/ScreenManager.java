package cn.ZeroEngine.Engine.api.v3.feature.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 自定义屏幕注册中心 + 进服阻塞分发器。
 *
 * 玩家进服处于 configuration phase 时，按 priority 顺序对每个 shouldShow 的 SScreen
 * 弹出 Dialog，用 CompletableFuture 阻塞等待玩家点击或超时。
 * accept() 放行下一个屏幕，deny() 踢出。
 */
public class ScreenManager implements Listener {

    private final Plugin plugin;
    private final SF sf;
    private final java.util.Map<String, SScreen> screens = new ConcurrentHashMap<>();

    private final java.util.Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public ScreenManager(Plugin plugin) {
        this.plugin = plugin;
        this.sf = SF.sf();
    }

    public ScreenManager register(SScreen screen) {
        String id = screen.id();
        if (screens.containsKey(id)) {
            throw new IllegalStateException("SScreen already registered: " + id);
        }
        String ns = slug(plugin.getName());
        screen.bind(ns, plugin);
        screens.put(id, screen);
        sf.info("[Screen] Registered: " + id + " (ns=" + ns + ", timeout=" + screen.timeoutSeconds() + "s, priority=" + screen.priority() + ")");
        return this;
    }

    public boolean registerIfAbsent(SScreen screen) {
        if (screens.containsKey(screen.id())) return false;
        try { register(screen); return true; }
        catch (IllegalStateException ignore) { return false; }
    }

    public void unregister(String id) { screens.remove(id); }

    public void unregisterAll() { screens.clear(); pending.clear(); }

    public SScreen get(String id) { return screens.get(id); }

    public java.util.Collection<SScreen> all() { return java.util.Collections.unmodifiableCollection(screens.values()); }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConfigure(AsyncPlayerConnectionConfigureEvent e) {
        if (screens.isEmpty()) return;
        PlayerConfigurationConnection conn = e.getConnection();
        UUID uuid;
        try { uuid = conn.getProfile().getId(); } catch (Throwable t) { return; }
        if (uuid == null) return;

        List<SScreen> toShow = new ArrayList<>();
        for (SScreen s : screens.values()) {
            try { if (s.shouldShow(conn)) toShow.add(s); }
            catch (Throwable ex) { sf.error("[Screen] shouldShow failed: " + s.id(), ex); }
        }
        if (toShow.isEmpty()) return;
        toShow.sort(Comparator.comparingInt(SScreen::priority));

        for (SScreen s : toShow) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeOnTimeout(false, Math.max(1, s.timeoutSeconds()), TimeUnit.SECONDS);
            pending.put(uuid, new Pending(s, future, conn));

            try {
                Dialog dialog = s.buildDialog();
                conn.getAudience().showDialog(dialog);
            } catch (Throwable ex) {
                sf.error("[Screen] buildDialog/showDialog failed: " + s.id(), ex);
                pending.remove(uuid);
                continue;
            }

            boolean ok;
            try {
                ok = Boolean.TRUE.equals(future.join());
            } catch (Throwable ex) {
                sf.error("[Screen] future.join failed: " + s.id(), ex);
                ok = false;
            }

            Pending p = pending.remove(uuid);
            try { if (p != null) conn.getAudience().closeDialog(); } catch (Throwable ignore) {}

            if (!ok) {
                try { conn.disconnect(Component.text("已拒绝进入服务器").color(NamedTextColor.RED)); } catch (Throwable ignore) {}
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCustomClick(PlayerCustomClickEvent e) {
        if (pending.isEmpty()) return;
        Object common = e.getCommonConnection();
        if (!(common instanceof PlayerConfigurationConnection conn)) return;
        UUID uuid;
        try { uuid = conn.getProfile().getId(); } catch (Throwable t) { return; }
        if (uuid == null) return;

        Pending p = pending.get(uuid);
        if (p == null) return;

        net.kyori.adventure.key.Key idKey = e.getIdentifier();
        if (idKey == null) return;
        String ns = idKey.namespace();
        String value = idKey.value();

        if (!ns.equals(p.screen.namespace())) return;
        String prefix = p.screen.id() + "/";
        if (!value.startsWith(prefix)) return;
        String action = value.substring(prefix.length());

        ClickContext ctx = new ClickContext(action, conn, p.future);
        try {
            p.screen.onClick(ctx);
        } catch (Throwable ex) {
            sf.error("[Screen] onClick failed: " + p.screen.id() + "/" + action, ex);
            if (!ctx.isResolved()) ctx.deny(Component.text("内部错误").color(NamedTextColor.RED));
        }
    }

    public void shutdown() {
        pending.clear();
    }

    private static String slug(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toLowerCase().toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') sb.append(c);
        }
        return sb.length() == 0 ? "sf" : sb.toString();
    }

    private static final class Pending {
        final SScreen screen;
        final CompletableFuture<Boolean> future;
        final PlayerConfigurationConnection conn;
        Pending(SScreen screen, CompletableFuture<Boolean> future, PlayerConfigurationConnection conn) {
            this.screen = screen; this.future = future; this.conn = conn;
        }
    }
}
