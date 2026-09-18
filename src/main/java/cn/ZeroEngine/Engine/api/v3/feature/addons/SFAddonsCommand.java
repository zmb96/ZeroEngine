package cn.ZeroEngine.Engine.api.v3.feature.addons;

import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantChestListener;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantManager;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.LifestealEnchant;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.AncestralMightEnchant;
import cn.ZeroEngine.Engine.api.v3.feature.gui.GUIManager;
import cn.ZeroEngine.Engine.api.v3.feature.gui.SChestGUI;
import cn.ZeroEngine.Engine.api.v3.feature.crop.CropManager;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemChestListener;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;
import cn.ZeroEngine.Engine.api.v3.feature.item.MagicScepterItem;
import cn.ZeroEngine.Engine.api.v3.feature.recipe.RecipeManager;
import cn.ZeroEngine.Engine.api.v3.feature.screen.ScreenManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /sfaddons 命令 —— ZeroEngine 全局热加载管理。
 *
 * 子命令：
 *   unload  — 清空所有第三方注册的物品/GUI/作物/屏幕/配方/附魔，重置掉率
 *   load    — 发送 SFAddonsEvent(LOAD)，第三方插件监听后重新注册
 *   reload  — unload → load
 *   status  — 显示各系统当前注册数量
 *   help    — 帮助
 */
public class SFAddonsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 0) { help(s); return true; }
        String sub = a[0].toLowerCase();
        switch (sub) {
            case "help": help(s); break;
            case "unload": handleUnload(s); break;
            case "load":   handleLoad(s);   break;
            case "reload": handleReload(s); break;
            case "status": handleStatus(s); break;
            default:
                s.sendMessage(ChatColor.RED + "未知子命令：" + sub + "，可用: help/unload/load/reload/status");
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage(ChatColor.GOLD + "=== SFAddons 命令帮助 ===");
        s.sendMessage(ChatColor.YELLOW + "  /sfaddons unload   " + ChatColor.WHITE + "清空所有第三方注册（物品/GUI/作物/屏幕/配方/附魔），重置掉率");
        s.sendMessage(ChatColor.YELLOW + "  /sfaddons load     " + ChatColor.WHITE + "触发 SFAddonsEvent(LOAD)，第三方插件重新注册");
        s.sendMessage(ChatColor.YELLOW + "  /sfaddons reload   " + ChatColor.WHITE + "unload → load 一键热重载");
        s.sendMessage(ChatColor.YELLOW + "  /sfaddons status   " + ChatColor.WHITE + "查看各系统当前注册数量");
        s.sendMessage(ChatColor.YELLOW + "  /sfaddons help     " + ChatColor.WHITE + "显示本帮助");
    }

    private void handleUnload(CommandSender s) {
        SF sf = SF.sf();
        int n = 0;

        // 物品
        ItemManager im = sf.item();
        if (im != null) { im.unregisterAll(); im.registerIfAbsent(new MagicScepterItem()); n += im.all().size(); }

        // 附魔
        EnchantManager em = sf.enchant();
        if (em != null) { em.unregisterAll(); em.registerIfAbsent(new LifestealEnchant()); em.registerIfAbsent(new AncestralMightEnchant()); }

        // GUI
        GUIManager gm = sf.gui();
        if (gm != null) gm.closeAll();

        // 作物
        CropManager cm = sf.crops();
        if (cm != null) cm.unregisterAll();

        // 屏幕
        ScreenManager sm = sf.screens();
        if (sm != null) sm.unregisterAll();

        // 配方 + 机器
        RecipeManager rm = sf.recipes();
        if (rm != null) { rm.unregisterAllTables(); rm.unregisterAll(); }

        // 重置掉率缩放
        ItemChestListener.setChanceScale(1.0);
        EnchantChestListener.setChanceScale(1.0);

        // 通知第三方插件
        Bukkit.getPluginManager().callEvent(new SFAddonsEvent(SFAddonsEvent.UNLOAD));

        s.sendMessage(ChatColor.GREEN + "[SFAddons] 已卸载所有第三方注册，掉率已重置为 100%");
    }

    private void handleLoad(CommandSender s) {
        Bukkit.getPluginManager().callEvent(new SFAddonsEvent(SFAddonsEvent.LOAD));
        s.sendMessage(ChatColor.GREEN + "[SFAddons] 已发送 LOAD 事件，第三方插件将重新注册");
    }

    private void handleReload(CommandSender s) {
        handleUnload(s);
        handleLoad(s);
        s.sendMessage(ChatColor.GREEN + "[SFAddons] reload 完成");
    }

    private void handleStatus(CommandSender s) {
        SF sf = SF.sf();
        s.sendMessage(ChatColor.GOLD + "=== SFAddons 系统状态 ===");
        s.sendMessage(ChatColor.YELLOW + "  物品: " + ChatColor.WHITE + (sf.item() != null ? sf.item().all().size() : 0));
        s.sendMessage(ChatColor.YELLOW + "  附魔: " + ChatColor.WHITE + (sf.enchant() != null ? sf.enchant().all().size() : 0));
        s.sendMessage(ChatColor.YELLOW + "  作物: " + ChatColor.WHITE + (sf.crops() != null ? sf.crops().all().size() : 0));
        s.sendMessage(ChatColor.YELLOW + "  屏幕: " + ChatColor.WHITE + (sf.screens() != null ? sf.screens().all().size() : 0));
        s.sendMessage(ChatColor.YELLOW + "  掉率缩放: " + ChatColor.WHITE + String.format("%.0f%%", ItemChestListener.getChanceScale() * 100));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1) {
            List<String> subs = Arrays.asList("help", "unload", "load", "reload", "status");
            List<String> out = new ArrayList<>();
            for (String k : subs) if (k.startsWith(a[0].toLowerCase())) out.add(k);
            return out;
        }
        return new ArrayList<>();
    }
}
