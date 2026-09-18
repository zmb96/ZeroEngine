package cn.ZeroEngine.Engine.api.v3.feature.main;

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

public class ReachCommand implements CommandExecutor, TabCompleter {

    private final ReachManager manager;

    public ReachCommand(ReachManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.reach")) {
            sf.msg(sender, ChatColor.RED + "你没有权限");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "info" -> handleInfo(sender, args);
            case "set" -> handleSet(sender, args);
            case "add" -> handleAdd(sender, args);
            case "reset" -> handleReset(sender, args);
            case "help" -> sendHelp(sender, label);
            default -> { sf.msg(sender, ChatColor.RED + "未知子命令: " + sub); sendHelp(sender, label); }
        }
        return true;
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        Player target = getTarget(s, args);
        if (target == null) return;
        sf.msg(s, ChatColor.GOLD + "===== " + target.getName() + " 交互距离 =====");
        sf.msg(s, ChatColor.YELLOW + "方块交互: " + ChatColor.WHITE + String.format("%.2f", manager.getBlockReach(target)) + " 格");
        sf.msg(s, ChatColor.YELLOW + "实体交互: " + ChatColor.WHITE + String.format("%.2f", manager.getEntityReach(target)) + " 格");
        sf.msg(s, ChatColor.GRAY + "(原版默认: 方块 4.5, 实体 3.0)");
    }

    private void handleSet(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfreach set <block|entity|both> <距离> [玩家]");
            return;
        }
        Player target = getTarget(s, args, 3);
        if (target == null) return;
        double range;
        try { range = Double.parseDouble(args[2]); } catch (NumberFormatException e) {
            sf.msg(s, ChatColor.RED + "距离必须是数字");
            return;
        }
        if (range < 0) { sf.msg(s, ChatColor.RED + "距离不能为负"); return; }
        switch (args[1].toLowerCase()) {
            case "block" -> { manager.setBlockReach(target, range); sf.msg(s, ChatColor.GREEN + target.getName() + " 方块交互距离设为 " + range); }
            case "entity" -> { manager.setEntityReach(target, range); sf.msg(s, ChatColor.GREEN + target.getName() + " 实体交互距离设为 " + range); }
            case "both" -> { manager.setBlockReach(target, range); manager.setEntityReach(target, range); sf.msg(s, ChatColor.GREEN + target.getName() + " 交互距离都设为 " + range); }
            default -> sf.msg(s, ChatColor.RED + "类型: block / entity / both");
        }
    }

    private void handleAdd(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 3) {
            sf.msg(s, ChatColor.RED + "用法: /sfreach add <block|entity|both> <增量> [玩家]");
            return;
        }
        Player target = getTarget(s, args, 3);
        if (target == null) return;
        double bonus;
        try { bonus = Double.parseDouble(args[2]); } catch (NumberFormatException e) {
            sf.msg(s, ChatColor.RED + "增量必须是数字");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "block" -> { manager.addBlockReach(target, bonus); sf.msg(s, ChatColor.GREEN + target.getName() + " 方块交互 +" + bonus + " → " + String.format("%.2f", manager.getBlockReach(target))); }
            case "entity" -> { manager.addEntityReach(target, bonus); sf.msg(s, ChatColor.GREEN + target.getName() + " 实体交互 +" + bonus + " → " + String.format("%.2f", manager.getEntityReach(target))); }
            case "both" -> { manager.addBlockReach(target, bonus); manager.addEntityReach(target, bonus); sf.msg(s, ChatColor.GREEN + target.getName() + " 交互距离 +" + bonus); }
            default -> sf.msg(s, ChatColor.RED + "类型: block / entity / both");
        }
    }

    private void handleReset(CommandSender s, String[] args) {
        SF sf = SF.sf();
        Player target = getTarget(s, args);
        if (target == null) return;
        manager.reset(target);
        sf.msg(s, ChatColor.GREEN + target.getName() + " 交互距离已重置为默认值");
    }

    private Player getTarget(CommandSender s, String[] args) {
        return getTarget(s, args, 1);
    }

    private Player getTarget(CommandSender s, String[] args, int nameIndex) {
        SF sf = SF.sf();
        if (args.length > nameIndex) {
            Player p = sf.player(args[nameIndex]);
            if (p == null) { sf.msg(s, ChatColor.RED + "玩家不在线"); return null; }
            return p;
        }
        if (s instanceof Player p) return p;
        sf.msg(s, ChatColor.RED + "请指定玩家");
        return null;
    }

    private void sendHelp(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 交互距离 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info [玩家]" + ChatColor.GRAY + " - 查看交互距离");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " set <block|entity|both> <距离> [玩家]" + ChatColor.GRAY + " - 设置距离");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " add <block|entity|both> <增量> [玩家]" + ChatColor.GRAY + " - 增加距离");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " reset [玩家]" + ChatColor.GRAY + " - 重置为默认");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Arrays.asList("info", "set", "add", "reset", "help")) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add")) {
                out.addAll(Arrays.asList("block", "entity", "both"));
            }
        }
        return out;
    }
}
