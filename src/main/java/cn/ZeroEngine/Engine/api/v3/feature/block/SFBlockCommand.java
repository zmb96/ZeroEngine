package cn.ZeroEngine.Engine.api.v3.feature.block;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;

import java.util.ArrayList;
import java.util.List;

public class SFBlockCommand implements CommandExecutor, TabCompleter {

    private final BlockManager manager;

    public SFBlockCommand(BlockManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.block")) {
            sf.msg(sender, ChatColor.RED + "你没有权限");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> sendList(sender);
            case "give" -> handleGive(sender, args);
            case "info" -> handleInfo(sender, args);
            case "hand" -> handleHand(sender);
            case "look" -> handleLook(sender);
            case "reload" -> handleReload(sender);
            case "help" -> sendHelp(sender, label);
            default -> {
                sf.msg(sender, ChatColor.RED + "未知子命令: " + args[0]);
                sendHelp(sender, label);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 自定义方块系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出所有已注册方块");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " give <id> [数量] [玩家]" + ChatColor.GRAY + " - 给予方块物品形式");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <id>" + ChatColor.GRAY + " - 查看方块详情");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " hand" + ChatColor.GRAY + " - 查看手中方块信息");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " look" + ChatColor.GRAY + " - 查看向看的方块是否为自定义方块");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - 重置方块系统");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " help" + ChatColor.GRAY + " - 显示此帮助");
    }

    private void sendList(CommandSender s) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== 已注册方块 (" + manager.all().size() + ") =====");
        for (SBlock b : manager.all()) {
            sf.msg(s, ChatColor.YELLOW + b.id() + ChatColor.GRAY + " (" + b.displayName() + ")"
                    + ChatColor.DARK_GRAY + " mat=" + b.material().name()
                    + " redstone=" + b.redstoneRadius()
                    + " drop=" + b.dropMode());
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(sender, ChatColor.RED + "用法: /sfblock give <id> [数量] [玩家]");
            return;
        }
        SBlock block = manager.get(args[1]);
        if (block == null) {
            sf.msg(sender, ChatColor.RED + "方块不存在: " + args[1]);
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try { amount = Integer.parseInt(args[2]); }
            catch (NumberFormatException ex) {
                sf.msg(sender, ChatColor.RED + "数量必须是数字");
                return;
            }
        }
        Player target;
        if (args.length >= 4) {
            target = sf.player(args[3]);
            if (target == null) {
                sf.msg(sender, ChatColor.RED + "玩家不存在: " + args[3]);
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sf.msg(sender, ChatColor.RED + "必须指定玩家或由玩家执行");
            return;
        }
        ItemManager im = sf.item();
        im.give(target, block.id(), amount);
        sf.msg(sender, ChatColor.GREEN + "已给予 " + target.getName() + " " + block.displayName() + " x" + amount);
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfblock info <id>");
            return;
        }
        SBlock b = manager.get(args[1]);
        if (b == null) {
            sf.msg(s, ChatColor.RED + "方块不存在: " + args[1]);
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 方块详情 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + b.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + b.displayName());
        sf.msg(s, ChatColor.YELLOW + "材质: " + ChatColor.WHITE + b.material().name());
        sf.msg(s, ChatColor.YELLOW + "硬度: " + ChatColor.WHITE + b.hardness());
        sf.msg(s, ChatColor.YELLOW + "爆炸抗性: " + ChatColor.WHITE + b.blastResistance());
        sf.msg(s, ChatColor.YELLOW + "可燃: " + (b.isFlammable() ? ChatColor.GREEN + "是" : ChatColor.RED + "否"));
        sf.msg(s, ChatColor.YELLOW + "发光: " + ChatColor.WHITE + b.lightLevel());
        sf.msg(s, ChatColor.YELLOW + "红石半径: " + ChatColor.WHITE + b.redstoneRadius()
                + (b.emitsRedstone() ? " (发射" + b.redstonePower() + ")" : ""));
        sf.msg(s, ChatColor.YELLOW + "掉落模式: " + ChatColor.WHITE + b.dropMode());
        sf.msg(s, ChatColor.YELLOW + "经验掉落: " + ChatColor.WHITE + b.expDrop());
        sf.msg(s, ChatColor.YELLOW + "描述: " + ChatColor.WHITE + b.description());
    }

    private void handleHand(CommandSender s) {
        SF sf = SF.sf();
        if (!(s instanceof Player p)) {
            sf.msg(s, ChatColor.RED + "此命令只能由玩家执行");
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sf.msg(s, ChatColor.RED + "你手中没有物品");
            return;
        }
        SBlock block = manager.findItem(item);
        if (block == null) {
            sf.msg(s, ChatColor.GRAY + "这不是 SF 自定义方块物品 (" + item.getType().name() + ")");
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 手中方块 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + block.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + block.displayName());
        sf.msg(s, ChatColor.YELLOW + "材质: " + ChatColor.WHITE + block.material().name());
        sf.msg(s, ChatColor.YELLOW + "数量: " + ChatColor.WHITE + item.getAmount());
    }

    private void handleLook(CommandSender s) {
        SF sf = SF.sf();
        if (!(s instanceof Player p)) {
            sf.msg(s, ChatColor.RED + "此命令只能由玩家执行");
            return;
        }
        Block target = p.getTargetBlockExact(8);
        if (target == null || target.getType().isAir()) {
            sf.msg(s, ChatColor.RED + "没有看向任何方块");
            return;
        }
        SBlock block = manager.findAt(target);
        if (block == null) {
            sf.msg(s, ChatColor.GRAY + "该方块不是 SF 自定义方块 (" + target.getType().name() + " @ " + loc(target) + ")");
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 看向的方块 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + block.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + block.displayName());
        sf.msg(s, ChatColor.YELLOW + "材质: " + ChatColor.WHITE + block.material().name());
        sf.msg(s, ChatColor.YELLOW + "位置: " + ChatColor.WHITE + loc(target));
        sf.msg(s, ChatColor.YELLOW + "红石半径: " + ChatColor.WHITE + block.redstoneRadius());
    }

    private void handleReload(CommandSender s) {
        SF sf = SF.sf();
        manager.unregisterAll();
        sf.msg(s, ChatColor.GREEN + "方块系统已重置，请在代码中重新注册方块");
    }

    private String loc(Block b) {
        return b.getWorld().getName() + " " + b.getX() + "," + b.getY() + "," + b.getZ();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list", "give", "info", "hand", "look", "reload", "help"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("info")) {
                for (SBlock b : manager.all()) {
                    if (b.id().startsWith(args[1].toLowerCase())) out.add(b.id());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            out.add("1"); out.add("16"); out.add("64");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            SF sf = SF.sf();
            for (Player p : sf.bukkit().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[3].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
