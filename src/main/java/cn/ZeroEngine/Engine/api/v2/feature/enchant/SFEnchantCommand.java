package cn.ZeroEngine.Engine.api.v2.feature.enchant;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.ArrayList;
import java.util.List;

public class SFEnchantCommand implements CommandExecutor, TabCompleter {

    private final EnchantManager manager;

    public SFEnchantCommand(EnchantManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!(sender instanceof Player p)) {
            sf.msg(sender, ChatColor.RED + "此命令只能由玩家执行");
            return true;
        }
        if (!sender.hasPermission("sf.admin.enchant")) {
            sf.msg(sender, ChatColor.RED + "你没有权限");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "list" -> sendList(sender);
            case "apply" -> handleApply(p, args);
            case "book" -> handleBook(p, args);
            case "remove" -> handleRemove(p, args);
            case "info" -> handleInfo(sender, args);
            case "help" -> sendHelp(sender, label);
            default -> {
                sf.msg(sender, ChatColor.RED + "未知子命令: " + sub);
                sendHelp(sender, label);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 附魔系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出所有已注册附魔");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " apply <id> [等级]" + ChatColor.GRAY + " - 给手中物品附魔");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " book <id> [等级]" + ChatColor.GRAY + " - 获取附魔书");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " remove <id>" + ChatColor.GRAY + " - 移除手中物品的附魔");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <id>" + ChatColor.GRAY + " - 查看附魔详情");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " help" + ChatColor.GRAY + " - 显示此帮助");
    }

    private void sendList(CommandSender s) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== 已注册附魔 (" + manager.all().size() + ") =====");
        for (SEnchantment e : manager.all()) {
            String curse = e.isCursed() ? ChatColor.RED + " [诅咒]" : "";
            String treasure = e.isTreasure() ? ChatColor.AQUA + " [宝藏]" : "";
            sf.msg(s, ChatColor.YELLOW + e.id() + ChatColor.GRAY + " (" + e.displayName() + ")"
                    + ChatColor.DARK_GRAY + " max=" + e.maxLevel()
                    + treasure + curse);
        }
    }

    private void handleApply(Player p, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(p, ChatColor.RED + "用法: /sfenchant apply <id> [等级]");
            return;
        }
        SEnchantment e = manager.get(args[1]);
        if (e == null) {
            sf.msg(p, ChatColor.RED + "附魔不存在: " + args[1]);
            return;
        }
        int level = e.maxLevel();
        if (args.length >= 3) {
            try { level = Integer.parseInt(args[2]); }
            catch (NumberFormatException ex) {
                sf.msg(p, ChatColor.RED + "等级必须是数字");
                return;
            }
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sf.msg(p, ChatColor.RED + "你手中没有物品");
            return;
        }
        if (!e.canEnchantItem(item)) {
            sf.msg(p, ChatColor.RED + "此附魔不能应用到 " + item.getType().name());
            return;
        }
        for (var en : manager.getOn(item).entrySet()) {
            if (!en.getKey().id().equals(e.id()) && e.conflictsWith(en.getKey())) {
                sf.msg(p, ChatColor.RED + "与现有附魔 " + en.getKey().displayName() + " 冲突");
                return;
            }
        }
        int current = e.getLevel(item);
        int finalLevel = Math.max(1, Math.min(level, e.maxLevel()));
        e.setLevel(item, finalLevel);
        sf.msg(p, ChatColor.GREEN + "已将 " + e.displayName() + " " + SEnchantment.roman(finalLevel)
                + " 应用到 " + item.getType().name()
                + (current > 0 ? " (覆盖原等级 " + SEnchantment.roman(current) + ")" : ""));
    }

    private void handleBook(Player p, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(p, ChatColor.RED + "用法: /sfenchant book <id> [等级]");
            return;
        }
        SEnchantment e = manager.get(args[1]);
        if (e == null) {
            sf.msg(p, ChatColor.RED + "附魔不存在: " + args[1]);
            return;
        }
        int level = e.maxLevel();
        if (args.length >= 3) {
            try { level = Integer.parseInt(args[2]); }
            catch (NumberFormatException ex) {
                sf.msg(p, ChatColor.RED + "等级必须是数字");
                return;
            }
        }
        int finalLevel = Math.max(1, Math.min(level, e.maxLevel()));

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "附魔书");
            e.applyLore(meta, finalLevel);
            book.setItemMeta(meta);
        }
        e.setLevel(book, finalLevel);

        p.getInventory().addItem(book).forEach((idx, leftover) ->
                p.getWorld().dropItemNaturally(p.getLocation(), leftover));
        sf.msg(p, ChatColor.GREEN + "获得附魔书: " + e.displayName() + " " + SEnchantment.roman(finalLevel));
    }

    private void handleRemove(Player p, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(p, ChatColor.RED + "用法: /sfenchant remove <id>");
            return;
        }
        SEnchantment e = manager.get(args[1]);
        if (e == null) {
            sf.msg(p, ChatColor.RED + "附魔不存在: " + args[1]);
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sf.msg(p, ChatColor.RED + "你手中没有物品");
            return;
        }
        int current = e.getLevel(item);
        if (current <= 0) {
            sf.msg(p, ChatColor.RED + "此物品上没有该附魔");
            return;
        }
        e.removeFrom(item);
        sf.msg(p, ChatColor.GREEN + "已移除 " + e.displayName() + " " + SEnchantment.roman(current));
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfenchant info <id>");
            return;
        }
        SEnchantment e = manager.get(args[1]);
        if (e == null) {
            sf.msg(s, ChatColor.RED + "附魔不存在: " + args[1]);
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 附魔详情 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + e.namespace() + ":" + e.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + e.displayName());
        sf.msg(s, ChatColor.YELLOW + "最大等级: " + ChatColor.WHITE + e.maxLevel());
        sf.msg(s, ChatColor.YELLOW + "起始等级: " + ChatColor.WHITE + e.startLevel());
        sf.msg(s, ChatColor.YELLOW + "铁砧费用: " + ChatColor.WHITE + e.anvilCost());
        sf.msg(s, ChatColor.YELLOW + "描述: " + ChatColor.WHITE + e.description());
        sf.msg(s, ChatColor.YELLOW + "宝藏: " + (e.isTreasure() ? ChatColor.GREEN + "是" : ChatColor.RED + "否"));
        sf.msg(s, ChatColor.YELLOW + "诅咒: " + (e.isCursed() ? ChatColor.GREEN + "是" : ChatColor.RED + "否"));

        sf.msg(s, ChatColor.YELLOW + "可附魔物品:");
        StringBuilder items = new StringBuilder();
        for (String i : e.applicableItems()) {
            items.append(ChatColor.GRAY).append(i).append(" ");
        }
        sf.msg(s, items.toString());

        if (!e.conflicts().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "冲突附魔:");
            StringBuilder cs = new StringBuilder();
            for (String c : e.conflicts()) cs.append(ChatColor.RED).append(c).append(" ");
            sf.msg(s, cs.toString());
        }
        if (!e.conflictGroups().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "冲突组: " + ChatColor.RED + String.join(", ", e.conflictGroups()));
        }
        if (!e.attributes().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "属性加成:");
            for (SEnchantment.AttributeBonus a : e.attributes()) {
                String op = a.operation == AttributeModifier.Operation.ADD_NUMBER ? "+"
                        : a.operation == AttributeModifier.Operation.ADD_SCALAR ? "x" : "×";
                sf.msg(s, ChatColor.GRAY + "  " + a.name + ": " + a.attribute
                        + " " + op + a.base + " (每级+" + a.perLevel + ")");
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list", "apply", "book", "remove", "info", "help"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("apply") || sub.equals("book") || sub.equals("remove") || sub.equals("info")) {
                for (SEnchantment e : manager.all()) {
                    if (e.id().startsWith(args[1].toLowerCase())) out.add(e.id());
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("apply") || sub.equals("book")) {
                SEnchantment e = manager.get(args[1]);
                if (e != null) {
                    for (int i = 1; i <= e.maxLevel(); i++) out.add(String.valueOf(i));
                }
            }
        }
        return out;
    }
}
