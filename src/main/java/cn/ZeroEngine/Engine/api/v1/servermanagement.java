package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class servermanagement implements CommandExecutor, TabCompleter {

    private final main plugin;

    public servermanagement(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        InputStream is = getClass().getResourceAsStream("/comhelp.txt");
        if (!sender.hasPermission("servermanagement.use")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "用法: /servermanagement reload");
            return true;
        }

        switch (args[0]) {
            case "reload":
                reload(sender);
                return true;
            case "help":
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sender.sendMessage(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
                return true;
            default:
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sender.sendMessage(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
                return true;
        }
    }

    private boolean reload(CommandSender sender) {
        try {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "✅ 配置已重新加载！");
            plugin.getLogger().info("配置被 " + sender.getName() + " 重新加载");
            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "❌ 重载失败！");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload");
        }
        return new ArrayList<>();
    }
}