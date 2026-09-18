package cn.ZeroEngine.Engine.api.v2.feature.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private final ChatManager manager;

    public ChatCommand(ChatManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "channel", "ch" -> handleChannel(sender, args);
            case "mute" -> handleMute(sender, args);
            case "unmute" -> handleUnmute(sender, args);
            case "muteinfo" -> handleMuteInfo(sender, args);
            case "block" -> handleBlock(sender, args);
            case "unblock" -> handleUnblock(sender, args);
            case "blocklist" -> handleBlockList(sender);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "clear" -> handleClear(sender);
            case "help" -> sendHelp(sender, label);
            default -> { sf.msg(sender, ChatColor.RED + "未知子命令: " + sub); sendHelp(sender, label); }
        }
        return true;
    }

    private void handleChannel(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!(s instanceof Player p)) {
            sf.msg(s, ChatColor.RED + "此命令只能由玩家执行");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.YELLOW + "当前频道: " + manager.getChannel(p).name);
            sf.msg(s, ChatColor.GRAY + "可用频道: " + String.join(", ", manager.allChannels().stream().map(c -> c.name).toList()));
            return;
        }
        ChatManager.ChatChannel ch = manager.getChannel(args[1]);
        if (ch == null) {
            sf.msg(s, ChatColor.RED + "频道不存在: " + args[1]);
            return;
        }
        manager.setChannel(p, ch.name);
        sf.msg(s, ChatColor.GREEN + "已切换到频道: " + ch.name);
    }

    private void handleMute(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat mute <玩家> [秒数] [原因]");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        long duration = 0;
        if (args.length >= 3) {
            try { duration = Long.parseLong(args[2]); } catch (NumberFormatException ignored) {}
        }
        String reason = args.length >= 4 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "管理员禁言";
        manager.mute(target, duration, reason);
        sf.msg(s, ChatColor.GREEN + "已禁言 " + target.getName()
                + (duration > 0 ? " " + duration + "秒" : " 永久") + ": " + reason);
        sf.msg(target, ChatColor.RED + "你已被禁言" + (duration > 0 ? " " + duration + "秒" : " 永久") + ": " + reason);
    }

    private void handleUnmute(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat unmute <玩家>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        if (!manager.isMuted(target)) {
            sf.msg(s, ChatColor.YELLOW + target.getName() + " 未被禁言");
            return;
        }
        manager.unmute(target);
        sf.msg(s, ChatColor.GREEN + "已解除 " + target.getName() + " 的禁言");
        sf.msg(target, ChatColor.GREEN + "你的禁言已解除");
    }

    private void handleMuteInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat muteinfo <玩家>");
            return;
        }
        Player target = sf.player(args[1]);
        if (target == null) {
            sf.msg(s, ChatColor.RED + "玩家不在线");
            return;
        }
        if (!manager.isMuted(target)) {
            sf.msg(s, ChatColor.YELLOW + target.getName() + " 未被禁言");
            return;
        }
        sf.msg(s, ChatColor.GOLD + target.getName() + " 禁言信息:");
        sf.msg(s, ChatColor.YELLOW + "原因: " + manager.muteReason(target));
        sf.msg(s, ChatColor.YELLOW + "剩余: " + manager.muteRemaining(target) + "秒");
    }

    private void handleBlock(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat block <词语>");
            return;
        }
        manager.addBlockedWord(args[1]);
        sf.msg(s, ChatColor.GREEN + "已屏蔽词语: " + args[1]);
    }

    private void handleUnblock(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat unblock <词语>");
            return;
        }
        manager.removeBlockedWord(args[1]);
        sf.msg(s, ChatColor.GREEN + "已解除屏蔽: " + args[1]);
    }

    private void handleBlockList(CommandSender s) {
        SF sf = SF.sf();
        if (manager.blockedWords().isEmpty()) {
            sf.msg(s, ChatColor.GRAY + "屏蔽词列表为空");
        } else {
            sf.msg(s, ChatColor.GOLD + "===== 屏蔽词列表 (" + manager.blockedWords().size() + ") =====");
            sf.msg(s, ChatColor.YELLOW + String.join(", ", manager.blockedWords()));
        }
    }

    private void handleCreate(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat create <名称> [范围] [前缀]");
            sf.msg(s, ChatColor.GRAY + "示例: /sfchat create trade 0 §7[§6交易§7] ");
            sf.msg(s, ChatColor.GRAY + "范围=0 表示全局频道，>0 表示附近范围（格）");
            return;
        }
        String name = args[1].toLowerCase();
        if (manager.getChannel(name) != null) {
            sf.msg(s, ChatColor.RED + "频道已存在: " + name);
            return;
        }
        double range = 0;
        if (args.length >= 3) {
            try { range = Double.parseDouble(args[2]); } catch (NumberFormatException e) {
                sf.msg(s, ChatColor.RED + "范围必须是数字");
                return;
            }
        }
        String prefix = args.length >= 4 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) + " " : "§7[" + name + "] ";
        Double rangeObj = range > 0 ? range : null;
        manager.registerChannel(new ChatManager.ChatChannel(name, prefix, rangeObj, 0));
        sf.msg(s, ChatColor.GREEN + "频道已创建: " + name
                + (rangeObj != null ? " (范围=" + range + "格)" : " (全局)")
                + " 前缀=" + prefix);
    }

    private void handleDelete(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfchat delete <名称>");
            return;
        }
        String name = args[1].toLowerCase();
        if (Arrays.asList("global", "local", "staff").contains(name)) {
            sf.msg(s, ChatColor.RED + "不能删除内置频道");
            return;
        }
        if (manager.getChannel(name) == null) {
            sf.msg(s, ChatColor.RED + "频道不存在: " + name);
            return;
        }
        manager.unregisterChannel(name);
        sf.msg(s, ChatColor.GREEN + "频道已删除: " + name);
    }

    private void handleClear(CommandSender s) {
        SF sf = SF.sf();
        if (!s.hasPermission("sf.admin.chat")) {
            sf.msg(s, ChatColor.RED + "你没有权限");
            return;
        }
        for (int i = 0; i < 100; i++) {
            sf.broadcast("");
        }
        sf.broadcast(ChatColor.GOLD + "===== 聊天已清空 =====");
    }

    private void sendHelp(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 聊天系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " channel <名称>" + ChatColor.GRAY + " - 切换频道");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " mute <玩家> [秒数] [原因]" + ChatColor.GRAY + " - 禁言");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " unmute <玩家>" + ChatColor.GRAY + " - 解除禁言");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " muteinfo <玩家>" + ChatColor.GRAY + " - 查看禁言信息");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " block <词语>" + ChatColor.GRAY + " - 屏蔽词语");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " unblock <词语>" + ChatColor.GRAY + " - 解除屏蔽");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " blocklist" + ChatColor.GRAY + " - 屏蔽词列表");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " create <名称> [范围] [前缀]" + ChatColor.GRAY + " - 创建频道");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " delete <名称>" + ChatColor.GRAY + " - 删除频道");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " clear" + ChatColor.GRAY + " - 清空聊天");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        SF sf = SF.sf();
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : Arrays.asList("channel","mute","unmute","muteinfo","block","unblock","blocklist","create","delete","clear","help")) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("channel")) {
                for (ChatManager.ChatChannel ch : manager.allChannels()) {
                    if (ch.name.startsWith(args[1].toLowerCase())) out.add(ch.name);
                }
            } else if (sub.equals("mute") || sub.equals("unmute") || sub.equals("muteinfo")) {
                for (Player p : sf.bukkit().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(p.getName());
                }
            }
        }
        return out;
    }
}
