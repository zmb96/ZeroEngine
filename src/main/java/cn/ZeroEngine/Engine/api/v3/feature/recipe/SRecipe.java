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

public abstract class SRecipe {

    private static Plugin plugin;

    public static void init(Plugin p) { plugin = p; }

    

    
    public abstract String id();

    
    public abstract RecipeMode mode();

    
    public abstract Map<Character, Object> ingredients();

    
    public abstract Object result();

    
    public int resultAmount() { return 1; }

    
    public List<String> shape() { return Collections.emptyList(); }

    
    public boolean unlockedByDefault() { return true; }

    
    public boolean advancedOnly() { return false; }

    public enum RecipeMode { SHAPED, SHAPELESS }

    

    
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

            int sMinR = 3, sMaxR = -1, sMinC = 3, sMaxC = -1;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (rows.get(r).charAt(c) != ' ') {
                        if (r < sMinR) sMinR = r;
                        if (r > sMaxR) sMaxR = r;
                        if (c < sMinC) sMinC = c;
                        if (c > sMaxC) sMaxC = c;
                    }
                }
            }
            if (sMaxR < 0) return MatchResult.FAIL;

            int gMinR = 3, gMaxR = -1, gMinC = 3, gMaxC = -1;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (gridChars[r][c] != ' ') {
                        if (r < gMinR) gMinR = r;
                        if (r > gMaxR) gMaxR = r;
                        if (c < gMinC) gMinC = c;
                        if (c > gMaxC) gMaxC = c;
                    }
                }
            }
            if (gMaxR < 0) return MatchResult.FAIL;

            int sH = sMaxR - sMinR + 1;
            int sW = sMaxC - sMinC + 1;
            int gH = gMaxR - gMinR + 1;
            int gW = gMaxC - gMinC + 1;
            if (sH != gH || sW != gW) return MatchResult.FAIL;

            List<Integer> consume = new ArrayList<>();
            for (int r = 0; r < sH; r++) {
                String shapeRow = rows.get(sMinR + r);
                for (int c = 0; c < sW; c++) {
                    char shapeChar = shapeRow.charAt(sMinC + c);
                    char gridChar = gridChars[gMinR + r][gMinC + c];
                    if (shapeChar == ' ') {
                        if (gridChar != ' ') return MatchResult.FAIL;
                        continue;
                    }
                    if (shapeChar != gridChar) return MatchResult.FAIL;
                    consume.add((gMinR + r) * 3 + (gMinC + c));
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
