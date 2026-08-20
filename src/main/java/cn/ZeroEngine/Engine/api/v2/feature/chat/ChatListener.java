package cn.ZeroEngine.Engine.api.v2.feature.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import cn.ZeroEngine.Engine.api.v2.SF;

public class ChatListener implements Listener {

    private final ChatManager manager;

    public ChatListener(ChatManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        SF sf = SF.sf();
        Player p = e.getPlayer();

        if (manager.checkAndClearListening(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if (manager.isMuted(p)) {
            sf.msg(p, "§c你已被禁言" + (manager.muteReason(p) != null ? ": " + manager.muteReason(p) : "")
                    + (manager.muteRemaining(p) > 0 ? " §7(剩余 " + manager.muteRemaining(p) + "秒)" : ""));
            e.setCancelled(true);
            return;
        }

        String message = e.message().toString();
        message = message.replaceAll("^\"|\"$", "");
        message = manager.filterMessage(message);

        ChatManager.ChatChannel channel = manager.getChannel(p);
        String formatted = channel.prefix + manager.format(p, message);
        Component formattedComponent = Component.text(formatted);

        ChatManager.ChatContextImpl ctx = new ChatManager.ChatContextImpl(p, message, formattedComponent, channel);
        manager.dispatch(ctx);

        e.setCancelled(true);

        if (ctx.consumed()) {
            return;
        }

        Component finalComponent = ctx.formattedMessage();
        for (Player recipient : manager.getRecipients(p, ctx.channel())) {
            recipient.sendMessage(finalComponent);
        }
        sf.bukkit().getConsoleSender().sendMessage(finalComponent);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.setChannel(e.getPlayer(), "global");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
    }
}
