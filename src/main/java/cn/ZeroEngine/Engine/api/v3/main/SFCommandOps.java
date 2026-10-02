package cn.ZeroEngine.Engine.api.v3.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

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
        if (cmd != null) {
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter tc) cmd.setTabCompleter(tc);
            return this;
        }

        
        Command found = findCommand(name);
        if (found instanceof PluginCommand pc) {
            pc.setExecutor(executor);
            if (executor instanceof TabCompleter tc) pc.setTabCompleter(tc);
            plugin.getLogger().info("[SF regCommand] bound command /" + name
                    + " via cross-plugin lookup (owner=" + pc.getPlugin().getName() + ")");
            return this;
        }

        plugin.getLogger().warning("Command not found: /" + name
                + " (not in this plugin's plugin.yml and not registered in Bukkit CommandMap)");
        return this;
    }

    private static Command findCommand(String name) {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager spm) {
                java.lang.reflect.Field fCmdMap = SimplePluginManager.class.getDeclaredField("commandMap");
                fCmdMap.setAccessible(true);
                Object commandMap = fCmdMap.get(spm);
                if (commandMap == null) return null;
                
                try {
                    Method m = commandMap.getClass().getMethod("getKnownCommands");
                    m.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<String, Command> map = (Map<String, Command>) m.invoke(commandMap);
                    Command direct = map.get(name);
                    if (direct != null) return direct;
                    
                    for (Iterator<Map.Entry<String, Command>> it = map.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, Command> e = it.next();
                        if (e.getKey().endsWith(":" + name)) return e.getValue();
                    }
                } catch (Throwable ignore) {
                    try {
                        java.lang.reflect.Field fKc = commandMap.getClass().getDeclaredField("knownCommands");
                        fKc.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        Map<String, Command> map = (Map<String, Command>) fKc.get(commandMap);
                        Command direct = map.get(name);
                        if (direct != null) return direct;
                        for (Iterator<Map.Entry<String, Command>> it = map.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry<String, Command> e = it.next();
                            if (e.getKey().endsWith(":" + name)) return e.getValue();
                        }
                    } catch (Throwable ignore2) { return null; }
                }
            }
        } catch (Throwable t) { return null; }
        return null;
    }

    public void console(String cmd) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
