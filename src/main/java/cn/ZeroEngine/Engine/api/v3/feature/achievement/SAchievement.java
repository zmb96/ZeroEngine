package cn.ZeroEngine.Engine.api.v3.feature.achievement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.SF;

public abstract class SAchievement {

    private static Plugin plugin;

    public static void init(Plugin p) {
        plugin = p;
    }

    public static Plugin plugin() {
        return plugin;
    }

    public abstract String id();

    public abstract String title();

    public abstract String description();

    public abstract Material icon();

    public String namespace() {
        return "sf";
    }

    public String parent() {
        return null;
    }

    public Frame frame() {
        return Frame.TASK;
    }

    public boolean hidden() {
        return false;
    }

    public boolean showToast() {
        return true;
    }

    public boolean announceToChat() {
        return true;
    }

    public String criteriaName() {
        return "trigger";
    }

    public void onGrant(Player p) {
    }

    public void giveMoney(Player p, double amount) {
        SF.sf().giveMoney(p, amount);
    }

    public void giveItem(Player p, String itemId, int amount) {
        SF.sf().item().give(p, itemId, amount);
    }

    public void runCommand(String cmd) {
        SF.sf().console(cmd);
    }

    public String fullId() {
        return namespace() + ":" + id();
    }

    public org.bukkit.NamespacedKey namespacedKey() {
        return new org.bukkit.NamespacedKey(plugin, namespace() + "_" + id());
    }

    public enum Frame {
        TASK, GOAL, CHALLENGE
    }
}
