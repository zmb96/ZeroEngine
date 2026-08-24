package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.SF;

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

    public SRecipe get(String id) { return registry.get(id); }

    public Collection<SRecipe> all() { return Collections.unmodifiableCollection(registry.values()); }

    public NamespacedKey getKey(String id) { return registeredKeys.get(id); }

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
