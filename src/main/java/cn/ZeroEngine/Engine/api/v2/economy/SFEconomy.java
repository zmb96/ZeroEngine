package cn.ZeroEngine.Engine.api.v2.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public final class SFEconomy {

    private final EssentialsBackend essentials;
    private final VaultBackend vault;
    private final EconomyOps ops;

    public SFEconomy(JavaPlugin plugin) {
        this.essentials = new EssentialsBackend();
        this.vault = new VaultBackend(plugin);
        this.ops = new EconomyOps(essentials, vault);
    }

    public boolean hasEssentials() {
        return essentials.ready();
    }

    public boolean hasVault() {
        return vault.ready();
    }

    public boolean ready() {
        return essentials.ready() || vault.ready();
    }

    public EssentialsBackend essentials() {
        return essentials;
    }

    public VaultBackend vault() {
        return vault;
    }

    public EconomyOps ops() {
        return ops;
    }

    public boolean hasAccount(OfflinePlayer p) {
        return (essentials.ready() && essentials.hasAccount(p))
                || (vault.ready() && vault.hasAccount(p));
    }

    public boolean has(OfflinePlayer p, double amount) {
        return ops.has(p, amount);
    }

    public double balance(OfflinePlayer p) {
        return ops.balance(p);
    }

    public boolean give(OfflinePlayer p, double amount) {
        return ops.give(p, amount);
    }

    public boolean take(OfflinePlayer p, double amount) {
        return ops.take(p, amount);
    }

    public boolean set(OfflinePlayer p, double amount) {
        return ops.set(p, amount);
    }

    public boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        return ops.transfer(from, to, amount);
    }

    public String format(double amount) {
        return ops.format(amount);
    }
}
