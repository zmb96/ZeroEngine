package cn.ZeroEngine.Engine.api.v3.main;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SFLogger {

    private final JavaPlugin plugin;

    public SFLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    public void info(String fmt, Object... args) {
        plugin.getLogger().info(String.format(fmt, args));
    }

    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

    public void warn(String fmt, Object... args) {
        plugin.getLogger().warning(String.format(fmt, args));
    }

    public void error(String msg) {
        plugin.getLogger().severe(msg);
    }

    public void error(String msg, Throwable t) {
        plugin.getLogger().log(Level.SEVERE, msg, t);
    }

    public void error(String fmt, Object... args) {
        plugin.getLogger().severe(String.format(fmt, args));
    }
}
