package cn.ZeroEngine.Engine.api.v3.economy;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultBackend implements EconomyBackend {

    private net.milkbowl.vault.economy.Economy vault;

    public VaultBackend(JavaPlugin plugin) {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                return;
            }
            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                    plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (rsp != null) {
                vault = rsp.getProvider();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean ready() {
        return vault != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer p) {
        if (vault == null) return false;
        try {
            return vault.hasAccount(p);
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public double balance(OfflinePlayer p) {
        if (vault == null) return 0;
        try {
            return vault.getBalance(p);
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @Override
    public boolean give(OfflinePlayer p, double amount) {
        if (vault == null) return false;
        try {
            EconomyResponse r = vault.depositPlayer(p, amount);
            return r.transactionSuccess();
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean take(OfflinePlayer p, double amount) {
        if (vault == null) return false;
        try {
            EconomyResponse r = vault.withdrawPlayer(p, amount);
            return r.transactionSuccess();
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean set(OfflinePlayer p, double amount) {
        if (vault == null) return false;
        try {
            double cur = vault.getBalance(p);
            if (cur > amount) {
                return vault.withdrawPlayer(p, cur - amount).transactionSuccess();
            } else if (cur < amount) {
                return vault.depositPlayer(p, amount - cur).transactionSuccess();
            }
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public String format(double amount) {
        if (vault == null) return String.format("%.2f", amount);
        try {
            return vault.format(amount);
        } catch (Throwable ignored) {
        }
        return String.format("%.2f", amount);
    }
}
