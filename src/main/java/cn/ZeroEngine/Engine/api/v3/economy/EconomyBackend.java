package cn.ZeroEngine.Engine.api.v3.economy;

import org.bukkit.OfflinePlayer;

public interface EconomyBackend {

    boolean ready();

    boolean hasAccount(OfflinePlayer p);

    double balance(OfflinePlayer p);

    boolean give(OfflinePlayer p, double amount);

    boolean take(OfflinePlayer p, double amount);

    boolean set(OfflinePlayer p, double amount);

    String format(double amount);
}
