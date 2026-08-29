package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import cn.ZeroEngine.Engine.api.v3.feature.gui.SChestGUI;

/**
 * 高级工作台（多方块结构：工作台 + 下方发射器）抽象基类。
 *
 * 默认行为：右键工作台 → 打开下方发射器原版 GUI（玩家放/取材料）。
 * 重写 {@link #onRightChest()} 返回 {@link SChestGUI} 子类实例 → 打开自定义 GUI
 * （加工机器 UI，配合 {@link SChestGUI#command()} 还可用命令打开）。
 *
 * 用法：
 *   public class MillMachine extends AdvancedCraftTable {
 *       @Override public String id() { return "mill"; }
 *       @Override public SChestGUI onRightChest() { return new MillGui(); }
 *   }
 *
 *   sf.recipes().registerTable(new MillMachine(), workbenchBlock);
 */
public abstract class AdvancedCraftTable {

    public abstract String id();

    public SChestGUI onRightChest() { return null; }

    public void onOpenChest(Player player, Block workbench, Block dispenser, Inventory dispenserInv) {}

    public void onCraft(Player player, Block workbench, Block dispenser, SRecipe recipe) {}

    public boolean allowDefaultCraft() { return true; }
}
