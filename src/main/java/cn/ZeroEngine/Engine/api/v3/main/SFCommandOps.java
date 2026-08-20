package cn.ZeroEngine.Engine.api.v3.main;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SFCommandOps {

    private final JavaPlugin plugin;

    public SFCommandOps(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public SFCommandOps regEvent(Listener listener, JavaPlugin owner) {
        owner.getServer().getPluginManager().registerEvents(listener, owner);
        return this;
    }

    public SFCommandOps regEvent(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        return this;
    }

    public SFCommandOps regCommand(String name, CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd == null) {
            plugin.getLogger().warning("Command not found in plugin.yml: " + name);
            return this;
        }
        cmd.setExecutor(executor);
        if (executor instanceof TabCompleter tc) {
            cmd.setTabCompleter(tc);
        }
        return this;
    }

    public void console(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
