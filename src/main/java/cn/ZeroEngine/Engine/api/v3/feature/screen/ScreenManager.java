package cn.ZeroEngine.Engine.api.v3.feature.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ScreenManager implements Listener {

    private static final int PROTOCOL_1_21_7 = 772;

    private final Plugin plugin;
    private final SF sf;
    private final java.util.Map<String, SScreen> screens = new ConcurrentHashMap<>();

    private final java.util.Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    private final java.util.List<Listener> triggerListeners = new CopyOnWriteArrayList<>();
    private final java.util.List<String> triggerCommands = new CopyOnWriteArrayList<>();

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

        Class<? extends Event> evt = screen.triggerEvent();
        if (evt != null) wireTriggerEvent(screen, evt);

        String cmd = screen.triggerCommand();
        if (cmd != null && !cmd.isEmpty()) wireTriggerCommand(screen, cmd);

        return this;
    }

    public boolean registerIfAbsent(SScreen screen) {
        if (screens.containsKey(screen.id())) return false;
        try { register(screen); return true; }
        catch (IllegalStateException ignore) { return false; }
    }

    public void unregister(String id) { screens.remove(id); }

    public void unregisterAll() { screens.clear(); pending.clear(); }

    public boolean open(Player player, String id) {
        if (player == null) return false;
        SScreen screen = screens.get(id);
        if (screen == null) {
            sf.msg(player, "§c未知屏幕: " + id);
            return false;
        }
        UUID uuid = player.getUniqueId();
        if (pending.containsKey(uuid)) {
            sf.msg(player, "§c你已有正在进行的弹窗");
            return false;
        }
        try { if (!screen.shouldShow(player)) return false; }
        catch (Throwable t) { sf.error("[Screen] shouldShow(Player) failed: " + id, t); return false; }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeOnTimeout(false, Math.max(1, screen.timeoutSeconds()), TimeUnit.SECONDS);
        Pending p = new Pending(screen, future);
        pending.put(uuid, p);
        future.whenComplete((result, ex) -> {
            pending.remove(uuid);
            try { player.closeDialog(); } catch (Throwable ignore) {}
        });

        try {
            player.showDialog(screen.buildDialog());
        } catch (Throwable t) {
            sf.error("[Screen] open in-world showDialog failed: " + id, t);
            pending.remove(uuid);
            return false;
        }
        return true;
    }

    private void wireTriggerEvent(SScreen screen, Class<? extends Event> evt) {
        if (!PlayerEvent.class.isAssignableFrom(evt)) {
            sf.error("[Screen] triggerEvent must be a PlayerEvent subclass, got: " + evt.getName() + " (screen=" + screen.id() + ")");
            return;
        }
        Listener fake = new Listener() {};
        EventExecutor exec = (listener, event) -> {
            if (!(event instanceof PlayerEvent)) return;
            Player p = ((PlayerEvent) event).getPlayer();
            if (p == null) return;
            try {
                if (pending.containsKey(p.getUniqueId())) return;
                if (!screen.shouldShow(p)) return;
                open(p, screen.id());
            } catch (Throwable t) {
                sf.error("[Screen] triggerEvent open failed: " + screen.id(), t);
            }
        };
        try {
            plugin.getServer().getPluginManager().registerEvent(
                    evt, fake, EventPriority.NORMAL, exec, plugin, false);
            triggerListeners.add(fake);
            sf.info("[Screen] triggerEvent bound: " + screen.id() + " <- " + evt.getSimpleName());
        } catch (Throwable t) {
            sf.error("[Screen] wire triggerEvent failed: " + screen.id(), t);
        }
    }

    private void wireTriggerCommand(SScreen screen, String cmd) {
        CommandExecutor exec = (sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sf.msg(sender, "§c只有玩家可以打开此屏幕");
                return true;
            }
            Player p = (Player) sender;
            open(p, screen.id());
            return true;
        };
        try {
            sf.regCommand(cmd, exec);
            triggerCommands.add(cmd);
            sf.info("[Screen] triggerCommand bound: " + screen.id() + " <- /" + cmd);
        } catch (Throwable t) {
            sf.error("[Screen] wire triggerCommand failed: " + screen.id(), t);
        }
    }

    public SScreen get(String id) { return screens.get(id); }

    public java.util.Collection<SScreen> all() { return java.util.Collections.unmodifiableCollection(screens.values()); }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConfigure(AsyncPlayerConnectionConfigureEvent e) {
        if (screens.isEmpty()) return;
        PlayerConfigurationConnection conn = e.getConnection();
        UUID uuid;
        try { uuid = conn.getProfile().getId(); } catch (Throwable t) { return; }
        if (uuid == null) return;

        int proto = getProtocolVersion(uuid);
        if (proto > 0 && proto < PROTOCOL_1_21_7) {
            sf.info("[Screen] 跳过自定义屏幕：客户端协议版本 " + proto + " < 1.21.7(" + PROTOCOL_1_21_7 + ")");
            return;
        }

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
            pending.put(uuid, new Pending(s, future));

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
        io.papermc.paper.connection.PlayerCommonConnection common = e.getCommonConnection();
        if (common == null) return;

        UUID uuid = null;
        if (common instanceof PlayerConfigurationConnection) {
            try { uuid = ((PlayerConfigurationConnection) common).getProfile().getId(); } catch (Throwable t) {}
        } else if (common instanceof io.papermc.paper.connection.PlayerGameConnection) {
            try {
                Player pl = ((io.papermc.paper.connection.PlayerGameConnection) common).getPlayer();
                if (pl != null) uuid = pl.getUniqueId();
            } catch (Throwable t) {}
        }
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

        ClickContext ctx = new ClickContext(action, common, e.getDialogResponseView(), p.future);
        try {
            p.screen.onClick(ctx);
        } catch (Throwable ex) {
            sf.error("[Screen] onClick failed: " + p.screen.id() + "/" + action, ex);
            if (!ctx.isResolved()) ctx.deny(Component.text("内部错误").color(NamedTextColor.RED));
        }
    }

    public void shutdown() {
        pending.clear();
        for (Listener l : triggerListeners) {
            try { org.bukkit.event.HandlerList.unregisterAll(l); } catch (Throwable ignore) {}
        }
        triggerListeners.clear();
        triggerCommands.clear();
    }

    private int getProtocolVersion(UUID uuid) {
        if (uuid == null) return -1;
        try {
            org.bukkit.plugin.Plugin via = org.bukkit.Bukkit.getPluginManager().getPlugin("ViaVersion");
            if (via == null) return -1;
            Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            java.lang.reflect.Method getAPI = viaClass.getMethod("getAPI");
            Object api = getAPI.invoke(null);
            if (api == null) return -1;
            java.lang.reflect.Method getPlayerVersion = api.getClass().getMethod("getPlayerVersion", java.util.UUID.class);
            Object result = getPlayerVersion.invoke(api, uuid);
            if (result instanceof Integer) return (Integer) result;
        } catch (Throwable t) {
            sf.error("[Screen] 获取协议版本失败", t);
        }
        return -1;
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
        Pending(SScreen screen, CompletableFuture<Boolean> future) {
            this.screen = screen; this.future = future;
        }
    }
}
