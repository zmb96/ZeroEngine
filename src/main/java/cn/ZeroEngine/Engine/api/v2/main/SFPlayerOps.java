package cn.ZeroEngine.Engine.api.v2.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class SFPlayerOps {

    public Player byName(String name) {
        return Bukkit.getPlayerExact(name);
    }

    public Player byId(UUID id) {
        return Bukkit.getPlayer(id);
    }

    public Player player(String name) {
        return Bukkit.getPlayerExact(name);
    }

    public Player player(UUID id) {
        return Bukkit.getPlayer(id);
    }
}
