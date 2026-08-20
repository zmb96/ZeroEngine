package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

@Deprecated
public class PlayerChatEvent implements Listener {
    private final main plugin;

    public PlayerChatEvent(main plugin) {
        this.plugin = plugin;
    }

    @Deprecated
    @EventHandler
    public void l(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();
        String us = u.toString();
        String n = p.getName();
        String opu = plugin.getConfig().getString("cn.ZeroEngine.Engine.api.v1.PlayerChat.opsuuid");

        String id = " ";

        if (p.isOp()) {
            id = "&c管理员";
            if (us.equals(opu)) {
                id = "&b腐竹";
            }
        } else {
            id = "&a玩家";
        }
        String ms = ChatColor.translateAlternateColorCodes(
                '&', plugin.getConfig().getString("cn.ZeroEngine.Engine.api.v1.PlayerChat.message")
                        .replace("{player}", n)
                        .replace("{id}", id)
                        .replace("{message}", e.getMessage())
        );
        if (ms == null) {
            plugin.getLogger().warning("在~/SFServerPlugin/config.yml.cn.ZeroEngine.Engine.api.v1.PlayerChat.message的地方为null！！！");
        }
        e.setCancelled(true);
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes(
                '&', ms
        ));
    }
}