package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import cn.ZeroEngine.Engine.api.v3.feature.gui.SChestGUI;

/**
 * 自定义多方块加工机器抽象基类。
 *
 * 支持 3 种结构（顶方块 + 下方发射器）：
 *   - CRAFTING_TABLE + DISPENSER  = 普通/高级工作台
 *   - GRINDSTONE    + DISPENSER  = 磨坊
 *   - FURNACE       + DISPENSER  = 灶台
 *
 * 结构识别优先用 RecipeManager.bindTableAt() 的 PDC 绑定；
 * 未绑定时按「顶方块 Material」与机器 baseBlock() 做默认映射。
 *
 * 打开逻辑：
 *   - onRightChest() 返回 SChestGUI 实例 → 打开自定义 GUI（加工机器 UI）
 *   - 返回 null → 打开下方发射器原版 GUI（玩家放/取材料）
 *   - allowDefaultCraft()==true 的普通工作台：没绑定且没 GUI → 直接 craftAtInventory 走合成
 */
public abstract class AdvancedCraftTable {

    public abstract String id();

    /** 该机器使用的「顶部方块」Material（用于默认结构识别） */
    public Material baseBlock() { return Material.CRAFTING_TABLE; }

    /** 下方支持的方块 Material（用于结构识别），默认发射器 */
    public Material bottomBlock() { return Material.DISPENSER; }

    public SChestGUI onRightChest() { return null; }

    public void onOpenChest(Player player, Block workbench, Block dispenser, Inventory dispenserInv) {}

    public void onCraft(Player player, Block workbench, Block dispenser, SRecipe recipe) {}

    public boolean allowDefaultCraft() { return baseBlock() == Material.CRAFTING_TABLE; }
}
