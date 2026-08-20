package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class giveit implements CommandExecutor {

    private final main plugin;

    public giveit(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = ((Player) sender);
        UUID u = ((Player) sender).getUniqueId();
        String us = u.toString();
        File folder = plugin.getDataFolder();
        ItemStack i1 = new ItemStack(Material.IRON_INGOT, 10);
        ItemStack i2 = new ItemStack(Material.NETHERITE_SCRAP, 3);
        ItemStack i3 = new ItemStack(Material.DIAMOND, 5);
        ItemStack i4 = new ItemStack(Material.LAPIS_LAZULI, 10);
        ItemStack i5 = new ItemStack(Material.EMERALD, 10);
        ItemStack i6 = new ItemStack(Material.LAVA_BUCKET, 1);
        ItemStack i7 = new ItemStack(Material.WATER_BUCKET, 1);
        ItemStack i8 = new ItemStack(Material.OAK_SAPLING, 5);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(plugin.getDataFolder(), "giveit.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(file);

        if (c.contains("giveit."+us)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&', "&c&l你已经领取过了！"
            ));
            return true;
        }

        c.set("giveit."+us, sender.getName());
        try {
            c.save(file);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c数据保存失败，请联系管理员！"));
            e.printStackTrace();
        }
        p.getInventory().addItem(i1);
        p.getInventory().addItem(i2);
        p.getInventory().addItem(i3);
        p.getInventory().addItem(i4);
        p.getInventory().addItem(i5);
        p.getInventory().addItem(i6);
        p.getInventory().addItem(i7);
        p.getInventory().addItem(i8);

        sender.sendMessage(ChatColor.translateAlternateColorCodes(
                '&', "&a你以获得物资，请打开背包查看！"
        ));
        return true;
    }
}
