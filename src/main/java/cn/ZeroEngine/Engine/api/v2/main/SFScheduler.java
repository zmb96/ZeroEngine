package cn.ZeroEngine.Engine.api.v2.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SFScheduler {

    private final JavaPlugin plugin;

    public SFScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void run(Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }

    public void runAsync(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    public void runLater(Runnable r, long ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, r, ticks);
    }

    public void runTimer(Runnable r, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, r, delay, period);
    }
}
