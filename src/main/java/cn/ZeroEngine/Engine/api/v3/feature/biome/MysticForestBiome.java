package cn.ZeroEngine.Engine.api.v3.feature.biome;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 神秘森林 —— 演示一个完整自定义生物群系的所有重写点
 *
 * <p>行为：</p>
 * <ul>
 *   <li>只在主世界（"world"）的 PLAINS / FOREST 上替换</li>
 *   <li>替换为原版的 DARK_FOREST（视觉底子 = 深色草地 + 黑橡木）</li>
 *   <li>用 PerlinNoise 阈值 0.6（约 40% 匹配 chunk 替换）</li>
 *   <li>每个被替换的 chunk，地表 4-8 格概率放置发光浆果装饰</li>
 *   <li>玩家进入时：发欢迎消息 + 上发光效果（防迷路）</li>
 *   <li>玩家离开时：清发光效果 + 提示</li>
 *   <li>每秒持续效果：给玩家上饱食度 +1（神秘森林的馈赠）</li>
 * </ul>
 *
 * <p>测试方法：</p>
 * <ol>
 *   <li>在插件 onEnable 里：{@code sf.biomes().register(new MysticForestBiome());}</li>
 *   <li>启动服务器，加载一个存档</li>
 *   <li>玩家走出已探索区域 → 服务器开始生成新 chunk → 自动应用 MysticForestBiome</li>
 *   <li>用 F3 看 biome 标签会显示 dark_forest，但 chunk 已被 PDC 标记为 mystic_forest
 *       —— onPlayerEnter / onPerSecond 等钩子会在 MysticForestBiome 范围内触发</li>
 * </ol>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
public class MysticForestBiome extends SBiome {

    // ==================== 基础元信息 ====================

    @Override
    public String id() {
        return "mystic_forest";  // 必须是 [a-z0-9_]，用于 PDC
    }

    @Override
    public String displayName() {
        return "§5§l神秘森林";
    }

    @Override
    public Biome targetBiome() {
        // 用原版 DARK_FOREST 作为视觉底子：
        //   - 草色偏深紫
        //   - 树木密度高
        //   - 玩家 F3 看到 "dark_forest"
        return Biome.DARK_FOREST;
    }

    // ==================== 分布条件 ====================

    @Override
    public List<String> worlds() {
        return Arrays.asList("world");  // 仅主世界
    }

    @Override
    public List<Biome> replaces() {
        // 只在原版 PLAINS 和 FOREST 上替换（不在沙漠、雪地等替换）
        return Arrays.asList(Biome.PLAINS, Biome.FOREST);
    }

    @Override
    public double weight() {
        return 1.0;  // 单一 SBiome 时无竞争
    }

    @Override
    public double noiseThreshold() {
        return 0.4;  // 约 40% 匹配的 chunk 会被替换
    }

    @Override
    public double noiseScale() {
        return 0.04;  // 每 25 个 chunk 一个周期，分布较碎
    }

    @Override
    public int[] yRange() {
        // 只覆盖地表层（Y=60 到 Y=200），地下保持原 biome
        return new int[]{60, 200};
    }

    // ==================== 自定义内容钩子 ====================

    /**
     * 新区块生成完成时触发
     * 在地表随机放发光浆果 + 凋零玫瑰装饰
     */
    @Override
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        // 1) 在地表随机放 4-8 朵发光浆果
        int flowerCount = 4 + random.nextInt(5);
        for (int i = 0; i < flowerCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z);
            if (y < world.getMinHeight()) continue;
            // 只在草、土、灰化土上放
            Material ground = world.getBlockAt(x, y, z).getType();
            if (ground == Material.GRASS_BLOCK || ground == Material.DIRT
                    || ground == Material.PODZOL || ground == Material.COARSE_DIRT) {
                // 上方放发光浆果
                world.getBlockAt(x, y + 1, z).setType(Material.GLOW_BERRIES);
            }
        }

        // 2) 概率放置凋零玫瑰（黑色花朵，稀有装饰）
        if (random.nextDouble() < 0.15) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z);
            Material ground = world.getBlockAt(x, y, z).getType();
            if (ground == Material.GRASS_BLOCK || ground == Material.DIRT) {
                world.getBlockAt(x, y + 1, z).setType(Material.WITHER_ROSE);
            }
        }

        // 3) 极低概率（1%）放一个宝箱（装有钻石 + 经验瓶）
        if (random.nextDouble() < 0.01) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = world.getHighestBlockYAt(x, z) + 1;
            world.getBlockAt(x, y, z).setType(Material.CHEST);
            var state = world.getBlockAt(x, y, z).getState();
            if (state instanceof org.bukkit.block.Chest chest) {
                var inv = chest.getBlockInventory();
                inv.setItem(13, new ItemStack(Material.DIAMOND, 1 + random.nextInt(2)));
                inv.setItem(15, new ItemStack(Material.EXPERIENCE_BOTTLE, 3 + random.nextInt(5)));
                chest.update();
            }
        }
    }

    /**
     * 玩家进入神秘森林时触发
     */
    @Override
    public void onPlayerEnter(Player player, Chunk chunk) {
        player.sendMessage("§5§l你进入了神秘森林...");
        player.sendMessage("§7『传说这里藏有发光浆果和凋零玫瑰。』");

        // 上发光效果（防迷路）
        var glow = org.bukkit.Registry.EFFECT.get(
                org.bukkit.NamespacedKey.minecraft("glowing"));
        if (glow != null) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    glow, 600, 0, false, false, true));
        }

        // 播放神秘音效（用稳定 Sound 字段，避免 1.21+ 移除/重命名问题）
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 0.5f);
    }

    /**
     * 玩家离开时触发
     */
    @Override
    public void onPlayerLeave(Player player, Chunk chunk) {
        player.sendMessage("§7你离开了神秘森林...");
        // 移除发光效果（如果是本插件加的）
        player.removePotionEffect(org.bukkit.Registry.EFFECT.get(
                org.bukkit.NamespacedKey.minecraft("glowing")));
    }

    /**
     * 每秒持续效果：给玩家上饱食度 +1（神秘森林的馈赠）
     */
    @Override
    public void onPerSecond(Player player, Location loc) {
        if (player.getFoodLevel() < 20) {
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
        }
        // 每 5 秒（概率 1/5）随机生成 1 个紫色粒子装饰
        if (Math.random() < 0.2) {
            loc.getWorld().spawnParticle(org.bukkit.Particle.DUST,
                    loc.clone().add(0, 1, 0), 1, 0.5, 1, 0.5, 0,
                    new org.bukkit.Particle.DustOptions(
                            org.bukkit.Color.fromRGB(160, 80, 255), 1.5f));
        }
    }

    // ==================== 注册 ====================

    /**
     * 在你的插件 onEnable 里调用：
     *
     * <pre>
     *   cn.ZeroEngine.Engine.api.v3.SF.init(this);
     *   cn.ZeroEngine.Engine.api.v3.SF.sf().biomes()
     *        .register(new MysticForestBiome());
     * </pre>
     *
     * 之后玩家走出已探索区域，服务器开始生成新 chunk 时：
     *   - 原 PLAINS / FOREST 的 chunk 有约 40% 概率被替换为 MysticForestBiome
     *   - 替换后的 chunk 在客户端显示为 dark_forest（深色草地 + 黑橡木）
     *   - chunk 内会自动生成发光浆果、凋零玫瑰装饰
     *   - 玩家进入此区域会收到欢迎消息 + 发光效果
     *   - 每秒玩家在该区域内会获得 +1 饱食度
     */
}
