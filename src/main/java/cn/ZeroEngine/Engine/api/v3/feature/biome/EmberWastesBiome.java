package cn.ZeroEngine.Engine.api.v3.feature.biome;

import cn.ZeroEngine.Engine.api.v3.main.PerlinNoise;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 炽焰荒原（Ember Wastes）—— 演示一个「完全不像原版」的自定义生物群系
 *
 * <p>核心思路：选一个视觉相近的底子（NETHER_WASTES = 红色天空 + 烟雾），
 * 然后在 chunk 生成时用 PerlinNoise 大量改造地表方块、撒岩浆池、放火、
 * 撒玄武岩尖刺，让玩家几乎认不出原版的「下界荒地」底子。</p>
 *
 * <p>群系特色：</p>
 * <ul>
 *   <li>仅在主世界（"world"）的沙漠（DESERT）和平原（PLAINS）上替换</li>
 *   <li>视觉底子用 NETHER_WASTES（红色天空 + 烟雾粒子）</li>
 *   <li>用 PerlinNoise fbm 2D 决定地表高度起伏（±5 格）</li>
 *   <li>用 PerlinNoise 3D 决定岩浆池位置（噪声 > 0.7 = 岩浆）</li>
 *   <li>地表全部覆盖为下界岩（NETHERRACK）+ 黑石（BLACKSTONE）混合</li>
 *   <li>随机撒 1-3 根玄武岩尖刺（高度 4-8 格）</li>
 *   <li>5% 概率放营火（带火）作为「祭坛」装饰</li>
 *   <li>玩家进入：上火抗性 + 速度 II + 警告消息 + 雷鸣音效</li>
 *   <li>每秒持续效果：玩家脚下冒岩浆滴粒子 + 烟雾粒子</li>
 *   <li>玩家离开：清效果 + 告别消息</li>
 * </ul>
 *
 * <p>注册方式：</p>
 * <pre>
 *   cn.ZeroEngine.Engine.api.v3.SF.init(this);
 *   cn.ZeroEngine.Engine.api.v3.SF.sf().biomes()
 *        .register(new EmberWastesBiome());
 * </pre>
 *
 * @author ZeroEngine
 * @since 3.2.7-LTS
 */
public class EmberWastesBiome extends SBiome {

    // ==================== 基础元信息 ====================

    @Override
    public String id() {
        return "ember_wastes";  // PDC 用，必须 [a-z0-9_]
    }

    @Override
    public String displayName() {
        return "§4§l炽焰荒原";
    }

    @Override
    public Biome targetBiome() {
        // 选 NETHER_WASTES 作视觉底子：红色天空 + 烟雾环境粒子
        // 但地表方块会在 onChunkPopulate 里全部覆盖，玩家几乎认不出原版
        return Biome.NETHER_WASTES;
    }

    // ==================== 分布条件 ====================

    @Override
    public List<String> worlds() {
        return Arrays.asList("world");  // 主世界中出现（让玩家不需要去下界就能体验）
    }

    @Override
    public List<Biome> replaces() {
        // 只在原版沙漠和平原上替换（这些地方地表开阔，适合改造成荒原）
        return Arrays.asList(Biome.DESERT, Biome.PLAINS);
    }

    @Override
    public double weight() {
        return 1.2;  // 略高优先级，与其他可能竞争的 SBiome 相比胜出
    }

    @Override
    public double noiseThreshold() {
        return 0.65;  // 约 35% 匹配的 chunk 替换，稀有但成片
    }

    @Override
    public double noiseScale() {
        return 0.03;  // 大块连续区域（约 33 chunk 一个周期）
    }

    @Override
    public int noiseOctaves() {
        return 5;  // 5 层 fbm，让地表起伏更自然
    }

    @Override
    public double noisePersistence() {
        return 0.55;  // 略高 persistence，保留更多高频细节
    }

    @Override
    public int[] yRange() {
        // 只覆盖主世界地表层（不让地下也变成下界岩）
        return new int[]{55, 100};
    }

    // ==================== chunk 生成（核心改造）====================

