package cn.ZeroEngine.Engine.api.v2.feature.engine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.BiFunction;

public interface BlockControl {

    void setBreakSpeed(Material material, float speed);

    float getBreakSpeed(Material material);

    void resetBreakSpeed(Material material);

    void setBlastResistance(Material material, float resistance);

    float getBlastResistance(Material material);

    void resetBlastResistance(Material material);

    void setDrop(Material material, ItemStack drop, float chance);

    ItemStack getDrop(Material material);

    void resetDrop(Material material);

    void setExpDrop(Material material, int minExp, int maxExp);

    void resetExpDrop(Material material);

    void registerBreakHandler(Material material, BiFunction<Player, Block, Boolean> handler);

    void unregisterBreakHandler(Material material);

    void setRequireTool(Material material, boolean requireTool);

    boolean isRequireTool(Material material);

    void setReplaceOnBreak(Material material, Material replaceWith);

    void cancelBlockUpdate(Location location, int radius);

    Map<Material, Float> getModifiedBreakSpeeds();

    Map<Material, Float> getModifiedBlastResistances();
}
