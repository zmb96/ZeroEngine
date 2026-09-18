package cn.ZeroEngine.Engine.api.v2.feature.item;

import org.bukkit.ChatColor;
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

public class SFItemCommand implements CommandExecutor, TabCompleter {

    private final ItemManager manager;

    public SFItemCommand(ItemManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.item")) {
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
            case "give" -> handleGive(sender, args);
            case "info" -> handleInfo(sender, args);
            case "hand" -> handleHand(sender);
            case "reload" -> handleReload(sender);
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
        sf.msg(s, ChatColor.GOLD + "===== SF 物品系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出所有已注册物品");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " give <id> [数量] [玩家]" + ChatColor.GRAY + " - 给予物品");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <id>" + ChatColor.GRAY + " - 查看物品详情");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " hand" + ChatColor.GRAY + " - 查看手中物品信息");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - 重载物品系统");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " help" + ChatColor.GRAY + " - 显示此帮助");
    }

    private void sendList(CommandSender s) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== 已注册物品 (" + manager.all().size() + ") =====");
        for (SItem i : manager.all()) {
            sf.msg(s, ChatColor.YELLOW + i.id() + ChatColor.GRAY + " (" + i.displayName() + ")"
                    + ChatColor.DARK_GRAY + " material=" + i.material().name()
                    + " stack=" + i.maxStackSize());
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(sender, ChatColor.RED + "用法: /sfitem give <id> [数量] [玩家]");
            return;
        }
        SItem item = manager.get(args[1]);
        if (item == null) {
            sf.msg(sender, ChatColor.RED + "物品不存在: " + args[1]);
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
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

        manager.give(target, item.id(), amount);
        sf.msg(sender, ChatColor.GREEN + "已给予 " + target.getName() + " " + item.displayName() + " x" + amount);
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfitem info <id>");
            return;
        }
        SItem item = manager.get(args[1]);
        if (item == null) {
            sf.msg(s, ChatColor.RED + "物品不存在: " + args[1]);
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 物品详情 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + item.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + item.displayName());
        sf.msg(s, ChatColor.YELLOW + "材质: " + ChatColor.WHITE + item.material().name());
        sf.msg(s, ChatColor.YELLOW + "堆叠: " + ChatColor.WHITE + item.maxStackSize());
        sf.msg(s, ChatColor.YELLOW + "不可破坏: " + (item.isUnbreakable() ? ChatColor.GREEN + "是" : ChatColor.RED + "否"));
        sf.msg(s, ChatColor.YELLOW + "描述: " + ChatColor.WHITE + item.description());

        if (!item.tags().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "标签: " + ChatColor.WHITE + String.join(", ", item.tags()));
        }
        if (!item.attributes().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "属性加成:");
            for (SItem.ItemAttributeBonus a : item.attributes()) {
                String op = a.operation == org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER ? "+"
                        : a.operation == org.bukkit.attribute.AttributeModifier.Operation.ADD_SCALAR ? "x" : "×";
                sf.msg(s, ChatColor.GRAY + "  " + a.name + ": " + a.attribute
                        + " " + op + a.baseValue);
            }
        }
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
        SItem custom = manager.find(item);
        if (custom == null) {
            sf.msg(s, ChatColor.GRAY + "这不是 SF 自定义物品 (" + item.getType().name() + ")");
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                sf.msg(s, ChatColor.GRAY + "显示名: " + meta.getDisplayName());
            }
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 手中物品 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + custom.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + custom.displayName());
        sf.msg(s, ChatColor.YELLOW + "材质: " + ChatColor.WHITE + custom.material().name());
        sf.msg(s, ChatColor.YELLOW + "数量: " + ChatColor.WHITE + item.getAmount());
    }

    private void handleReload(CommandSender s) {
        SF sf = SF.sf();
        manager.unregisterAll();
        sf.msg(s, ChatColor.GREEN + "物品系统已重置，请在代码中重新注册物品");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list", "give", "info", "hand", "reload", "help"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("info")) {
                for (SItem i : manager.all()) {
                    if (i.id().startsWith(args[1].toLowerCase())) out.add(i.id());
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give")) {
                out.add("1");
                out.add("16");
                out.add("64");
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give")) {
                SF sf = SF.sf();
                for (Player p : sf.bukkit().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[3].toLowerCase())) out.add(p.getName());
                }
            }
        }
        return out;
    }
}
