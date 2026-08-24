package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * /sfrecipe 命令（别名 /sfr），权限 sf.admin.recipe
 *
 * 子命令：
 *   /sfrecipe list                          列出全部自定义配方
 *   /sfrecipe info <id>                     查看配方形状/材料/产物
 *   /sfrecipe give <id> [玩家]              给玩家发一份配方产物（用于测试）
 *   /sfrecipe reload                        重注册所有配方
 *   /sfrecipe remove <id>                   移除一个配方
 *   /sfrecipe help                          帮助
 */
public class SFRecipeCommand implements CommandExecutor, TabCompleter {

    private final RecipeManager manager;

    public SFRecipeCommand(RecipeManager manager) { this.manager = manager; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.recipe")) {
            sf.msg(sender, ChatColor.RED + "你没有权限");
            return true;
        }
        if (args.length == 0) { help(sender, label); return true; }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> list(sender);
            case "info" -> info(sender, args);
            case "give" -> give(sender, args);
            case "reload" -> reload(sender);
            case "remove" -> remove(sender, args);
            case "help" -> help(sender, label);
            default -> { sf.msg(sender, ChatColor.RED + "未知子命令: " + sub); help(sender, label); }
        }
        return true;
    }

    private void help(CommandSender s, String label) {
        SF sf = SF.sf();
        sf.msg(s, ChatColor.GOLD + "===== SF 自定义配方系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出全部配方");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <id>" + ChatColor.GRAY + " - 查看配方详情");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " give <id> [玩家]" + ChatColor.GRAY + " - 发一份产物");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " remove <id>" + ChatColor.GRAY + " - 移除配方");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - 重注册全部配方");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " help" + ChatColor.GRAY + " - 帮助");
    }

    private void list(CommandSender s) {
        SF sf = SF.sf();
        if (manager.all().isEmpty()) { sf.msg(s, ChatColor.YELLOW + "暂无已注册配方"); return; }
        sf.msg(s, ChatColor.GOLD + "===== 自定义配方 (" + manager.all().size() + ") =====");
        for (SRecipe r : manager.all()) {
            Object res = r.result();
            String resStr;
            if (res instanceof Material m) resStr = m.name();
            else if (res instanceof SItem si) resStr = "SItem[" + si.id() + " x" + r.resultAmount() + "]";
            else resStr = String.valueOf(res);
            sf.msg(s, ChatColor.YELLOW + r.id()
                    + ChatColor.GRAY + " [" + r.mode() + "] -> "
                    + ChatColor.WHITE + resStr);
        }
    }

    private void info(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) { sf.msg(s, ChatColor.RED + "用法: /sfrecipe info <id>"); return; }
        SRecipe r = manager.get(args[1]);
        if (r == null) { sf.msg(s, ChatColor.RED + "配方不存在: " + args[1]); return; }

        sf.msg(s, ChatColor.GOLD + "===== 配方: " + r.id() + " =====");
        sf.msg(s, ChatColor.YELLOW + "模式: " + ChatColor.WHITE + r.mode());
        if (r.mode() == SRecipe.RecipeMode.SHAPED && !r.shape().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "形状: ");
            for (String row : r.shape()) {
                StringBuilder sb = new StringBuilder("   ");
                for (int i = 0; i < row.length(); i++) {
                    char c = row.charAt(i);
                    if (c == ' ') sb.append("· ");
                    else {
                        Object ing = r.ingredients().get(c);
                        sb.append(c).append(":").append(nameOf(ing)).append(" ");
                    }
                }
                sf.msg(s, ChatColor.GRAY + sb.toString());
            }
        }
        sf.msg(s, ChatColor.YELLOW + "材料:");
        for (Map.Entry<Character, Object> e : r.ingredients().entrySet()) {
            sf.msg(s, ChatColor.GRAY + "  '" + e.getKey() + "' = " + nameOf(e.getValue()));
        }
        Object res = r.result();
        sf.msg(s, ChatColor.YELLOW + "产物: " + ChatColor.WHITE + nameOf(res) + " x" + r.resultAmount());
    }

    private void give(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) { sf.msg(s, ChatColor.RED + "用法: /sfrecipe give <id> [玩家]"); return; }
        SRecipe r = manager.get(args[1]);
        if (r == null) { sf.msg(s, ChatColor.RED + "配方不存在: " + args[1]); return; }

        Player target;
        if (args.length >= 3) {
            target = org.bukkit.Bukkit.getPlayer(args[2]);
            if (target == null) { sf.msg(s, ChatColor.RED + "玩家不在线: " + args[2]); return; }
        } else {
            if (!(s instanceof Player p)) { sf.msg(s, ChatColor.RED + "控制台需指定玩家"); return; }
            target = p;
        }
        ItemStack item = r.resultItem();
        target.getInventory().addItem(item);
        sf.msg(s, ChatColor.GREEN + "已给 " + target.getName() + " 发放 " + nameOf(r.result()) + " x" + r.resultAmount());
    }

    private void reload(CommandSender s) {
        SF sf = SF.sf();
        manager.unregisterAll();
        sf.msg(s, ChatColor.YELLOW + "Bukkit 配方已清空，请在代码中重新注册");
    }

    private void remove(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) { sf.msg(s, ChatColor.RED + "用法: /sfrecipe remove <id>"); return; }
        boolean ok = manager.remove(args[1]);
        sf.msg(s, ok ? ChatColor.GREEN + "已移除: " + args[1] : ChatColor.RED + "移除失败: " + args[1]);
    }

    private String nameOf(Object o) {
        if (o instanceof Material m) return m.name();
        if (o instanceof SItem si) return si.id() + "(SItem)";
        return String.valueOf(o);
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list", "info", "give", "remove", "reload", "help"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("info") || sub.equals("give") || sub.equals("remove")) {
                for (SRecipe r : manager.all()) {
                    if (r.id().startsWith(args[1].toLowerCase())) out.add(r.id());
                }
            }
        } else if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) out.add(p.getName());
            }
        }
        return out;
    }
}
