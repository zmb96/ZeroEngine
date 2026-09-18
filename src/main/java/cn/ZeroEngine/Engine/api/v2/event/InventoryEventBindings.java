package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.*;

import java.util.function.Consumer;

public final class InventoryEventBindings {

    private final EventRegistrar reg;

    InventoryEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public InventoryEventBindings open(Consumer<InventoryOpenEvent> h) {
        reg.register(InventoryOpenEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings close(Consumer<InventoryCloseEvent> h) {
        reg.register(InventoryCloseEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings click(Consumer<InventoryClickEvent> h) {
        reg.register(InventoryClickEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings drag(Consumer<InventoryDragEvent> h) {
        reg.register(InventoryDragEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings moveItem(Consumer<InventoryMoveItemEvent> h) {
        reg.register(InventoryMoveItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings pickupItem(Consumer<InventoryPickupItemEvent> h) {
        reg.register(InventoryPickupItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings creative(Consumer<InventoryCreativeEvent> h) {
        reg.register(InventoryCreativeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings craftItem(Consumer<CraftItemEvent> h) {
        reg.register(CraftItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings brew(Consumer<BrewEvent> h) {
        reg.register(BrewEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings furnaceSmelt(Consumer<FurnaceSmeltEvent> h) {
        reg.register(FurnaceSmeltEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings furnaceBurn(Consumer<FurnaceBurnEvent> h) {
        reg.register(FurnaceBurnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings furnaceExtract(Consumer<FurnaceExtractEvent> h) {
        reg.register(FurnaceExtractEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings prepareAnvil(Consumer<PrepareAnvilEvent> h) {
        reg.register(PrepareAnvilEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings prepareItemCraft(Consumer<PrepareItemCraftEvent> h) {
        reg.register(PrepareItemCraftEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings prepareSmithing(Consumer<PrepareSmithingEvent> h) {
        reg.register(PrepareSmithingEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public InventoryEventBindings tradeSelect(Consumer<TradeSelectEvent> h) {
        reg.register(TradeSelectEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
