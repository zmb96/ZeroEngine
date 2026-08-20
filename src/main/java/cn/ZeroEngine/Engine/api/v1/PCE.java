package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Deprecated
public class PCE implements Listener {
    private final main plugin;

    public PCE(main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void ce(AsyncPlayerChatEvent e) {

    }
}
