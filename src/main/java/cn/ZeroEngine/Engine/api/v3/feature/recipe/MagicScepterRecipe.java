package cn.ZeroEngine.Engine.api.v3.feature.recipe;

import org.bukkit.Material;
import cn.ZeroEngine.Engine.api.v3.feature.item.MagicScepterItem;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 魔法权杖合成配方（演示 SRecipe 用法）
 *
 *     E              - E = 末影之眼
 *    GBG             - G = 金锭；B = 烈焰棒
 *     D              - D = 钻石
 *
 * 产物：MagicScepterItem（自定义物品）x 1
 */
public class MagicScepterRecipe extends SRecipe {

    @Override public String id() { return "magic_scepter"; }

    @Override public RecipeMode mode() { return RecipeMode.SHAPED; }

    @Override
    public List<String> shape() {
        return Arrays.asList(
                " E ",
                "GBG",
                " D "
        );
    }

    @Override
    public Map<Character, Object> ingredients() {
        // 用 LinkedHashMap 保证展示顺序（info 命令里按序列出）
        Map<Character, Object> map = new LinkedHashMap<>();
        map.put('E', Material.ENDER_EYE);
        map.put('G', Material.GOLD_INGOT);
        map.put('B', Material.BLAZE_ROD);
        map.put('D', Material.DIAMOND);
        return map;
    }

    @Override
    public Object result() { return new MagicScepterItem(); }

    @Override
    public int resultAmount() { return 1; }
}
