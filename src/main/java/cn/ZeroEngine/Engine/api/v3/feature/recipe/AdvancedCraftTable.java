package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import cn.ZeroEngine.Engine.api.v3.feature.gui.SChestGUI;

public abstract class AdvancedCraftTable {

    public abstract String id();

    
    public Material baseBlock() { return Material.CRAFTING_TABLE; }

    
    public Material bottomBlock() { return Material.BARREL; }

    public SChestGUI onRightChest() { return null; }

    public SChestGUI craftGUI(RecipeManager manager) { return null; }

    public void onOpenChest(Player player, Block workbench, Block dispenser, Inventory dispenserInv) {}

    public void onCraft(Player player, Block workbench, Block dispenser, SRecipe recipe) {}

    public boolean allowDefaultCraft() { return baseBlock() == Material.CRAFTING_TABLE; }
}
