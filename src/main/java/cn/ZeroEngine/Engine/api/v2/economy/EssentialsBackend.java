package cn.ZeroEngine.Engine.api.v2.economy;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public final class EssentialsBackend implements EconomyBackend {

    private final boolean loaded;

    public EssentialsBackend() {
        this.loaded = Bukkit.getPluginManager().getPlugin("Essentials") != null;
    }

    @Override
    public boolean ready() {
        return loaded;
    }

    @Override
    public boolean hasAccount(OfflinePlayer p) {
        return loaded && p.getName() != null;
    }

    @Override
    public double balance(OfflinePlayer p) {
        if (!loaded || p.getName() == null) return 0;
        try {
            return Economy.getMoneyExact(p.getName()).doubleValue();
        } catch (UserDoesNotExistException ignored) {
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @Override
    public boolean give(OfflinePlayer p, double amount) {
        if (!loaded || p.getName() == null) return false;
        try {
            Economy.setMoney(p.getName(), new BigDecimal(Double.toString(balance(p) + amount)));
            return true;
        } catch (UserDoesNotExistException ignored) {
        } catch (NoLoanPermittedException ignored) {
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean take(OfflinePlayer p, double amount) {
        if (!loaded || p.getName() == null) return false;
        try {
            Economy.setMoney(p.getName(), new BigDecimal(Double.toString(balance(p) - amount)));
            return true;
        } catch (UserDoesNotExistException ignored) {
        } catch (NoLoanPermittedException ignored) {
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean set(OfflinePlayer p, double amount) {
        if (!loaded || p.getName() == null) return false;
        try {
            Economy.setMoney(p.getName(), new BigDecimal(Double.toString(amount)));
            return true;
        } catch (UserDoesNotExistException ignored) {
        } catch (NoLoanPermittedException ignored) {
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
    }
}
