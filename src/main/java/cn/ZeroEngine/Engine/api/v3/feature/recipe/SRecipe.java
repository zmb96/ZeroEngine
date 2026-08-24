package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;

import java.util.*;

/**
 * 自定义配方抽象基类
 *
 * 用法：
 *   public class MyRecipe extends SRecipe {
 *       @Override public String id() { return "magic_scepter"; }
 *       @Override public RecipeMode mode() { return RecipeMode.SHAPED; }
 *       @Override public List<String> shape() { return Arrays.asList(" E ","GBG"," D "); }
 *       @Override public Map<Character, Object> ingredients() {
 *           return Map.of(
 *               'E', Material.ENDER_EYE,            // 原版物品
 *               'G', Material.GOLD_INGOT,
 *               'B', new BlazeRodItem(),             // 自定义物品（会走 ExactChoice 精确匹配）
 *               'D', Material.DIAMOND
 *           );
 *       }
 *       @Override public Object result() { return new MagicScepterItem(); } // 自定义物品作为产物
 *       @Override public int resultAmount() { return 1; }
 *   }
 *
 *   sf.recipes().register(new MyRecipe());
 */
public abstract class SRecipe {

    private static Plugin plugin;

    public static void init(Plugin p) { plugin = p; }

    // ==================== 必须实现 ====================

    /** 唯一标识（Bukkit Recipe 的 NamespacedKey 后半段） */
    public abstract String id();

    /** SHAPED = 有序配方（需要 shape()）；SHAPELESS = 无序配方（只需 ingredients） */
    public abstract RecipeMode mode();

    /**
     * 合成材料映射表。value 支持两种类型：
     *   - org.bukkit.Material → 原版物品（匹配任意同 Material 物品）
     *   - cn.ZeroEngine.Engine.api.v3.feature.item.SItem 实例 → 自定义物品（走 ExactChoice 精确匹配，必须带完整 PDC）
     *
     * 注：有序配方用 Character 做 key；无序配方 key 用任意字母也可以，实际只统计每种 ingredient 的份数。
     */
    public abstract Map<Character, Object> ingredients();

    /**
     * 合成产物。支持：
     *   - org.bukkit.Material → 原版物品（枚举）
     *   - cn.ZeroEngine.Engine.api.v3.feature.item.SItem 实例 → 自定义物品（create(resultAmount())）
     */
    public abstract Object result();

    /** 产物数量（默认 1） */
    public int resultAmount() { return 1; }

    /** 有序配方的形状（3 行字符串，每行 1~3 个字符）；无序配方忽略 */
    public List<String> shape() { return Collections.emptyList(); }

    /** 铁砧/砂轮等是否允许在合成台上解锁 —— 默认 true */
    public boolean unlockedByDefault() { return true; }

    public enum RecipeMode { SHAPED, SHAPELESS }

    // ==================== 内部工具 ====================

    /** 把 Object（Material / SItem）转成对应的 RecipeChoice */
    public static RecipeChoice choiceOf(Object ingredient) {
        if (ingredient instanceof Material m) {
            return new RecipeChoice.MaterialChoice(m);
        }
        if (ingredient instanceof SItem s) {
            ItemStack exact = s.create(1);
            return new RecipeChoice.ExactChoice(Collections.singletonList(exact));
        }
        throw new IllegalArgumentException("Unknown ingredient type: " + (ingredient == null ? "null" : ingredient.getClass().getName()));
    }

    /** 把 result Object（Material / SItem）转成 ItemStack */
    public ItemStack resultItem() {
        Object r = result();
        int amount = Math.max(1, resultAmount());
        if (r instanceof Material m) {
            return new ItemStack(m, amount);
        }
        if (r instanceof SItem s) {
            return s.create(amount);
        }
        throw new IllegalArgumentException("Unknown result type: " + (r == null ? "null" : r.getClass().getName()));
    }

    /** 把本实例转换成 Bukkit Recipe；对有序配方会自动校验 shape */
    public Recipe toBukkitRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "sf_" + id());
        ItemStack out = resultItem();
        Map<Character, Object> ingMap = ingredients();

        if (mode() == RecipeMode.SHAPED) {
            List<String> rows = shape();
            if (rows == null || rows.isEmpty() || rows.size() > 3) {
                throw new IllegalStateException("Recipe " + id() + ": shape must be 1..3 non-empty rows");
            }
            ShapedRecipe recipe = new ShapedRecipe(key, out);
            recipe.shape(rows.toArray(new String[0]));
            Set<Character> used = new HashSet<>();
            for (String row : rows) {
                for (int i = 0; i < row.length(); i++) {
                    char c = row.charAt(i);
                    if (c == ' ') continue;
                    if (used.add(c) && !ingMap.containsKey(c)) {
                        throw new IllegalStateException("Recipe " + id() + ": shape char '" + c + "' missing from ingredients map");
                    }
                }
            }
            for (Map.Entry<Character, Object> e : ingMap.entrySet()) {
                recipe.setIngredient(e.getKey(), choiceOf(e.getValue()));
            }
            return recipe;
        } else {
            ShapelessRecipe recipe = new ShapelessRecipe(key, out);
            for (Object ing : ingMap.values()) {
                recipe.addIngredient(choiceOf(ing));
            }
            return recipe;
        }
    }
}
