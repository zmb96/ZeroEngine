package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

@Deprecated
public class PlayerJoinOrQuitEvent implements Listener {
    private final main plugin;

    public PlayerJoinOrQuitEvent(main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void JoinMessage(PlayerJoinEvent e) throws IOException {
        Player p = e.getPlayer();
        String n = p.getName();
        String us = p.getUniqueId().toString();
        File foter = plugin.getDataFolder();
        File file = new File(foter, "giveit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(file);
        ItemStack i1 = new ItemStack(Material.IRON_INGOT, 10);
        ItemStack i3 = new ItemStack(Material.DIAMOND, 3);
        ItemStack i4 = new ItemStack(Material.LAPIS_LAZULI, 10);
        ItemStack i5 = new ItemStack(Material.EMERALD, 5);
        ItemStack i6 = new ItemStack(Material.LAVA_BUCKET, 1);
        ItemStack i7 = new ItemStack(Material.WATER_BUCKET, 1);
        ItemStack i8 = new ItemStack(Material.OAK_SAPLING, 5);

        FileConfiguration c1 = plugin.getConfig();

        String jm = c1.getString("cn.ZeroEngine.Engine.api.v1.JoQMess.jm").replace("{player}",n);
        if (jm == null) {
            plugin.getLogger().warning("在~/SFServerPlugin/config.yml.cn.ZeroEngine.Engine.api.v1.JoQMess.jm的地方为null！！！");
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes(
                '&', jm
        ));
        if (c.contains("giveit."+us)) {
        } else {
            p.getInventory().addItem(i1);
            p.getInventory().addItem(i3);
            p.getInventory().addItem(i4);
            p.getInventory().addItem(i5);
            p.getInventory().addItem(i6);
            p.getInventory().addItem(i7);
            p.getInventory().addItem(i8);
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes(
                    '&', c1.getString("cn.ZeroEngine.Engine.api.v1.JoQMess.NewPlayerJoin").replace("{player}", n)
            ));
            c.set("giveit."+us, n);
            c.save(file);
        }
    }
    @EventHandler
    public void QuitMessage(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String n = p.getName();

        FileConfiguration c = plugin.getConfig();

        String qm = c.getString("cn.ZeroEngine.Engine.api.v1.JoQMess.qm").replace("{player}",n);
        if (qm == null) {
            plugin.getLogger().warning("在~/SFServerPlugin/config.yml.cn.ZeroEngine.Engine.api.v1.JoQMess.qm的地方为null！！！");
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes(
                '&', qm
        ));
    }
}
