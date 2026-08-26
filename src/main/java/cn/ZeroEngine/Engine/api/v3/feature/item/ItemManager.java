package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;

public class ItemManager {

    private final Map<String, SItem> items = new HashMap<>();

    public ItemManager register(SItem item) {
        SF sf = SF.sf();
        String id = item.id();
        if (items.containsKey(id)) {
            throw new IllegalStateException("Item already registered: " + id);
        }
        items.put(id, item);
        sf.info("[Item] Registered: " + id + " (" + item.displayName() + ")");
        return this;
    }

    public boolean registerIfAbsent(SItem item) {
        if (items.containsKey(item.id())) return false;
        try {
            register(item);
            return true;
        } catch (IllegalStateException ignore) {
            return false;
        }
    }

    public ItemManager registerAll(SItem... items) {
        for (SItem i : items) register(i);
        return this;
    }

    public void unregister(String id) { items.remove(id); }

    public void unregisterAll() { items.clear(); }

    public SItem get(String id) { return items.get(id); }

    public Collection<SItem> all() { return Collections.unmodifiableCollection(items.values()); }

    public SItem find(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        for (SItem i : items.values()) {
            if (i.is(item)) return i;
        }
        return null;
    }

    public boolean isCustom(ItemStack item) {
        return find(item) != null;
    }

    public boolean has(Player player, String id) {
        SItem item = get(id);
        if (item == null) return false;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (item.is(invItem)) return true;
        }
        return false;
    }

    public int count(Player player, String id) {
        SItem item = get(id);
        if (item == null) return 0;
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (item.is(invItem)) count += invItem.getAmount();
        }
        return count;
    }

    public void give(Player player, String id, int amount) {
        SF sf = SF.sf();
        SItem item = get(id);
        if (item == null) return;
        ItemStack stack = item.create(amount);
        player.getInventory().addItem(stack).forEach((idx, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        sf.msg(player, "§a获得 " + item.displayName() + " x" + amount);
    }

    public void give(Player player, String id) {
        give(player, id, 1);
    }

    public void consume(Player player, String id, int amount) {
        SItem item = get(id);
        if (item == null) return;
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack invItem = contents[i];
            if (invItem != null && item.is(invItem)) {
                int stackAmount = invItem.getAmount();
                if (stackAmount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    invItem.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    public void consume(Player player, String id) {
        consume(player, id, 1);
    }

    public ItemStack create(String id, int amount) {
        SItem item = get(id);
        return item == null ? null : item.create(amount);
    }

    public ItemStack create(String id) {
        return create(id, 1);
    }
}
