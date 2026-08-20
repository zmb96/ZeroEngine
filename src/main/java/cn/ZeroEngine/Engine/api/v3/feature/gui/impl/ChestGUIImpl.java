package cn.ZeroEngine.Engine.api.v3.feature.gui.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v3.feature.gui.ChestGUI;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChestGUIImpl implements ChestGUI, Listener {

    private final Plugin plugin;
    private String title = "ChestGUI";
    private int slots = 27;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, Consumer<ClickContext>> clickHandlers = new HashMap<>();
    private Consumer<Player> openHandler;
    private Consumer<Player> closeHandler;
    private Consumer<ClickContext> anyClickHandler;
    private boolean readonly = true;

    private final Set<Inventory> trackedInventories = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<UUID, Inventory> viewerInventories = new HashMap<>();

    private List<ItemStack> paginationItems;
    private int itemsPerPage = 0;
    private int currentPage = 0;
    private ItemStack prevNavItem;
    private ItemStack nextNavItem;
    private int prevNavSlot = -1;
    private int nextNavSlot = -1;

    public ChestGUIImpl(Plugin plugin) {
        this.plugin = plugin;
        this.prevNavItem = ChestGUI.itemBuilder(Material.ARROW, "§a上一页");
        this.nextNavItem = ChestGUI.itemBuilder(Material.ARROW, "§a下一页");
    }

    @Override
    public ChestGUI title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ChestGUI rows(int rows) {
        this.slots = Math.max(9, Math.min(54, rows * 9));
        return this;
    }

    @Override
    public ChestGUI size(int slots) {
        this.slots = Math.max(9, Math.min(54, (slots / 9) * 9));
        if (slots % 9 != 0) this.slots = Math.min(54, ((slots / 9) + 1) * 9);
        return this;
    }

    @Override
    public ChestGUI item(int slot, ItemStack item) {
        items.put(slot, item);
        return this;
    }

    @Override
    public ChestGUI item(int row, int col, ItemStack item) {
        return item(row * 9 + col, item);
    }

    @Override
    public ChestGUI item(int slot, ItemStack item, Consumer<ClickContext> onClick) {
        items.put(slot, item);
        clickHandlers.put(slot, onClick);
        return this;
    }

    @Override
    public ChestGUI item(int row, int col, ItemStack item, Consumer<ClickContext> onClick) {
        return item(row * 9 + col, item, onClick);
    }

    @Override
    public ChestGUI fill(ItemStack item) {
        for (int i = 0; i < slots; i++) {
            if (!items.containsKey(i)) items.put(i, item);
        }
        return this;
    }

    @Override
    public ChestGUI fill(Material mat, String name, String... lore) {
        return fill(ChestGUI.itemBuilder(mat, name, lore));
    }

    @Override
    public ChestGUI border(ItemStack item) {
        for (int row = 0; row < slots / 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 0 || row == slots / 9 - 1 || col == 0 || col == 8) {
                    int slot = row * 9 + col;
                    if (!items.containsKey(slot)) items.put(slot, item);
                }
            }
        }
        return this;
    }

    @Override
    public ChestGUI border(Material mat, String name, String... lore) {
        return border(ChestGUI.itemBuilder(mat, name, lore));
    }

    @Override
    public ChestGUI fillRange(int startSlot, int endSlot, ItemStack item) {
        for (int i = startSlot; i <= endSlot && i < slots; i++) {
            if (!items.containsKey(i)) items.put(i, item);
        }
        return this;
    }

    @Override
    public ChestGUI fillRange(int startRow, int startCol, int endRow, int endCol, ItemStack item) {
        return fillRange(startRow * 9 + startCol, endRow * 9 + endCol, item);
    }

    @Override
    public ChestGUI clear(int slot) {
        items.remove(slot);
        clickHandlers.remove(slot);
        return this;
    }

    @Override
    public ChestGUI clear() {
        items.clear();
        clickHandlers.clear();
        return this;
    }

    @Override
    public ChestGUI onClose(Consumer<Player> onClose) {
        this.closeHandler = onClose;
        return this;
    }

    @Override
    public ChestGUI onOpen(Consumer<Player> onOpen) {
        this.openHandler = onOpen;
        return this;
    }

    @Override
    public ChestGUI onAnyClick(Consumer<ClickContext> onAnyClick) {
        this.anyClickHandler = onAnyClick;
        return this;
    }

    @Override
    public ChestGUI readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    @Override
    public ChestGUI pagination(List<ItemStack> items, int itemsPerPage, Function<Integer, ItemStack> navBuilder) {
        this.paginationItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.itemsPerPage = itemsPerPage;
        this.prevNavItem = navBuilder != null ? navBuilder.apply(-1) : this.prevNavItem;
        this.nextNavItem = navBuilder != null ? navBuilder.apply(1) : this.nextNavItem;
        applyPagination();
        return this;
    }

    @Override
    public ChestGUI pagination(List<ItemStack> items, int itemsPerPage) {
        return pagination(items, itemsPerPage, null);
    }

    @Override
    public ChestGUI nextPage(ItemStack navItem) {
        this.nextNavItem = navItem;
        return this;
    }

    @Override
    public ChestGUI prevPage(ItemStack navItem) {
        this.prevNavItem = navItem;
        return this;
    }

    @Override
    public ChestGUI page(int page) {
        this.currentPage = Math.max(0, Math.min(page, totalPages() - 1));
        applyPagination();
        return this;
    }

    @Override
    public int currentPage() {
        return currentPage;
    }

    @Override
    public int totalPages() {
        if (paginationItems == null || itemsPerPage <= 0) return 1;
        return Math.max(1, (int) Math.ceil((double) paginationItems.size() / itemsPerPage));
    }

    @Override
    public ChestGUI refresh() {
        for (Inventory inv : new ArrayList<>(trackedInventories)) {
            rebuildInventory(inv);
        }
        return this;
    }

    @Override
    public ChestGUI refresh(Player viewer) {
        Inventory inv = viewerInventories.get(viewer.getUniqueId());
        if (inv != null) rebuildInventory(inv);
        return this;
    }

    @Override
    public void open(Player player) {
        Inventory inv = createInventory();
        rebuildInventory(inv);
        trackedInventories.add(inv);
        viewerInventories.put(player.getUniqueId(), inv);
        if (openHandler != null) {
            try { openHandler.accept(player); } catch (Exception ignored) {}
        }
        player.openInventory(inv);
    }

    @Override
    public void close(Player player) {
        Inventory inv = viewerInventories.remove(player.getUniqueId());
        if (inv != null) trackedInventories.remove(inv);
        player.closeInventory();
    }

    @Override
    public void closeAll() {
        for (UUID id : new ArrayList<>(viewerInventories.keySet())) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) close(p);
        }
        trackedInventories.clear();
        viewerInventories.clear();
    }

    @Override
    public String title() { return title; }

    @Override
    public int size() { return slots; }

    @Override
    public ItemStack[] contents() {
        ItemStack[] arr = new ItemStack[slots];
        for (var e : items.entrySet()) {
            if (e.getKey() < slots) arr[e.getKey()] = e.getValue();
        }
        return arr;
    }

    @Override
    public boolean isReadonly() { return readonly; }

    public void unregister() {
        HandlerList.unregisterAll(this);
        closeAll();
    }

    private void applyPagination() {
        for (int i = 0; i < slots; i++) {
            items.remove(i);
            clickHandlers.remove(i);
        }
        if (paginationItems == null || itemsPerPage <= 0) return;

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, paginationItems.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            final int dataIdx = i;
            while (items.containsKey(slot)) slot++;
            if (slot >= slots) break;
            items.put(slot, paginationItems.get(i));
            clickHandlers.put(slot, ctx -> {
                ctx.gui().page(0);
            });
            slot++;
        }

        if (currentPage > 0) {
            int ps = prevNavSlot >= 0 ? prevNavSlot : slots - 9;
            items.put(ps, prevNavItem);
            final int targetPage = currentPage - 1;
            clickHandlers.put(ps, ctx -> {
                page(targetPage);
                refresh(ctx.player());
            });
        }
        if (currentPage < totalPages() - 1) {
            int ns = nextNavSlot >= 0 ? nextNavSlot : slots - 1;
            items.put(ns, nextNavItem);
            final int targetPage = currentPage + 1;
            clickHandlers.put(ns, ctx -> {
                page(targetPage);
                refresh(ctx.player());
            });
        }
    }

    private Inventory createInventory() {
        int rounded = (slots / 9) * 9;
        if (rounded < 9) rounded = 9;
        return Bukkit.createInventory(null, rounded, org.bukkit.ChatColor.translateAlternateColorCodes('&', title));
    }

    private void rebuildInventory(Inventory inv) {
        inv.clear();
        for (var e : items.entrySet()) {
            if (e.getKey() >= 0 && e.getKey() < inv.getSize()) {
                inv.setItem(e.getKey(), e.getValue());
            }
        }
    }

    private boolean isTracked(Inventory inv) {
        return trackedInventories.contains(inv);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!isTracked(inv)) return;
        event.setCancelled(readonly);

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inv)) {
            if (readonly) event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        ClickType ct;
        switch (event.getClick()) {
            case LEFT -> ct = ClickType.LEFT;
            case RIGHT -> ct = ClickType.RIGHT;
            case SHIFT_LEFT -> ct = ClickType.SHIFT_LEFT;
            case SHIFT_RIGHT -> ct = ClickType.SHIFT_RIGHT;
            case MIDDLE -> ct = ClickType.MIDDLE;
            case DOUBLE_CLICK -> ct = ClickType.DOUBLE_CLICK;
            case DROP -> ct = ClickType.DROP;
            case NUMBER_KEY -> ct = ClickType.NUMBER_KEY;
            default -> ct = ClickType.LEFT;
        }

        ClickContext ctx = new ClickContext() {
            @Override public Player player() { return player; }
            @Override public int slot() { return slot; }
            @Override public int row() { return slot / 9; }
            @Override public int col() { return slot % 9; }
            @Override public ItemStack cursor() { return event.getCursor(); }
            @Override public ItemStack current() { return clicked; }
            @Override public boolean isShiftClick() { return event.isShiftClick(); }
            @Override public boolean isRightClick() { return event.isRightClick(); }
            @Override public ClickType type() { return ct; }
            @Override public ChestGUI gui() { return ChestGUIImpl.this; }
        };

        Consumer<ClickContext> handler = clickHandlers.get(slot);
        if (handler != null) {
            try { handler.accept(ctx); } catch (Exception ignored) {}
        }
        if (anyClickHandler != null) {
            try { anyClickHandler.accept(ctx); } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isTracked(event.getInventory())) return;
        if (readonly) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!isTracked(inv)) return;
        Player player = (Player) event.getPlayer();
        viewerInventories.remove(player.getUniqueId());
        trackedInventories.remove(inv);
        if (closeHandler != null) {
            try { closeHandler.accept(player); } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Inventory inv = viewerInventories.remove(event.getPlayer().getUniqueId());
        if (inv != null) trackedInventories.remove(inv);
    }
}
