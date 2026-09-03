package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;
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

    /**
     * 是否仅限高级工作台（AdvancedCraftTable）合成。
     * 默认 false —— 注册时同时注册到 Bukkit 原版工作台。
     * 子类重写返回 true 时，RecipeManager.register() 跳过 Bukkit.addRecipe()，
     * 仅加入内部 registry 供高级工作台 matchesGrid() 匹配，普通工作台无法合成。
     */
    public boolean advancedOnly() { return false; }

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
            int maxLen = 0;
            for (String row : rows) {
                if (row.length() > maxLen) maxLen = row.length();
            }
            if (maxLen > 3) {
                throw new IllegalStateException("Recipe " + id() + ": shape row width must be <= 3, got " + maxLen);
            }
            boolean needPad = false;
            for (String row : rows) {
                if (row.length() != maxLen) { needPad = true; break; }
            }
            if (needPad) {
                StringBuilder sb = new StringBuilder();
                sb.append("Recipe ").append(id()).append(": shape rows have unequal length; auto-padding with spaces to ").append(maxLen).append(" chars -> [");
                List<String> padded = new ArrayList<>(rows.size());
                for (int i = 0; i < rows.size(); i++) {
                    String row = rows.get(i);
                    StringBuilder r = new StringBuilder(row);
                    while (r.length() < maxLen) r.append(' ');
                    padded.add(r.toString());
                    if (i > 0) sb.append(", ");
                    sb.append("'").append(r.toString()).append("'");
                }
                sb.append("]");
                cn.ZeroEngine.Engine.api.v3.SF.sf().warn("[Recipe] " + sb);
                rows = padded;
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

    public static final class MatchResult {
        public final boolean matched;
        public final List<Integer> consumeSlots;
        public final ItemStack result;
        public final SRecipe recipe;
        public static final MatchResult FAIL = new MatchResult(false, Collections.emptyList(), null, null);
        public MatchResult(boolean matched, List<Integer> consumeSlots, ItemStack result, SRecipe recipe) {
            this.matched = matched;
            this.consumeSlots = consumeSlots;
            this.result = result;
            this.recipe = recipe;
        }
    }

    public MatchResult matchesGrid(ItemStack[] grid, ItemManager im) {
        if (grid == null || grid.length < 9) return MatchResult.FAIL;
        ItemStack[] g = new ItemStack[9];
        for (int i = 0; i < 9; i++) g[i] = grid[i];
        Map<Character, Object> ingMap = ingredients();

        if (mode() == RecipeMode.SHAPED) {
            List<String> rows = new ArrayList<>(shape());
            if (rows.isEmpty()) return MatchResult.FAIL;
            while (rows.size() < 3) rows.add("   ");
            for (int i = 0; i < rows.size(); i++) {
                String r = rows.get(i);
                StringBuilder sb = new StringBuilder(r);
                while (sb.length() < 3) sb.append(' ');
                if (sb.length() > 3) return MatchResult.FAIL;
                rows.set(i, sb.toString());
            }

            char[][] gridChars = new char[3][3];
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                ItemStack item = g[i];
                if (item == null || item.getType().isAir()) {
                    gridChars[row][col] = ' ';
                } else {
                    char matched = 0;
                    for (Map.Entry<Character, Object> e : ingMap.entrySet()) {
                        if (itemMatchesIngredient(item, e.getValue(), im)) {
                            matched = e.getKey();
                            break;
                        }
                    }
                    if (matched == 0) return MatchResult.FAIL;
                    gridChars[row][col] = matched;
                }
            }

            List<Integer> consume = new ArrayList<>();
            for (int row = 0; row < 3; row++) {
                String shapeRow = rows.get(row);
                for (int col = 0; col < 3; col++) {
                    char shapeChar = shapeRow.charAt(col);
                    char gridChar = gridChars[row][col];
                    if (shapeChar == ' ' && gridChar == ' ') continue;
                    if (shapeChar != gridChar) return MatchResult.FAIL;
                    consume.add(row * 3 + col);
                }
            }
            return new MatchResult(true, consume, resultItem(), this);
        } else {
            List<ItemStack> gridItems = new ArrayList<>();
            List<Integer> gridSlots = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                if (g[i] != null && !g[i].getType().isAir()) {
                    gridItems.add(g[i]);
                    gridSlots.add(i);
                }
            }
            List<Object> ingValues = new ArrayList<>(ingMap.values());
            if (gridItems.size() != ingValues.size()) return MatchResult.FAIL;
            boolean[] used = new boolean[ingValues.size()];
            for (int i = 0; i < gridItems.size(); i++) {
                int foundIdx = -1;
                for (int j = 0; j < ingValues.size(); j++) {
                    if (used[j]) continue;
                    if (itemMatchesIngredient(gridItems.get(i), ingValues.get(j), im)) {
                        foundIdx = j;
                        break;
                    }
                }
                if (foundIdx < 0) return MatchResult.FAIL;
                used[foundIdx] = true;
            }
            return new MatchResult(true, gridSlots, resultItem(), this);
        }
    }

    private boolean itemMatchesIngredient(ItemStack item, Object ingredient, ItemManager im) {
        if (ingredient instanceof Material m) {
            return item.getType() == m;
        }
        if (ingredient instanceof SItem s) {
            if (im == null) return false;
            SItem found = im.find(item);
            return found != null && found.id().equals(s.id());
        }
        return false;
    }
}
