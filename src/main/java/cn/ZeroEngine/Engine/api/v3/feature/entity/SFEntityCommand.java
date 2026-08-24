package cn.ZeroEngine.Engine.api.v3.feature.entity;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.ArrayList;
import java.util.List;

/**
 * /sfentity 命令 —— 列出/生成/查看/清理 自定义生物
 *
 * 子命令：
 *   /sfentity list                       列出所有已注册自定义生物
 *   /sfentity spawn <id> [数量]          在脚下生成
 *   /sfentity info <id>                  查看属性/装备/生成条件
 *   /sfentity count [id]                 查看当前活动实例数
 *   /sfentity cleanup                    清理无效引用
 *   /sfentity reload                     清空注册表（需代码重新注册）
 *   /sfentity help                       帮助
 *
 * 别名 /sfe，权限 sf.admin.entity
 */
public class SFEntityCommand implements CommandExecutor, TabCompleter {

    private final EntityManager manager;

    public SFEntityCommand(EntityManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SF sf = SF.sf();
        if (!sender.hasPermission("sf.admin.entity")) {
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
            case "spawn" -> handleSpawn(sender, args);
            case "info" -> handleInfo(sender, args);
            case "count" -> handleCount(sender, args);
            case "cleanup" -> handleCleanup(sender);
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
        sf.msg(s, ChatColor.GOLD + "===== SF 自定义生物系统 =====");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " list" + ChatColor.GRAY + " - 列出所有已注册生物");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " spawn <id> [数量]" + ChatColor.GRAY + " - 在脚下生成");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " info <id>" + ChatColor.GRAY + " - 查看属性/装备/生成条件");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " count [id]" + ChatColor.GRAY + " - 当前活动实例数");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " cleanup" + ChatColor.GRAY + " - 清理无效引用");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - 清空注册表");
        sf.msg(s, ChatColor.YELLOW + "/" + label + " help" + ChatColor.GRAY + " - 显示此帮助");
    }

    private void sendList(CommandSender s) {
        SF sf = SF.sf();
        if (manager.all().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "暂无已注册的自定义生物");
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 已注册生物 (" + manager.all().size() + ") =====");
        for (SEntity e : manager.all()) {
            sf.msg(s, ChatColor.YELLOW + e.id()
                    + ChatColor.GRAY + " (" + e.displayName() + ")"
                    + ChatColor.DARK_GRAY + " type=" + e.entityType()
                    + " hostility=" + e.hostility()
                    + " hp=" + e.maxHealth()
                    + " atk=" + e.attackDamage()
                    + " active=" + manager.activeCount(e.id()));
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        SF sf = SF.sf();
        if (!(sender instanceof Player p)) {
            sf.msg(sender, ChatColor.RED + "此命令只能由玩家执行");
            return;
        }
        if (args.length < 2) {
            sf.msg(sender, ChatColor.RED + "用法: /sfentity spawn <id> [数量]");
            return;
        }
        SEntity def = manager.get(args[1]);
        if (def == null) {
            sf.msg(sender, ChatColor.RED + "生物不存在: " + args[1]);
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Math.min(50, Integer.parseInt(args[2])));
            } catch (NumberFormatException ex) {
                sf.msg(sender, ChatColor.RED + "数量必须是数字");
                return;
            }
        }
        Location loc = p.getLocation();
        int ok = 0;
        for (int i = 0; i < amount; i++) {
            LivingEntity spawned = manager.spawn(def.id(), loc);
            if (spawned != null) ok++;
        }
        sf.msg(sender, ChatColor.GREEN + "已生成 " + def.displayName() + " x" + ok + "/" + amount);
    }

    private void handleInfo(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length < 2) {
            sf.msg(s, ChatColor.RED + "用法: /sfentity info <id>");
            return;
        }
        SEntity e = manager.get(args[1]);
        if (e == null) {
            sf.msg(s, ChatColor.RED + "生物不存在: " + args[1]);
            return;
        }
        sf.msg(s, ChatColor.GOLD + "===== 生物详情 =====");
        sf.msg(s, ChatColor.YELLOW + "ID: " + ChatColor.WHITE + e.id());
        sf.msg(s, ChatColor.YELLOW + "名称: " + ChatColor.WHITE + e.displayName());
        sf.msg(s, ChatColor.YELLOW + "类型: " + ChatColor.WHITE + e.entityType());
        sf.msg(s, ChatColor.YELLOW + "阵营: " + ChatColor.WHITE + e.hostility());
        sf.msg(s, ChatColor.YELLOW + "属性: " + ChatColor.WHITE
                + "hp=" + e.maxHealth()
                + " atk=" + e.attackDamage()
                + " atkSpeed=" + e.attackSpeed()
                + " speed=" + e.movementSpeed()
                + " armor=" + e.armor()
                + " toughness=" + e.armorToughness()
                + " kbResist=" + e.knockbackResistance()
                + " follow=" + e.followRange());

        SEntity.SpawnCondition c = e.spawnCondition();
        sf.msg(s, ChatColor.YELLOW + "生成条件:");
        sf.msg(s, ChatColor.GRAY + "  chance=" + c.chance
                + " worlds=" + (c.worlds.isEmpty() ? "*" : c.worlds)
                + " biomes=" + (c.biomes.isEmpty() ? "*" : c.biomes.size())
                + " y=[" + c.minY + "," + c.maxY + "]");
        sf.msg(s, ChatColor.GRAY + "  light=[" + c.minLight + "," + c.maxLight + "]"
                + " burnInDay=" + c.burnInDaylight
                + " nightOnly=" + c.onlyAtNight
                + " replaceVanilla=" + c.replaceVanillaSpawns
                + " limit/chunk=" + c.spawnLimitPerChunk);

        if (!e.equipment().isEmpty()) {
            sf.msg(s, ChatColor.YELLOW + "装备:");
            for (SEntity.EquipmentEntry eq : e.equipment()) {
                sf.msg(s, ChatColor.GRAY + "  " + eq.slot
                        + " chance=" + (eq.chance * 100) + "%"
                        + " drop=" + (eq.dropOnDeath ? (eq.dropChance * 100) + "%" : "no")
                        + " -> " + (eq.item != null ? eq.item.getType() : "null"));
            }
        }
        sf.msg(s, ChatColor.YELLOW + "当前活动: " + manager.activeCount(e.id()));
    }

    private void handleCount(CommandSender s, String[] args) {
        SF sf = SF.sf();
        if (args.length >= 2) {
            sf.msg(s, ChatColor.YELLOW + args[1] + " 活动数: " + manager.activeCount(args[1]));
        } else {
            sf.msg(s, ChatColor.YELLOW + "总活动数: " + manager.activeCount());
        }
    }

    private void handleCleanup(CommandSender s) {
        SF sf = SF.sf();
        int before = manager.activeCount();
        manager.cleanup();
        int after = manager.activeCount();
        sf.msg(s, ChatColor.GREEN + "清理完成: " + before + " -> " + after);
    }

    private void handleReload(CommandSender s) {
        SF sf = SF.sf();
        manager.unregisterAll();
        sf.msg(s, ChatColor.GREEN + "生物系统已重置，请在代码中重新注册");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : new String[]{"list", "spawn", "info", "count", "cleanup", "reload", "help"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("spawn") || sub.equals("info") || sub.equals("count")) {
                for (SEntity e : manager.all()) {
                    if (e.id().startsWith(args[1].toLowerCase())) out.add(e.id());
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("spawn")) {
                out.add("1"); out.add("3"); out.add("10");
            }
        }
        return out;
    }
}
