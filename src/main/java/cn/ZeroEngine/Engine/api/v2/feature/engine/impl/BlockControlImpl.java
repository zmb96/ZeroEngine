package cn.ZeroEngine.Engine.api.v2.feature.engine.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.feature.engine.BlockControl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class BlockControlImpl implements BlockControl, Listener {

    private final JavaPlugin plugin;
    private final Map<Material, Float> breakSpeeds = new ConcurrentHashMap<>();
    private final Map<Material, Float> blastResistances = new ConcurrentHashMap<>();
    private final Map<Material, DropEntry> drops = new ConcurrentHashMap<>();
    private final Map<Material, int[]> expDrops = new ConcurrentHashMap<>();
    private final Map<Material, BiFunction<Player, Block, Boolean>> breakHandlers = new ConcurrentHashMap<>();
    private final Set<Material> requireTools = ConcurrentHashMap.newKeySet();
    private final Map<Material, Material> replaceOnBreak = new ConcurrentHashMap<>();
    private final Set<Location> noUpdateBlocks = ConcurrentHashMap.newKeySet();

    private record DropEntry(ItemStack item, float chance) {}

    public BlockControlImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void setBreakSpeed(Material material, float speed) { breakSpeeds.put(material, speed); }

    @Override
    public float getBreakSpeed(Material material) { return breakSpeeds.getOrDefault(material, -1f); }

    @Override
    public void resetBreakSpeed(Material material) { breakSpeeds.remove(material); }

    @Override
    public void setBlastResistance(Material material, float resistance) { blastResistances.put(material, resistance); }

    @Override
    public float getBlastResistance(Material material) { return blastResistances.getOrDefault(material, material.getBlastResistance()); }

    @Override
    public void resetBlastResistance(Material material) { blastResistances.remove(material); }

    @Override
    public void setDrop(Material material, ItemStack drop, float chance) { drops.put(material, new DropEntry(drop, chance)); }

    @Override
    public ItemStack getDrop(Material material) {
        DropEntry e = drops.get(material);
        return e != null ? e.item : null;
    }

    @Override
    public void resetDrop(Material material) { drops.remove(material); }

    @Override
    public void setExpDrop(Material material, int minExp, int maxExp) { expDrops.put(material, new int[]{minExp, maxExp}); }

    @Override
    public void resetExpDrop(Material material) { expDrops.remove(material); }

    @Override
    public void registerBreakHandler(Material material, BiFunction<Player, Block, Boolean> handler) { breakHandlers.put(material, handler); }

    @Override
    public void unregisterBreakHandler(Material material) { breakHandlers.remove(material); }

    @Override
    public void setRequireTool(Material material, boolean requireTool) {
        if (requireTool) requireTools.add(material);
        else requireTools.remove(material);
    }

    @Override
    public boolean isRequireTool(Material material) { return requireTools.contains(material); }

    @Override
    public void setReplaceOnBreak(Material material, Material replaceWith) { replaceOnBreak.put(material, replaceWith); }

    @Override
    public void cancelBlockUpdate(Location location, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    noUpdateBlocks.add(location.clone().add(x, y, z));
                }
            }
        }
    }

    @Override
    public Map<Material, Float> getModifiedBreakSpeeds() { return Collections.unmodifiableMap(breakSpeeds); }

    @Override
    public Map<Material, Float> getModifiedBlastResistances() { return Collections.unmodifiableMap(blastResistances); }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material mat = block.getType();
        Player player = e.getPlayer();

        if (requireTools.contains(mat)) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.getType() == Material.AIR || !tool.getType().name().endsWith("_PICKAXE")) {
                if (mat.name().contains("STONE") || mat.name().contains("ORE")) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        BiFunction<Player, Block, Boolean> handler = breakHandlers.get(mat);
        if (handler != null && !handler.apply(player, block)) {
            e.setCancelled(true);
            return;
        }

        DropEntry drop = drops.get(mat);
        if (drop != null) {
            e.setDropItems(false);
            if (Math.random() < drop.chance) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop.item);
            }
        }

        int[] exp = expDrops.get(mat);
        if (exp != null) {
            e.setExpToDrop(exp[0] + (int)(Math.random() * (exp[1] - exp[0] + 1)));
        }

        Material replace = replaceOnBreak.get(mat);
        if (replace != null) {
            e.getBlock().setType(replace);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent e) {
        if (noUpdateBlocks.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}
