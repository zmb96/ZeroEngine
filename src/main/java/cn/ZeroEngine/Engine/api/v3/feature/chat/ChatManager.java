package cn.ZeroEngine.Engine.api.v3.feature.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.tick.TickManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatManager {

    private final ChatFormat defaultFormat = new ChatFormat("<{prefix}{name}{suffix}> {message}");
    private final Map<UUID, String> playerChannels = new ConcurrentHashMap<>();
    private final Map<String, ChatChannel> channels = new ConcurrentHashMap<>();
    private final Map<UUID, MuteEntry> muted = new ConcurrentHashMap<>();
    private final Set<String> blockedWords = ConcurrentHashMap.newKeySet();
    private final List<ChatHandler> handlers = new CopyOnWriteArrayList<>();
    private final Set<UUID> pluginListening = ConcurrentHashMap.newKeySet();
    private final TickManager tickManager;

    public ChatManager(TickManager tickManager) {
        this.tickManager = tickManager;
        registerChannel(new ChatChannel("global", "§7[§a全§7] ", null, 0));
        registerChannel(new ChatChannel("local", "§7[§e附近§7] ", 100.0, 0));
        registerChannel(new ChatChannel("staff", "§7[§c管理§7] ", null, 0));
    }

    public static class ChatFormat {
        public String template;

        public ChatFormat(String template) {
            this.template = template;
        }

        public String format(String prefix, String name, String suffix, String message) {
            return ChatColor.translateAlternateColorCodes('&',
                    template.replace("{prefix}", prefix == null ? "" : prefix)
                            .replace("{name}", name)
                            .replace("{suffix}", suffix == null ? "" : suffix)
                            .replace("{message}", message));
        }
    }

    public static class ChatChannel {
        public final String name;
        public final String prefix;
        public final Double range;
        public final long cooldownTicks;

        public ChatChannel(String name, String prefix, Double range, long cooldownTicks) {
            this.name = name;
            this.prefix = prefix;
            this.range = range;
            this.cooldownTicks = cooldownTicks;
        }
    }

    private static class MuteEntry {
        final long untilTick;
        final String reason;

        MuteEntry(long untilTick, String reason) {
            this.untilTick = untilTick;
            this.reason = reason;
        }
    }

    public void registerChannel(ChatChannel channel) {
        channels.put(channel.name.toLowerCase(), channel);
    }

    public void unregisterChannel(String name) {
        channels.remove(name.toLowerCase());
    }

    public ChatChannel getChannel(String name) {
        return channels.get(name.toLowerCase());
    }

    public Collection<ChatChannel> allChannels() {
        return channels.values();
    }

    public void setChannel(Player p, String channel) {
        if (channels.containsKey(channel.toLowerCase())) {
            playerChannels.put(p.getUniqueId(), channel.toLowerCase());
        }
    }

    public ChatChannel getChannel(Player p) {
        String name = playerChannels.get(p.getUniqueId());
        if (name == null) return channels.get("global");
        ChatChannel ch = channels.get(name);
        return ch != null ? ch : channels.get("global");
    }

    public void mute(Player p, long durationSeconds, String reason) {
        long until = durationSeconds <= 0 ? Long.MAX_VALUE : tickManager.now() + tickManager.fromSeconds(durationSeconds);
        muted.put(p.getUniqueId(), new MuteEntry(until, reason));
    }

    public void unmute(Player p) {
        muted.remove(p.getUniqueId());
    }

    public boolean isMuted(Player p) {
        MuteEntry e = muted.get(p.getUniqueId());
        if (e == null) return false;
        if (tickManager.now() >= e.untilTick) {
            muted.remove(p.getUniqueId());
            return false;
        }
        return true;
    }

    public String muteReason(Player p) {
        MuteEntry e = muted.get(p.getUniqueId());
        return e == null ? null : e.reason;
    }

    public long muteRemaining(Player p) {
        MuteEntry e = muted.get(p.getUniqueId());
        if (e == null) return 0;
        long remaining = e.untilTick - tickManager.now();
        return remaining < 0 ? 0 : tickManager.toSeconds(remaining);
    }

    public void addBlockedWord(String word) {
        blockedWords.add(word.toLowerCase());
    }

    public void removeBlockedWord(String word) {
        blockedWords.remove(word.toLowerCase());
    }

    public Set<String> blockedWords() {
        return blockedWords;
    }

    public String filterMessage(String message) {
        String filtered = message;
        for (String word : blockedWords) {
            filtered = filtered.replaceAll("(?i)" + java.util.regex.Pattern.quote(word),
                    "*".repeat(Math.max(1, word.length())));
        }
        return filtered;
    }

    public String format(Player p, String message) {
        SF sf = SF.sf();
        String prefix = "";
        String suffix = "";
        try {
            var perm = sf.permission();
            if (perm != null) {
                prefix = perm.getPrefix(p);
                suffix = perm.getSuffix(p);
            }
        } catch (Throwable ignored) {}
        return defaultFormat.format(prefix, p.getName(), suffix, message);
    }

    public Collection<Player> getRecipients(Player sender, ChatChannel channel) {
        SF sf = SF.sf();
        if (channel.range == null) {
            return new ArrayList<>(sf.bukkit().getOnlinePlayers());
        }
        List<Player> nearby = new ArrayList<>();
        for (Player p : sender.getWorld().getPlayers()) {
            if (p.getLocation().distance(sender.getLocation()) <= channel.range) {
                nearby.add(p);
            }
        }
        return nearby;
    }

    public void markListening(Player player) {
        pluginListening.add(player.getUniqueId());
    }

    public void markListening(UUID playerId) {
        pluginListening.add(playerId);
    }

    public void unmarkListening(Player player) {
        pluginListening.remove(player.getUniqueId());
    }

    public void unmarkListening(UUID playerId) {
        pluginListening.remove(playerId);
    }

    public boolean isPluginListening(Player player) {
        return pluginListening.contains(player.getUniqueId());
    }

    public boolean isPluginListening(UUID playerId) {
        return pluginListening.contains(playerId);
    }

    boolean checkAndClearListening(UUID playerId) {
        return pluginListening.remove(playerId);
    }

    public void registerHandler(ChatHandler handler) {
        handlers.add(handler);
        handlers.sort(Comparator.comparingInt(ChatHandler::priority));
    }

    public void unregisterHandler(ChatHandler handler) {
        handlers.remove(handler);
    }

    public List<ChatHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    void dispatch(ChatContextImpl ctx) {
        SF sf = SF.sf();
        for (ChatHandler h : handlers) {
            if (ctx.cancelled()) break;
            try {
                h.handle(ctx);
            } catch (Throwable t) {
                sf.error("[Chat] Handler {} threw: {}", h.getClass().getName(), t.getMessage());
            }
        }
    }

    static final class ChatContextImpl implements ChatHandler.ChatContext {
        private final Player player;
        private final String rawMessage;
        private Component formattedMessage;
        private boolean consumed;
        private boolean cancelled;
        private ChatChannel channel;

        ChatContextImpl(Player player, String rawMessage, Component formatted, ChatChannel channel) {
            this.player = player;
            this.rawMessage = rawMessage;
            this.formattedMessage = formatted;
            this.channel = channel;
        }

        @Override public Player player() { return player; }
        @Override public String rawMessage() { return rawMessage; }
        @Override public Component formattedMessage() { return formattedMessage; }
        @Override public void formattedMessage(Component c) { this.formattedMessage = c; }
        @Override public boolean consumed() { return consumed; }
        @Override public void consume() { this.consumed = true; }
        @Override public boolean cancelled() { return cancelled; }
        @Override public void cancel() { this.cancelled = true; }
        @Override public ChatChannel channel() { return channel; }
        @Override public void channel(ChatChannel c) { this.channel = c; }
    }
}
