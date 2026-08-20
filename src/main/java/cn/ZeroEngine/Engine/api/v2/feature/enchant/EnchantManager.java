package cn.ZeroEngine.Engine.api.v2.feature.enchant;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.*;

public class EnchantManager {

    private final Map<String, SEnchantment> enchants = new HashMap<>();

    public EnchantManager register(SEnchantment enchant) {
        SF sf = SF.sf();
        String id = enchant.id();
        if (enchants.containsKey(id)) {
            throw new IllegalStateException("Enchant already registered: " + id);
        }
        enchants.put(id, enchant);
        sf.info("[Enchant] Registered: " + enchant.namespace() + ":" + id + " (" + enchant.displayName() + ")");
        return this;
    }

    public EnchantManager registerAll(SEnchantment... enchants) {
        for (SEnchantment e : enchants) register(e);
        return this;
    }

    public void unregister(String id) { enchants.remove(id); }

    public void unregisterAll() { enchants.clear(); }

    public SEnchantment get(String id) { return enchants.get(id); }

    public Collection<SEnchantment> all() { return Collections.unmodifiableCollection(enchants.values()); }

    public Map<SEnchantment, Integer> getOn(ItemStack item) {
        Map<SEnchantment, Integer> result = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return result;
        for (SEnchantment e : enchants.values()) {
            int lvl = e.getLevel(item);
            if (lvl > 0) result.put(e, lvl);
        }
        return result;
    }

    public boolean has(ItemStack item, String id) {
        SEnchantment e = get(id);
        return e != null && e.isOn(item);
    }

    public int level(ItemStack item, String id) {
        SEnchantment e = get(id);
        return e == null ? 0 : e.getLevel(item);
    }

    public ItemStack apply(ItemStack item, SEnchantment enchant, int level) {
        if (item == null || !enchant.canEnchantItem(item)) return item;
        for (Map.Entry<SEnchantment, Integer> e : getOn(item).entrySet()) {
            if (!e.getKey().id().equals(enchant.id()) && enchant.conflictsWith(e.getKey())) return item;
        }
        ItemStack result = item.clone();
        enchant.setLevel(result, level);
        return result;
    }

    public ItemStack apply(ItemStack item, String id, int level) {
        SEnchantment e = get(id);
        return e == null ? item : apply(item, e, level);
    }

    public ItemStack remove(ItemStack item, String id) {
        SEnchantment e = get(id);
        if (e == null || item == null) return item;
        ItemStack result = item.clone();
        e.removeFrom(result);
        return result;
    }

    public List<SEnchantment> applicableTo(ItemStack item) {
        List<SEnchantment> list = new ArrayList<>();
        if (item == null) return list;
        for (SEnchantment e : enchants.values()) if (e.canEnchantItem(item)) list.add(e);
        return list;
    }

    public List<SEnchantment> conflictsOf(SEnchantment enchant) {
        List<SEnchantment> list = new ArrayList<>();
        for (SEnchantment e : enchants.values()) {
            if (!e.id().equals(enchant.id()) && enchant.conflictsWith(e)) list.add(e);
        }
        return list;
    }

    public ItemStack createBook(String id, int level) {
        SEnchantment e = get(id);
        return e == null ? null : createBook(e, level);
    }

    public ItemStack createBook(SEnchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "附魔书");
            enchant.applyLore(meta, level);
            book.setItemMeta(meta);
        }
        enchant.setLevel(book, level);
        return book;
    }

    public ItemStack createBook(String id) {
        SEnchantment e = get(id);
        return e == null ? null : createBook(e, e.maxLevel());
    }

    public void giveBook(Player player, String id, int level) {
        SF sf = SF.sf();
        ItemStack book = createBook(id, level);
        if (book == null) return;
        player.getInventory().addItem(book).forEach((idx, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        sf.msg(player, "§a获得附魔书: " + get(id).displayName() + " " + SEnchantment.roman(level));
    }

    public void giveBook(Player player, String id) {
        SEnchantment e = get(id);
        if (e == null) return;
        giveBook(player, id, e.maxLevel());
    }
}
