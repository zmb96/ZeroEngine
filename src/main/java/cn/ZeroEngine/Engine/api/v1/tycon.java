package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class tycon implements CommandExecutor {

    private final main plugin;

    public tycon(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&', "&c正确用法：/ty <你要提的意见>"
            ));
            return true;
        }

        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(plugin.getDataFolder(), "ty.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String playerName = sender.getName();
        String uuid;

        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId().toString();
        } else {
            // 控制台执行
            uuid = "CONSOLE";
        }

        // ========== 5. 拼接用户输入的完整意见 ==========
        StringBuilder opinion = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            opinion.append(args[i]);
            if (i < args.length - 1) {
                opinion.append(" ");
            }
        }

        // ========== 6. 保存到配置 ==========
        String path = "ty." + playerName;
        config.set(path + ".uuid", uuid);
        config.set(path + ".opinion", opinion.toString());
        config.set(path + ".time", System.currentTimeMillis());

        // ========== 7. 写入文件 ==========
        try {
            config.save(file);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&', "&c保存意见失败！请检查服务器日志。"
            ));
            e.printStackTrace();
            return true;
        }

        // ========== 8. 成功消息 ==========
        sender.sendMessage(ChatColor.translateAlternateColorCodes(
                '&', "&a✅ 你的意见已保存！"
        ));
        sender.sendMessage(ChatColor.translateAlternateColorCodes(
                '&', "&7内容: &f" + opinion.toString()
        ));

        return true;
    }
}