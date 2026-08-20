package cn.ZeroEngine.Engine.api.v3.economy;

import org.bukkit.OfflinePlayer;

public final class EconomyOps {

    private final EconomyBackend primary;
    private final EconomyBackend fallback;

    EconomyOps(EconomyBackend primary, EconomyBackend fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    public EconomyBackend backend(OfflinePlayer p) {
        if (primary.ready() && primary.hasAccount(p)) return primary;
        if (fallback.ready() && fallback.hasAccount(p)) return fallback;
        return primary.ready() ? primary : fallback;
    }

    public boolean has(OfflinePlayer p, double amount) {
        if (amount < 0) return false;
        return balance(p) >= amount;
    }

    public double balance(OfflinePlayer p) {
        EconomyBackend b = backend(p);
        return b.ready() ? b.balance(p) : 0;
    }

    public boolean give(OfflinePlayer p, double amount) {
        if (amount < 0) return false;
        EconomyBackend b = backend(p);
        if (!b.ready()) return false;
        return b.give(p, amount);
    }

    public boolean take(OfflinePlayer p, double amount) {
        if (amount < 0) return false;
        if (!has(p, amount)) return false;
        EconomyBackend b = backend(p);
        if (!b.ready()) return false;
        return b.take(p, amount);
    }

    public boolean set(OfflinePlayer p, double amount) {
        if (amount < 0) return false;
        EconomyBackend b = backend(p);
        if (!b.ready()) return false;
        return b.set(p, amount);
    }

    public boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        if (amount < 0) return false;
        if (!has(from, amount)) return false;
        if (!take(from, amount)) return false;
        if (!give(to, amount)) {
            give(from, amount);
            return false;
        }
        return true;
    }

    public String format(double amount) {
        EconomyBackend b = primary.ready() ? primary : fallback;
        return b.format(amount);
    }
}
