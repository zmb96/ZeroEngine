package cn.ZeroEngine.Engine.api.v3.feature.crop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SFCropCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = Arrays.asList("list", "give", "info", "look", "help");
    private final CropManager manager;
    private final SF sf;

    public SFCropCommand(CropManager manager) {
        this.manager = manager;
        this.sf = SF.sf();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(sender, label);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                sender.sendMessage("§6===== 自定义农作物 (" + manager.all().size() + ") =====");
                for (SCrop c : manager.all()) {
                    sender.sendMessage("§e" + c.id() + " §7- " + c.displayName() + " §8[" + c.cropBlock() + ", max=" + c.maxStage() + "]");
                }
            }
            case "give" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§c仅玩家可用"); return true; }
                if (args.length < 2) { sender.sendMessage("§c/" + label + " give <id> [数量]"); return true; }
                SCrop c = manager.get(args[1]);
                if (c == null) { sender.sendMessage("§c未找到作物: " + args[1]); return true; }
                int amount = args.length >= 3 ? Math.max(1, safe(args[2])) : 1;
                ItemStack seed = c.create(amount);
                p.getInventory().addItem(seed).forEach((i, left) -> p.getWorld().dropItemNaturally(p.getLocation(), left));
                sf.msg(p, "§a获得 " + c.displayName() + " §7x" + amount);
            }
            case "info" -> {
                if (args.length < 2) { sender.sendMessage("§c/" + label + " info <id>"); return true; }
                SCrop c = manager.get(args[1]);
                if (c == null) { sender.sendMessage("§c未找到作物: " + args[1]); return true; }
                sender.sendMessage("§6===== " + c.id() + " =====");
                sender.sendMessage("§e名称: §f" + c.displayName());
                sender.sendMessage("§e物品: §f" + c.material());
                sender.sendMessage("§e方块: §f" + c.cropBlock());
                sender.sendMessage("§e最大阶段: §f" + c.maxStage());
                sender.sendMessage("§e生长概率: §f" + c.growthChance());
                sender.sendMessage("§e食物: §f" + (c.isFood() ? "是 (营养" + c.foodNutrition() + ", 饱和" + c.foodSaturation() + ")" : "否"));
            }
            case "look" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("§c仅玩家可用"); return true; }
                Block target = p.getTargetBlockExact(6);
                if (target == null) { sender.sendMessage("§c未对准方块"); return true; }
                SCrop c = manager.findAt(target);
                if (c == null) { sender.sendMessage("§7此方块不是自定义作物"); return true; }
                sender.sendMessage("§a作物: §f" + c.id() + " §7(" + c.displayName() + ")");
                sender.sendMessage("§e阶段: §f" + c.currentStage(target) + "/" + c.maxStage() + (c.isMature(target) ? " §a(已成熟)" : ""));
            }
            default -> help(sender, label);
        }
        return true;
    }

    private void help(CommandSender s, String label) {
        s.sendMessage("§6===== /" + label + " 帮助 =====");
        s.sendMessage("§e/" + label + " list §7- 列出所有作物");
        s.sendMessage("§e/" + label + " give <id> [数量] §7- 给予种子");
        s.sendMessage("§e/" + label + " info <id> §7- 查看详情");
        s.sendMessage("§e/" + label + " look §7- 看向已种植作物查询");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> r = new ArrayList<>(SUBS);
            r.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return r;
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("info"))) {
            List<String> r = new ArrayList<>();
            for (SCrop c : manager.all()) if (c.id().startsWith(args[1].toLowerCase())) r.add(c.id());
            return r;
        }
        return new ArrayList<>();
    }

    private int safe(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 1; }
    }
}
