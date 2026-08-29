package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义配方注册中心
 *
 * 注册：sf.recipes().register(new MyRecipe())
 * 查找：sf.recipes().get("my_recipe")
 * 列出：sf.recipes().all()
 *
 * 通过 `Bukkit.addRecipe()` 注册到原版工作台 —— 玩家直接在工作台就能合成。
 * 自定义物品作 ingredient 时用 `RecipeChoice.ExactChoice` 精确匹配 PDC/lore。
 */
public class RecipeManager {

    private final Map<String, SRecipe> registry = new HashMap<>();
    private final Map<String, NamespacedKey> registeredKeys = new ConcurrentHashMap<>();
    private final Map<String, AdvancedCraftTable> tables = new HashMap<>();
    private final Map<String, NamespacedKey> tableKeyCache = new ConcurrentHashMap<>();
    private ItemManager itemManager;
    private Plugin plugin;

    /** 注册一个配方（立即调用 Bukkit.addRecipe），成功 true，失败（重复 id 或 Bukkit 拒绝）false */
    public boolean register(SRecipe r) {
        SF sf = SF.sf();
        String id = r.id();
        if (registry.containsKey(id)) {
            sf.warn("[Recipe] already registered: " + id);
            return false;
        }
        try {
            Recipe bukkit = r.toBukkitRecipe();
            boolean ok = Bukkit.addRecipe(bukkit, r.unlockedByDefault());
            if (!ok) {
                sf.warn("[Recipe] Bukkit.addRecipe returned false for: " + id);
                return false;
            }
            NamespacedKey k = new NamespacedKey(SF.sf().plugin(), "sf_" + id);
            registry.put(id, r);
            registeredKeys.put(id, k);
            sf.info("[Recipe] registered: " + id + " (" + r.mode() + " -> " + formatResult(r) + ")");
            return true;
        } catch (Throwable t) {
            sf.error("[Recipe] register failed: " + id, t);
            return false;
        }
    }

    public RecipeManager registerAll(SRecipe... recipes) {
        for (SRecipe r : recipes) register(r);
        return this;
    }

    public void setItemManager(ItemManager im) { this.itemManager = im; this.plugin = SF.sf().plugin(); }
    public ItemManager itemManager() { return itemManager; }

    public SRecipe get(String id) { return registry.get(id); }

    public Collection<SRecipe> all() { return Collections.unmodifiableCollection(registry.values()); }

    public NamespacedKey getKey(String id) { return registeredKeys.get(id); }

    public AdvancedCraftTable registerTable(AdvancedCraftTable table) {
        if (tables.containsKey(table.id())) {
            throw new IllegalStateException("AdvancedCraftTable already registered: " + table.id());
        }
        tables.put(table.id(), table);
        SF.sf().info("[Recipe] AdvancedCraftTable registered: " + table.id());
        return table;
    }

    public boolean registerTableIfAbsent(AdvancedCraftTable table) {
        if (tables.containsKey(table.id())) return false;
        try { registerTable(table); return true; }
        catch (IllegalStateException ignore) { return false; }
    }

    public AdvancedCraftTable getTable(String id) { return tables.get(id); }

    public Collection<AdvancedCraftTable> allTables() { return Collections.unmodifiableCollection(tables.values()); }

    public boolean bindTableAt(Block workbench, AdvancedCraftTable table) {
        if (plugin == null) plugin = SF.sf().plugin();
        try {
            Chunk chunk = workbench.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            pdc.set(tableKey(workbench), PersistentDataType.STRING, table.id());
            return true;
        } catch (Throwable t) {
            SF.sf().error("[Recipe] bindTableAt failed", t);
            return false;
        }
    }

    public AdvancedCraftTable findTableAt(Block workbench) {
        if (workbench == null) return null;
        try {
            Chunk chunk = workbench.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            String id = pdc.get(tableKey(workbench), PersistentDataType.STRING);
            if (id == null) return null;
            return tables.get(id);
        } catch (Throwable ignore) {
            return null;
        }
    }

