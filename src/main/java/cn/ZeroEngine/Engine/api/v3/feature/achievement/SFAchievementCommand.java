package cn.ZeroEngine.Engine.api.v3.feature.achievement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SFAchievementCommand implements CommandExecutor, TabCompleter {

    private final AchievementManager manager;

    public SFAchievementCommand(AchievementManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 0) {
            list(s, null);
            return true;
        }
        String sub = a[0].toLowerCase();
        switch (sub) {
            case "list":
                list(s, a.length > 1 ? a[1] : null);
                break;
            case "grant":
                if (!s.hasPermission("sf.admin")) {
                    s.sendMessage(ChatColor.RED + "无权限");
                    return true;
                }
                grant(s, a);
                break;
            case "revoke":
                if (!s.hasPermission("sf.admin")) {
                    s.sendMessage(ChatColor.RED + "无权限");
                    return true;
                }
                revoke(s, a);
                break;
            case "gen":
                if (!s.hasPermission("sf.admin")) {
                    s.sendMessage(ChatColor.RED + "无权限");
                    return true;
                }
                manager.generateDataPack();
                s.sendMessage(ChatColor.GREEN + "[Achievement] Datapack generated. Restart server to load new advancements.");
                break;
            case "help":
                help(s);
                break;
            default:
                s.sendMessage(ChatColor.RED + "未知子命令: " + sub + " (可用: list/grant/revoke/gen/help)");
        }
        return true;
    }

    private void list(CommandSender s, String player) {
        if (manager.all().isEmpty()) {
            s.sendMessage(ChatColor.YELLOW + "暂无已注册成就");
            return;
        }
        s.sendMessage(ChatColor.GOLD + "=== 已注册成就 (" + manager.all().size() + ") ===");
        for (SAchievement a : manager.all()) {
            boolean granted = false;
            if (player != null && s instanceof Player) {
                Player p = Bukkit.getPlayerExact(player);
                if (p != null) {
                    granted = manager.isGranted(p, a.fullId());
                }
            }
            String status = granted ? ChatColor.GREEN + "✔" : ChatColor.GRAY + "✗";
            s.sendMessage(status + " " + ChatColor.WHITE + a.fullId() + " " + ChatColor.GRAY + a.title());
        }
    }

    private void grant(CommandSender s, String[] a) {
        if (a.length < 3) {
            s.sendMessage(ChatColor.RED + "用法: /sfadv grant <player> <namespace:id>");
            return;
        }
        Player target = Bukkit.getPlayerExact(a[1]);
        if (target == null) {
            s.sendMessage(ChatColor.RED + "玩家不在线: " + a[1]);
            return;
        }
        String fullId = a[2];
        if (manager.grant(target, fullId)) {
            s.sendMessage(ChatColor.GREEN + "已授予 " + target.getName() + " 成就: " + fullId);
        } else {
            s.sendMessage(ChatColor.RED + "授予失败（成就不存在/已获得/datapack未加载）: " + fullId);
        }
    }

    private void revoke(CommandSender s, String[] a) {
        if (a.length < 3) {
            s.sendMessage(ChatColor.RED + "用法: /sfadv revoke <player> <namespace:id>");
            return;
        }
        Player target = Bukkit.getPlayerExact(a[1]);
        if (target == null) {
            s.sendMessage(ChatColor.RED + "玩家不在线: " + a[1]);
            return;
        }
        if (manager.revoke(target, a[2])) {
            s.sendMessage(ChatColor.GREEN + "已撤销 " + target.getName() + " 成就: " + a[2]);
        } else {
            s.sendMessage(ChatColor.RED + "撤销失败: " + a[2]);
        }
    }

    private void help(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "=== /sfadv 帮助 ===");
        s.sendMessage(ChatColor.YELLOW + "  /sfadv list [player]" + ChatColor.WHITE + " 列出所有成就");
        s.sendMessage(ChatColor.YELLOW + "  /sfadv grant <player> <id>" + ChatColor.WHITE + " 授予成就");
        s.sendMessage(ChatColor.YELLOW + "  /sfadv revoke <player> <id>" + ChatColor.WHITE + " 撤销成就");
        s.sendMessage(ChatColor.YELLOW + "  /sfadv gen" + ChatColor.WHITE + " 重新生成datapack（需重启）");
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1) {
            List<String> subs = Arrays.asList("list", "grant", "revoke", "gen", "help");
            List<String> out = new ArrayList<>();
            for (String k : subs) {
                if (k.startsWith(a[0].toLowerCase())) out.add(k);
            }
            return out;
        }
        if (a.length == 3 && (a[0].equalsIgnoreCase("grant") || a[0].equalsIgnoreCase("revoke"))) {
            List<String> out = new ArrayList<>();
            for (SAchievement ach : manager.all()) {
                if (ach.fullId().startsWith(a[2])) out.add(ach.fullId());
            }
            return out;
        }
        if (a.length == 2 && (a[0].equalsIgnoreCase("grant") || a[0].equalsIgnoreCase("revoke"))) {
            List<String> out = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().startsWith(a[1])) out.add(p.getName());
            }
            return out;
        }
        return new ArrayList<>();
    }
}
