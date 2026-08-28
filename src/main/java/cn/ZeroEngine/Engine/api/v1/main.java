package cn.ZeroEngine.Engine.api.v1;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.SF;
import cn.ZeroEngine.Engine.api.v2.database.DatabaseManager;
import cn.ZeroEngine.Engine.api.v2.feature.enchant.LifestealEnchant;
import cn.ZeroEngine.Engine.api.v2.feature.enchant.AncestralMightEnchant;
import cn.ZeroEngine.Engine.api.v2.feature.enchant.EnchantManager;
import cn.ZeroEngine.Engine.api.v2.feature.chat.ChatCommand;
import cn.ZeroEngine.Engine.api.v2.feature.chat.ChatManager;
import cn.ZeroEngine.Engine.api.v2.feature.main.ReachCommand;
import cn.ZeroEngine.Engine.api.v2.feature.perf.PerformanceCommand;
import cn.ZeroEngine.Engine.api.v2.feature.permission.PermissionCommand;
import cn.ZeroEngine.Engine.api.v2.feature.permission.PermissionManager;
import cn.ZeroEngine.Engine.api.v2.feature.world.WorldCommand;
import cn.ZeroEngine.Engine.api.v2.feature.world.WorldManager;
import cn.ZeroEngine.Engine.api.v2.feature.teleport.TeleportManager;
@Deprecated
public final class main extends JavaPlugin {

    private static Economy eco;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        SF.init(this);
        SF sf = SF.sf();
        sf.info("Starting cn.ZeroEngine.Engine.api.v1 (v2 bridge enabled)");
        saveDefaultConfig();

        boolean dbOk = DatabaseManager.init(this);
        sf.info("Database ready: " + dbOk);

        teleportManager = new TeleportManager(this);
        sf.setTeleportManager(teleportManager);

        sf
                .regCommand("servermanagement", new servermanagement(this))
                .regCommand("ru", new rulescom())
                .regCommand("sh", new helpcom())
                .regCommand("ty", new tycon(this))
                .regCommand("giveit", new giveit(this));

        EnchantManager em = sf.enchant();
        em.register(new LifestealEnchant());
        em.register(new AncestralMightEnchant());
        // 注意：v1 main 不再绑定 /sfenchant 的执行器。
        // 该命令现在由 v3 SF.enchant() 懒加载统一绑定 —— 请通过 v3 SF 初始化 enchant 系统。
        // 任何第三方插件用 "import cn.ZeroEngine.Engine.api.v3.SF" 的 sf.enchant() 注册的附魔，
        // 都能立刻被 /sfenchant book/list 看到。

        ChatManager chatManager = sf.chat();
        sf.regCommand("sfchat", new ChatCommand(chatManager));

        WorldManager worldManager = sf.world();
        sf.regCommand("sfworld", new WorldCommand(worldManager));

        PermissionManager permManager = sf.permission();
        sf.regCommand("sfperm", new PermissionCommand(permManager));

        sf.reach();
        sf.regCommand("sfreach", new ReachCommand(sf.reach()));

        sf.perf();
        sf.regCommand("sfperf", new PerformanceCommand(sf.perf()));

        // 启动 v3 附魔系统的懒加载：
        //   1) 注册 v3 内置示例附魔 Lifesteal/Ancestral
        //   2) 将 plugin.yml 声明的 /sfenchant 命令执行器覆盖为 v3 SFEnchantCommand
        //      （v3 EnchantManager 才是第三方插件真正注册附魔的地方，比如 zmb96_ServerManagementPlugin）
        // 注意：v1 main 继续注册 v2 的附魔管理器，但 v2 的管理器不再对外发布命令。
        cn.ZeroEngine.Engine.api.v3.SF.init(this);
        cn.ZeroEngine.Engine.api.v3.SF v3sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
        v3sf.enchant();
        v3sf.item();

        sf.info("插件已加载");
        sf.info("Economy ready: " + sf.eco().ready() + " (Essentials=" + sf.eco().hasEssentials() + ", Vault=" + sf.eco().hasVault() + ")");
    }

    @Override
    public void onDisable() {
        SF sf = SF.sf();
        sf.info("Unload cn.ZeroEngine.Engine.api.v1.main");
        DatabaseManager.shutdown();
        SF.shutdown();
    }

    private boolean setupEconomy() {
        try {
            if (getServer().getPluginManager().getPlugin("Vault") == null) {
                return false;
            }
            RegisteredServiceProvider<Economy> rsp =
                    getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            eco = rsp.getProvider();
            return eco != null;
        } catch(NullPointerException e) {

        }
        return eco != null;
    }

    public static Economy eco() {
        return eco;
    }

    public static SF sf() {
        return SF.sf();
    }
}
