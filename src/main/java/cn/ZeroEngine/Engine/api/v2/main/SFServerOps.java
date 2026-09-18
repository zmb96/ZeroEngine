package cn.ZeroEngine.Engine.api.v2.main;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public final class SFServerOps {

    public Server server() {
        return Bukkit.getServer();
    }

    public void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }

    public void broadcast(String perm, String msg) {
        Bukkit.broadcast(msg, perm);
    }

    public void msg(CommandSender sender, String msg) {
        sender.sendMessage(msg);
    }
}
