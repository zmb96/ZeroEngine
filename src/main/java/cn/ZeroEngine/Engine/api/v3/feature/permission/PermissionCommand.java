package cn.ZeroEngine.Engine.api.v3.feature.permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionCommand implements CommandExecutor, TabCompleter {

    private final PermissionManager manager;

    public PermissionCommand(PermissionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.permission")) {
            sf.msg(sender, ChatColor.RED + "你没有权限");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "group" -> handleGroup(sender, args);
            case "set" -> handleSet(sender, args);
            case "addperm" -> handleAddPerm(sender, args);
            case "rmperm" -> handleRmPerm(sender, args);
            case "info" -> handleInfo(sender, args);
            case "list" -> handleList(sender);
            case "help" -> sendHelp(sender, label);
            default -> { sf.msg(sender, ChatColor.RED + "未知子命令: " + args[0]); sendHelp(sender, label); }
        }
        return true;
    }

    private void handleGroup(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfperm group <create|delete|setprefix|setsuffix|addperm|rmperm|inherit> <组名> ...");
            return;
        }
        String action = args[1].toLowerCase();
        String groupName = args[2];
        switch (action) {
            case "create" -> {
                if (manager.getGroup(groupName) != null) {
                    sf.msg(s, ChatColor.RED + "组已存在: " + groupName);
                    return;
                }
                String prefix = args.length >= 4 ? args[3] : "";
                String suffix = args.length >= 5 ? args[4] : "";
                int weight = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
                manager.registerGroup(new PermissionManager.Group(groupName, prefix, suffix, weight));
                sf.msg(s, ChatColor.GREEN + "组已创建: " + groupName);
            }
            case "delete" -> {
                if (groupName.equalsIgnoreCase("default")) {
                    sf.msg(s, ChatColor.RED + "不能删除 default 组");
                    return;
                }
                sf.msg(s, ChatColor.YELLOW + "请通过代码删除组 (API限制)");
            }
            case "setprefix" -> {
                PermissionManager.Group g = manager.getGroup(groupName);
                if (g == null) { sf.msg(s, ChatColor.RED + "组不存在"); return; }
                g.prefix = args.length >= 4 ? args[3] : "";
                sf.msg(s, ChatColor.GREEN + groupName + " 前缀设为: " + g.prefix);
            }
            case "setsuffix" -> {
                PermissionManager.Group g = manager.getGroup(groupName);
                if (g == null) { sf.msg(s, ChatColor.RED + "组不存在"); return; }
                g.suffix = args.length >= 4 ? args[3] : "";
                sf.msg(s, ChatColor.GREEN + groupName + " 后缀设为: " + g.suffix);
            }
            case "addperm" -> {
                PermissionManager.Group g = manager.getGroup(groupName);
                if (g == null) { sf.msg(s, ChatColor.RED + "组不存在"); return; }
                if (args.length < 4) { sf.msg(s, ChatColor.RED + "用法: /sfperm group addperm <组名> <权限>"); return; }
                g.permissions.add(args[3].toLowerCase());
                sf.msg(s, ChatColor.GREEN + "已添加权限 " + args[3] + " 到组 " + groupName);
            }
            case "rmperm" -> {
                PermissionManager.Group g = manager.getGroup(groupName);
                if (g == null) { sf.msg(s, ChatColor.RED + "组不存在"); return; }
                if (args.length < 4) { sf.msg(s, ChatColor.RED + "用法: /sfperm group rmperm <组名> <权限>"); return; }
                g.permissions.remove(args[3].toLowerCase());
                sf.msg(s, ChatColor.GREEN + "已从组 " + groupName + " 移除权限 " + args[3]);
            }
            case "inherit" -> {
                PermissionManager.Group g = manager.getGroup(groupName);
                if (g == null) { sf.msg(s, ChatColor.RED + "组不存在"); return; }
                if (args.length < 4) { sf.msg(s, ChatColor.RED + "用法: /sfperm group inherit <组名> <父组名>"); return; }
                g.inherits.add(args[3].toLowerCase());
                sf.msg(s, ChatColor.GREEN + groupName + " 现在继承 " + args[3]);
            }
            default -> sf.msg(s, ChatColor.RED + "未知: " + action);
        }
    }

    private void handleSet(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfperm set <玩家> <组名>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        if (manager.getGroup(args[2]) == null) {
            sf.msg(s, ChatColor.RED + "组不存在: " + args[2]);
            return;
        }
        manager.setGroup(target, args[2]);
        sf.msg(s, ChatColor.GREEN + target.getName() + " 已设为 " + args[2]);
    }

    private void handleAddPerm(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfperm addperm <玩家> <权限>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        manager.addPermission(target, args[2]);
        sf.msg(s, ChatColor.GREEN + "已给 " + target.getName() + " 添加权限 " + args[2]);
    }

    private void handleRmPerm(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfperm rmperm <玩家> <权限>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        manager.removePermission(target, args[2]);
        sf.msg(s, ChatColor.GREEN + "已从 " + target.getName() + " 移除权限 " + args[2]);
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfperm info <玩家>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== " + target.getName() + " 权限信息 =====");
        sf.msg(s, ChatColor.YELLOW + "组: " + ChatColor.WHITE + manager.getGroupExact(target));
        sf.msg(s, ChatColor.YELLOW + "前缀: " + ChatColor.WHITE + manager.getPrefix(target));
        sf.msg(s, ChatColor.YELLOW + "后缀: " + ChatColor.WHITE + manager.getSuffix(target));
        sf.msg(s, ChatColor.YELLOW + "有效权限 (" + manager.getEffectivePermissions(target).size() + "):");
        for (String perm : manager.getEffectivePermissions(target)) {
            sf.msg(s, ChatColor.GRAY + "  " + perm);
        }
    }

    private void handleList(CommandSender s) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== 权限组 (" + manager.allGroups().size() + ") =====");
        for (PermissionManager.Group g : manager.allGroups()) {
            sf.msg(s, ChatColor.YELLOW + g.name + ChatColor.GRAY + " (weight=" + g.weight
                    + " perms=" + g.permissions.size() + " inherits=" + g.inherits + ")"
                    + " prefix=" + g.prefix + " suffix=" + g.suffix);
        }
    }

    private void sendHelp(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 权限系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " group <create|delete|setprefix|setsuffix|addperm|rmperm|inherit> <组名> ...");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " set <玩家> <组名>" + ChatColor.GRAY + " - 设置玩家组");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " addperm <玩家> <权限>" + ChatColor.GRAY + " - 添加个人权限");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " rmperm <玩家> <权限>" + ChatColor.GRAY + " - 移除个人权限");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <玩家>" + ChatColor.GRAY + " - 查看权限信息");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出所有组");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        SF sf = SF.sf();
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Arrays.asList("group","set","addperm","rmperm","info","list","help")) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("set") || sub.equals("addperm") || sub.equals("rmperm") || sub.equals("info")) {
                for (Player p : sf.bukkit().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
                }
            } else if (sub.equals("group")) {
                out.addAll(Arrays.asList("create","delete","setprefix","setsuffix","addperm","rmperm","inherit"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group")) {
                for (PermissionManager.Group g : manager.allGroups()) {
                    if (g.name.startsWith(args[2].toLowerCase())) out.add(g.name);
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                for (PermissionManager.Group g : manager.allGroups()) {
                    if (g.name.startsWith(args[2].toLowerCase())) out.add(g.name);
                }
            }
        }
        return out;
    }
}
