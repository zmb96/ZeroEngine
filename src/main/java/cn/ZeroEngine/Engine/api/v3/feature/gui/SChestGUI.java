package cn.ZeroEngine.Engine.api.v3.feature.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class SChestGUI {

    public static ItemStack named(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(color(name));
            if (lore != null && lore.length > 0) {
                List<String> colored = new ArrayList<>(lore.length);
                for (String s : lore) colored.add(color(s));
                meta.setLore(colored);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String color(String s) {
        return s == null ? null : s.replace('&', '§');
    }

    public abstract String id();

    public String title() { return "容器"; }

    public int size() { return 27; }

    public boolean readonly() { return false; }

    public String command() { return null; }

    public void build(Builder b) {}

    public void onClick(ChestGUI.ClickContext ctx) {}

    public void onOpen(Player player) {}

    public void onClose(Player player) {}

    public final void open(Player player) {
        GUIManager gm = cn.ZeroEngine.Engine.api.v3.SF.sf().gui();
        ChestGUI gui = gm.create(title(), size() / 9, readonly());
        build(new Builder(gui));
        gui.onOpen(this::onOpen);
        gui.onClose(this::onClose);
        gui.onAnyClick(this::onClick);
        gui.open(player);
    }

    public static final class Builder {
        private final ChestGUI gui;
        Builder(ChestGUI gui) { this.gui = gui; }

        public Builder item(int slot, ItemStack item) { gui.item(slot, item); return this; }
        public Builder item(int row, int col, ItemStack item) { gui.item(row, col, item); return this; }
        public Builder item(int slot, ItemStack item, java.util.function.Consumer<ChestGUI.ClickContext> onClick) {
            gui.item(slot, item, onClick); return this;
        }
        public Builder item(int slot, org.bukkit.Material mat, String name, java.util.function.Consumer<ChestGUI.ClickContext> onClick, String... lore) {
            gui.item(slot, mat, name, onClick, lore); return this;
        }
        public Builder item(int slot, org.bukkit.Material mat, String name, String... lore) {
            gui.item(slot, mat, name, lore); return this;
        }
        public Builder item(int row, int col, org.bukkit.Material mat, String name, String... lore) {
            gui.item(row, col, mat, name, lore); return this;
        }
        public Builder fill(ItemStack item) { gui.fill(item); return this; }
        public Builder fill(org.bukkit.Material mat, String name, String... lore) { gui.fill(mat, name, lore); return this; }
        public Builder border(ItemStack item) { gui.border(item); return this; }
        public Builder border(org.bukkit.Material mat, String name, String... lore) { gui.border(mat, name, lore); return this; }
        public Builder fillRange(int start, int end, ItemStack item) { gui.fillRange(start, end, item); return this; }
        public Builder clear(int slot) { gui.clear(slot); return this; }
        public Builder clear() { gui.clear(); return this; }
        public Builder pagination(java.util.List<ItemStack> items, int perPage) { gui.pagination(items, perPage); return this; }
        public ChestGUI gui() { return gui; }
    }
}
