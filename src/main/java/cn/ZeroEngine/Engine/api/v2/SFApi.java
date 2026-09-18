package cn.ZeroEngine.Engine.api.v2;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import cn.ZeroEngine.Engine.api.v2.database.Database;
import cn.ZeroEngine.Engine.api.v2.economy.SFEconomy;
import cn.ZeroEngine.Engine.api.v2.event.SFEvents;
import cn.ZeroEngine.Engine.api.v2.feature.chat.ChatManager;
import cn.ZeroEngine.Engine.api.v2.feature.engine.BlockControl;
import cn.ZeroEngine.Engine.api.v2.feature.engine.DamageSystem;
import cn.ZeroEngine.Engine.api.v2.feature.engine.MonsterAttribute;
import cn.ZeroEngine.Engine.api.v2.feature.engine.ResourcePackManager;
import cn.ZeroEngine.Engine.api.v2.feature.engine.SpawnControl;
import cn.ZeroEngine.Engine.api.v2.feature.enchant.SFAttr;
import cn.ZeroEngine.Engine.api.v2.feature.permission.PermissionManager;
import cn.ZeroEngine.Engine.api.v2.feature.perf.PerformanceManager;
import cn.ZeroEngine.Engine.api.v2.feature.tick.TickManager;
import cn.ZeroEngine.Engine.api.v2.feature.world.WorldManager;
import cn.ZeroEngine.Engine.api.v2.main.SFLogger;
import cn.ZeroEngine.Engine.api.v2.main.SFPlayerOps;
import cn.ZeroEngine.Engine.api.v2.main.SFScheduler;
import cn.ZeroEngine.Engine.api.v2.main.SFServerOps;

import java.util.UUID;

public interface SFApi {

    SFLogger logger();

    SFEconomy economy();

    SFEvents events();

    SFScheduler scheduler();

    SFPlayerOps players();

    SFServerOps server();

    TickManager tick();

    ChatManager chat();

    WorldManager world();

    PermissionManager permission();

    PerformanceManager perf();

    SFAttr attr();

    Database database();

    MonsterAttribute monster();

    DamageSystem damage();

    BlockControl block();

    SpawnControl spawn();

    ResourcePackManager resourcePack();

    boolean isPluginListenerChat(Player player);

    boolean isPluginListenerChat(UUID playerId);

    void info(String msg);

    void info(String fmt, Object... args);

    void warn(String msg);

    void warn(String fmt, Object... args);

    void error(String msg);

    void error(String msg, Throwable t);

    void error(String fmt, Object... args);

    void broadcast(String msg);

    void broadcast(String perm, String msg);

    void msg(CommandSender sender, String msg);

    Player player(String name);

    Player player(UUID id);

    boolean giveMoney(OfflinePlayer p, double amount);

    boolean takeMoney(OfflinePlayer p, double amount);

    boolean setMoney(OfflinePlayer p, double amount);

    double balance(OfflinePlayer p);

    boolean transferMoney(OfflinePlayer from, OfflinePlayer to, double amount);

    String formatMoney(double amount);

    boolean teleport(Player p, Location loc);

    void run(Runnable r);

    void runAsync(Runnable r);

    void runLater(Runnable r, long ticks);

    void runTimer(Runnable r, long delay, long period);

    void console(String cmd);

    static SFApi get() {
        RegisteredServiceProvider<SFApi> rsp = Bukkit.getServer().getServicesManager().getRegistration(SFApi.class);
        if (rsp == null) {
            throw new IllegalStateException("SFApi not registered. Is ZeroEngine enabled?");
        }
        return rsp.getProvider();
    }

    static boolean isAvailable() {
        return Bukkit.getServer().getServicesManager().getRegistration(SFApi.class) != null;
    }
}