    public boolean unbindTableAt(Block workbench) {
        try {
            Chunk chunk = workbench.getChunk();
            PersistentDataContainer pdc = chunk.getPersistentDataContainer();
            if (pdc.get(tableKey(workbench), PersistentDataType.STRING) == null) return false;
            pdc.remove(tableKey(workbench));
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    private NamespacedKey tableKey(Block b) {
        String k = "sfact_" + (b.getX() & 15) + "_" + (b.getY() & 15) + "_" + (b.getZ() & 15);
        return tableKeyCache.computeIfAbsent(k, key -> new NamespacedKey(plugin, key));
    }

    /** 从 Bukkit 移除一个自定义配方；同时清空注册表记录 */
    public boolean remove(String id) {
        NamespacedKey k = registeredKeys.get(id);
        if (k == null) return false;
        Bukkit.removeRecipe(k);
        registeredKeys.remove(id);
        registry.remove(id);
        SF.sf().info("[Recipe] removed: " + id);
        return true;
    }

    public SRecipe.MatchResult tryCraftAtGrid(ItemStack[] grid) {
        if (grid == null || grid.length < 9) return SRecipe.MatchResult.FAIL;
        ItemManager im = itemManager;
        if (im == null) {
            try { im = SF.sf().item(); } catch (Throwable ignore) { im = null; }
        }
        for (SRecipe r : registry.values()) {
            SRecipe.MatchResult mr = r.matchesGrid(grid, im);
            if (mr.matched) return mr;
        }
        return SRecipe.MatchResult.FAIL;
    }

    public SRecipe craftAtInventory(Inventory inv) {
        if (inv == null) return null;
        ItemStack[] contents = inv.getContents();
        if (contents == null || contents.length < 9) return null;
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) grid[i] = contents[i];

        SRecipe.MatchResult mr = tryCraftAtGrid(grid);
        if (!mr.matched || mr.recipe == null) return null;

        for (int slot : mr.consumeSlots) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null) continue;
            if (stack.getAmount() <= 1) {
                inv.setItem(slot, null);
            } else {
                ItemStack copy = stack.clone();
                copy.setAmount(copy.getAmount() - 1);
                inv.setItem(slot, copy);
            }
        }

        int firstEmpty = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack cur = inv.getItem(i);
            if (cur == null || cur.getType().isAir()) { firstEmpty = i; break; }
        }
        int targetSlot = firstEmpty >= 0 ? firstEmpty : 0;
        inv.setItem(targetSlot, mr.result);
        SF.sf().info("[Recipe] Advanced Craft Table: crafted '" + mr.recipe.id() + "' -> slot " + targetSlot);
        return mr.recipe;
    }

    /** 清空所有已注册配方（Bukkit 层一并移除） */
    public void unregisterAll() {
        for (String id : new ArrayList<>(registeredKeys.keySet())) remove(id);
    }

    private String formatResult(SRecipe r) {
        Object res = r.result();
        if (res instanceof Enum<?> m) return m.name();
        if (res instanceof cn.ZeroEngine.Engine.api.v3.feature.item.SItem s) return "SItem[" + s.id() + "]";
        return String.valueOf(res);
    }

    /** 插件生命周期：Server 重启 / reload 前，调用本方法刷新 Bukkit 中的配方（通常不需要，除非 DataPack 热重载） */
    public void resetRecipes(Plugin plugin) {
        SF sf = SF.sf();
        int n = 0;
        for (NamespacedKey k : registeredKeys.values()) {
            Bukkit.removeRecipe(k);
            n++;
        }
        for (SRecipe r : registry.values()) {
            try {
                Bukkit.addRecipe(r.toBukkitRecipe(), r.unlockedByDefault());
            } catch (Throwable t) {
                sf.warn("[Recipe] reset re-add failed: " + r.id() + " -> " + t.getMessage());
            }
        }
        sf.info("[Recipe] reset " + n + " recipes -> Bukkit");
    }
}
