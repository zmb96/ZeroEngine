package cn.ZeroEngine.Engine.api.v3.feature.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ChestGUI {

    interface ClickContext {
        Player player();
        int slot();
        int row();
        int col();
        ItemStack cursor();
        ItemStack current();
        boolean isShiftClick();
        boolean isRightClick();
        ClickType type();
        ChestGUI gui();
        default void cursor(ItemStack item) {}
        default void cancelled(boolean v) {}
    }

    enum ClickType {
        LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, MIDDLE, DOUBLE_CLICK, DROP, NUMBER_KEY
    }

    ChestGUI title(String title);

    ChestGUI rows(int rows);

    ChestGUI size(int slots);

    ChestGUI item(int slot, ItemStack item);

    ChestGUI item(int row, int col, ItemStack item);

    default ChestGUI item(int slot, Material mat, String name, String... lore) {
        return item(slot, itemBuilder(mat, name, lore));
    }

    default ChestGUI item(int row, int col, Material mat, String name, String... lore) {
        return item(row, col, itemBuilder(mat, name, lore));
    }

    ChestGUI item(int slot, ItemStack item, Consumer<ClickContext> onClick);

    ChestGUI item(int row, int col, ItemStack item, Consumer<ClickContext> onClick);

    default ChestGUI item(int slot, Material mat, String name, Consumer<ClickContext> onClick, String... lore) {
        return item(slot, itemBuilder(mat, name, lore), onClick);
    }

    default ChestGUI item(int row, int col, Material mat, String name, Consumer<ClickContext> onClick, String... lore) {
        return item(row, col, itemBuilder(mat, name, lore), onClick);
    }

    ChestGUI fill(ItemStack item);

    ChestGUI fill(Material mat, String name, String... lore);

    ChestGUI border(ItemStack item);

    ChestGUI border(Material mat, String name, String... lore);

    ChestGUI fillRange(int startSlot, int endSlot, ItemStack item);

    ChestGUI fillRange(int startRow, int startCol, int endRow, int endCol, ItemStack item);

    ChestGUI clear(int slot);

    ChestGUI clear();

    ChestGUI onClose(Consumer<Player> onClose);

    ChestGUI onOpen(Consumer<Player> onOpen);

    ChestGUI onAnyClick(Consumer<ClickContext> onAnyClick);

    ChestGUI readonly(boolean readonly);

    ChestGUI pagination(List<ItemStack> items, int itemsPerPage, Function<Integer, ItemStack> navBuilder);

    ChestGUI pagination(List<ItemStack> items, int itemsPerPage);

    ChestGUI nextPage(ItemStack navItem);

    ChestGUI prevPage(ItemStack navItem);

    ChestGUI page(int page);

    int currentPage();

    int totalPages();

    ChestGUI refresh();

    ChestGUI refresh(Player viewer);

    void open(Player player);

    void close(Player player);

    void closeAll();

    String title();

    int size();

    ItemStack[] contents();

    boolean isReadonly();

    static ItemStack itemBuilder(Material mat, String name, String... lore) {
        var item = new ItemStack(mat);
        var meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null && lore.length > 0) meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
