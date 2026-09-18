package cn.ZeroEngine.Engine.api.v3.feature.crop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.Collections;
import java.util.List;

/**
 * 自定义农作物基类（继承 SItem，物品形式即种子）。
 *
 * 作物方块用 vanilla Ageable Material（WHEAT/CARROTS/POTATOES/BEETROOTS 等），
 * 通过 chunk PDC 标记 cropId 区分不同自定义作物。生长沿用原版随机刻，
 * 成熟后右键收获掉落产物与种子。
 *
 * 用法：
 *   public class TomatoCrop extends SCrop {
 *       @Override public String id() { return "tomato"; }
 *       @Override public String displayName() { return "§c番茄种子"; }
 *       @Override public Material material() { return Material.WHEAT_SEEDS; }   // 物品形式（种子）
 *       @Override public Material cropBlock() { return Material.WHEAT; }        // 方块形式
 *       @Override public List<ItemStack> harvestDrops() {
 *           return List.of(new ItemStack(Material.APPLE, 2));                   // 成熟产物
 *       }
 *   }
 *
 *   sf.crops().register(new TomatoCrop());
 */
public abstract class SCrop extends SItem {

    public abstract Material cropBlock();

    public java.util.List<Material> stages() { return Collections.emptyList(); }

    public final boolean isStageMode() { return stages() != null && !stages().isEmpty(); }

    public int maxStage() {
        if (isStageMode()) return stages().size() - 1;
        return 7;
    }

    public double growthChance() { return 0.125; }

    public int minSeedsOnHarvest() { return 1; }

    public int maxSeedsOnHarvest() { return 3; }

    public List<ItemStack> harvestDrops() { return Collections.emptyList(); }

    public boolean requireFarmland() { return true; }

    public int minLightLevel() { return 9; }

    public boolean onBonemeal(Block block) { return true; }

    public void onPlant(Block block, Player player) {}

    public void onGrow(Block block, int newStage) {}

    public void onHarvest(Block block, Player player) {}

    public boolean placeAt(Block target, int stage) {
        if (target == null || !target.getType().isAir()) return false;
        if (isStageMode()) {
            int idx = Math.max(0, Math.min(stage, stages().size() - 1));
            target.setType(stages().get(idx));
            return true;
        }
        Material m = cropBlock();
        if (m == null || !m.isBlock()) return false;
        target.setType(m);
        try {
            if (target.getBlockData() instanceof Ageable ageable) {
                ageable.setAge(Math.max(0, Math.min(stage, ageable.getMaximumAge())));
                target.setBlockData(ageable);
            }
        } catch (Throwable ignore) {}
        return true;
    }

    public int currentStage(Block block) {
        if (block == null) return 0;
        if (isStageMode()) {
            Material cur = block.getType();
            java.util.List<Material> st = stages();
            for (int i = 0; i < st.size(); i++) if (st.get(i) == cur) return i;
            return 0;
        }
        try {
            if (block.getBlockData() instanceof Ageable ageable) {
                return ageable.getAge();
            }
        } catch (Throwable ignore) {}
        return 0;
    }

    public boolean isMature(Block block) {
        return currentStage(block) >= maxStage();
    }

    public boolean growOneStep(Block block) {
        if (block == null) return false;
        if (isStageMode()) {
            int cur = currentStage(block);
            int max = maxStage();
            if (cur >= max) return false;
            block.setType(stages().get(cur + 1));
            onGrow(block, cur + 1);
            return true;
        }
        try {
            if (block.getBlockData() instanceof Ageable ageable) {
                int cur = ageable.getAge();
                int max = Math.min(ageable.getMaximumAge(), maxStage());
                if (cur >= max) return false;
                ageable.setAge(cur + 1);
                block.setBlockData(ageable);
                onGrow(block, cur + 1);
                return true;
            }
        } catch (Throwable ignore) {}
        return false;
    }

    public boolean canGrowAt(Block block) {
        if (block == null) return false;
        if (requireFarmland()) {
            if (block.getRelative(BlockFace.DOWN).getType() != Material.FARMLAND) return false;
        }
        if (minLightLevel() > 0) {
            try {
                if (block.getLightLevel() < minLightLevel()) return false;
            } catch (Throwable ignore) {}
        }
        return true;
    }
}
