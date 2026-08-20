package cn.ZeroEngine.Engine.api.v1;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

@Deprecated
public class BuyTagCommand implements CommandExecutor {

    private final main plugin;

    public BuyTagCommand(main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家才能使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();

        // ========== 无参数：显示帮助和标签列表 ==========
        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "========== 🏷️ 标签商店 ==========");
            player.sendMessage(ChatColor.YELLOW + "/buytag <标签名> " + ChatColor.WHITE + "- 购买标签");
            player.sendMessage(ChatColor.YELLOW + "/tags " + ChatColor.WHITE + "- 查看所有标签");
            player.sendMessage("");

            // 显示当前标签
            String currentTag = config.getString("chat.player-tags." + player.getName());
            if (currentTag != null) {
                String display = config.getString("chat.tags." + currentTag, currentTag);
                player.sendMessage(ChatColor.GRAY + "当前标签: " + ChatColor.translateAlternateColorCodes('&', display));
            } else {
                player.sendMessage(ChatColor.GRAY + "当前标签: 无");
            }
            return true;
        }

        // ========== 处理购买 ==========
        String tagName = args[0];
        purchaseTag(player, tagName);

        return true;
    }

    /**
     * 购买标签
     */
    public void purchaseTag(Player player, String tagName) {
        FileConfiguration config = plugin.getConfig();

        // ========== 1. 检查标签是否存在 ==========
        if (!config.contains("chat.tags." + tagName)) {
            player.sendMessage(ChatColor.RED + "❌ 标签 '" + tagName + "' 不存在！");
            player.sendMessage(ChatColor.GRAY + "输入 /tags 查看所有可用标签");
            return;
        }

        // ========== 2. 检查是否已经拥有该标签 ==========
        String currentTag = config.getString("chat.player-tags." + player.getName());
        if (tagName.equals(currentTag)) {
            player.sendMessage(ChatColor.YELLOW + "⚠️ 你已经拥有这个标签了！");
            return;
        }

        // ========== 3. 获取价格 ==========
        double price = config.getDouble("chat.tags." + tagName, 0);

        // ========== 4. 免费标签直接给予 ==========
        if (price <= 0) {
            giveTag(player, tagName);
            player.sendMessage(ChatColor.GREEN + "✅ 你已获得免费标签！");
            return;
        }

        // ========== 5. 检查经济系统 ==========
        Economy economy = main.eco();
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "❌ 经济系统未启用！");
            return;
        }

        // ========== 6. 检查余额 ==========
        if (!economy.has(player, price)) {
            double balance = economy.getBalance(player);
            player.sendMessage(ChatColor.RED + "❌ 金币不足！");
            player.sendMessage(ChatColor.GRAY + "需要: " + ChatColor.GOLD + price + " 金币");
            player.sendMessage(ChatColor.GRAY + "拥有: " + ChatColor.GOLD + balance + " 金币");
            return;
        }

        // ========== 7. 扣费 ==========
        EconomyResponse response = economy.withdrawPlayer(player, price);

        if (response.transactionSuccess()) {
            // 扣费成功，给予标签
            giveTag(player, tagName);
            player.sendMessage(ChatColor.GREEN + "✅ 购买成功！花费 " + ChatColor.GOLD + price + " 金币");
            player.sendMessage(ChatColor.GRAY + "剩余余额: " + economy.getBalance(player));
        } else {
            player.sendMessage(ChatColor.RED + "❌ 购买失败: " + response.errorMessage);
        }
    }

    /**
     * 给予玩家标签
     */
    private void giveTag(Player player, String tagName) {
        FileConfiguration config = plugin.getConfig();

        // 保存到配置文件
        config.set("chat.player-tags." + player.getName(), tagName);
        plugin.saveConfig();

        // 显示标签
        String tagDisplay = config.getString("cn.ZeroEngine.Engine.api.v1.PlayerChat.tags." + tagName, tagName);
        String coloredTag = ChatColor.translateAlternateColorCodes('&', tagDisplay);

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "🏷️ 当前标签: " + coloredTag);
        player.sendMessage(ChatColor.GRAY + "聊天时会自动显示这个标签！");
    }
}