    @Override
    public void onChunkPopulate(World world, int chunkX, int chunkZ, Random random) {
        // === 1. 改造地表：把草/沙换成下界岩 + 黑石 ===
        // 用 PerlinNoise 决定每格是下界岩（80%）还是黑石（20%）
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = chunkX * 16 + x;
                int wz = chunkZ * 16 + z;
                int y = world.getHighestBlockYAt(wx, wz);
                if (y < 55) continue;  // 太低不动

                // 跳过空气和水（保留地形）
                Block top = world.getBlockAt(wx, y, wz);
                if (top.getType() == Material.AIR) {
                    // 找地表块
                    while (y > 0 && world.getBlockAt(wx, y, wz).getType() == Material.AIR) y--;
                    if (y <= 0) continue;
                    top = world.getBlockAt(wx, y, wz);
                }
                // 不替换石头/矿物层（只改最表层）
                if (top.getType() == Material.STONE
                        || top.getType() == Material.DEEPSLATE
                        || top.getType() == Material.BEDROCK) continue;

                // 用 3D 噪声决定地表块类型
                double n3 = sampleNoise3D(wx, y, wz);  // [0, 1]
                if (n3 > 0.7) {
                    // 岩浆池（噪声极高 = 岩浆点）
                    world.getBlockAt(wx, y + 1, wz).setType(Material.LAVA);
                    top.setType(Material.LAVA);
                } else if (n3 > 0.5) {
                    top.setType(Material.BLACKSTONE);  // 黑石
                } else {
                    top.setType(Material.NETHERRACK);  // 下界岩（占多数）
                }

                // === 2. 概率放火 ===
                // 紧邻岩浆的格子上放火（让荒原有零星火苗）
                if (n3 > 0.7 && n3 < 0.72 && random.nextDouble() < 0.3) {
                    Block above = world.getBlockAt(wx, y + 2, wz);
                    if (above.getType() == Material.AIR) {
                        above.setType(Material.FIRE);
                    }
                }
            }
        }

        // === 3. 撒玄武岩尖刺（1-3 根）===
        int spikeCount = 1 + random.nextInt(3);
        for (int i = 0; i < spikeCount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int baseY = world.getHighestBlockYAt(wx, wz);
            if (baseY < 55) continue;

            int height = 4 + random.nextInt(5);  // 高度 4-8
            // 柱状玄武岩
            for (int h = 0; h < height; h++) {
                world.getBlockAt(wx, baseY + 1 + h, wz).setType(Material.BASALT);
            }
            // 顶部放岩浆块（发光效果）
            world.getBlockAt(wx, baseY + 1 + height, wz).setType(Material.MAGMA_BLOCK);
        }

        // === 4. 5% 概率放营火「祭坛」===
        if (random.nextDouble() < 0.05) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int y = world.getHighestBlockYAt(wx, wz) + 1;
            Block b = world.getBlockAt(wx, y, wz);
            if (b.getType() == Material.AIR) {
                b.setType(Material.CAMPFIRE);
                // 让营火上方冒烟（带 SoulCampfire 不需要侧风）
                // 注：Campfire 默认就冒烟
            }
        }

        // === 5. 极低概率（0.5%）撒「炽焰之核」宝箱 ===
        if (random.nextDouble() < 0.005) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int wx = chunkX * 16 + x;
            int wz = chunkZ * 16 + z;
            int y = world.getHighestBlockYAt(wx, wz) + 1;
            world.getBlockAt(wx, y, wz).setType(Material.CHEST);
            if (world.getBlockAt(wx, y, wz).getState() instanceof org.bukkit.block.Chest chest) {
                var inv = chest.getBlockInventory();
                inv.setItem(13, new org.bukkit.inventory.ItemStack(Material.BLAZE_POWDER, 5 + random.nextInt(10)));
                inv.setItem(11, new org.bukkit.inventory.ItemStack(Material.MAGMA_CREAM, 3 + random.nextInt(5)));
                inv.setItem(15, new org.bukkit.inventory.ItemStack(Material.FIRE_CHARGE, 2 + random.nextInt(8)));
                chest.update();
            }
        }
    }

    // ==================== 玩家交互 ====================

    @Override
    public void onPlayerEnter(Player player, Chunk chunk) {
        player.sendMessage("§4§l┌─────────────────────────────────┐");
        player.sendMessage("§4§l│ §c你踏入了炽焰荒原... §4§l          │");
        player.sendMessage("§4§l│ §7『大地焦黑，岩浆在地表流淌。』 §4§l│");
        player.sendMessage("§4§l└─────────────────────────────────┘");

        // 上火抗性 + 速度 II（让玩家能在岩浆池间跳跃）
        PotionEffectType fireRes = Registry.EFFECT.get(NamespacedKey.minecraft("fire_resistance"));
        if (fireRes != null) {
            player.addPotionEffect(new PotionEffect(fireRes, 1200, 0, false, false, true));
        }
        PotionEffectType speed = Registry.EFFECT.get(NamespacedKey.minecraft("speed"));
        if (speed != null) {
            player.addPotionEffect(new PotionEffect(speed, 1200, 1, false, false, true));
        }

        // 雷鸣音效
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
    }

    @Override
    public void onPlayerLeave(Player player, Chunk chunk) {
        player.sendMessage("§7你走出了炽焰荒原...");
        // 清效果（如果是本插件加的）
        PotionEffectType fireRes = Registry.EFFECT.get(NamespacedKey.minecraft("fire_resistance"));
        PotionEffectType speed = Registry.EFFECT.get(NamespacedKey.minecraft("speed"));
        if (fireRes != null) player.removePotionEffect(fireRes);
        if (speed != null) player.removePotionEffect(speed);
    }

    @Override
    public void onPerSecond(Player player, Location loc) {
        // 脚下冒岩浆滴粒子 + 头顶冒烟雾
        loc.getWorld().spawnParticle(Particle.DRIPPING_LAVA,
                loc.clone().add(0, 0.1, 0), 3, 0.5, 0.1, 0.5, 0);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                loc.clone().add(0, 1.5, 0), 2, 0.3, 0.5, 0.3, 0.01);

        // 每 10 秒（1/10 概率）在附近随机放火苗（让环境持续燃烧）
        if (Math.random() < 0.1) {
            int dx = (int) (Math.random() * 6 - 3);
            int dz = (int) (Math.random() * 6 - 3);
            Block target = loc.getWorld().getBlockAt(
                    loc.getBlockX() + dx,
                    loc.getBlockY() - 1,
                    loc.getBlockZ() + dz);
            Block above = loc.getWorld().getBlockAt(
                    target.getX(), target.getY() + 1, target.getZ());
            // 只在下界岩/黑石/玄武岩上放火
            if ((target.getType() == Material.NETHERRACK
                    || target.getType() == Material.BLACKSTONE
                    || target.getType() == Material.BASALT)
                    && above.getType() == Material.AIR) {
                above.setType(Material.FIRE);
            }
        }
    }
}
