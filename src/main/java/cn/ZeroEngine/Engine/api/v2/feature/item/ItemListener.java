package cn.ZeroEngine.Engine.api.v2.feature.item;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.*;

public class ItemListener implements Listener {

    private final ItemManager manager;
    private final Map<UUID, Set<String>> activeItems = new HashMap<>();

    public ItemListener(ItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType().isAir()) return;

        SItem custom = manager.find(item);
        if (custom == null) return;

        Action action = e.getAction();
        boolean handled = false;

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handled = custom.onRightClick(e);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handled = custom.onLeftClick(e);
        }

        if (handled) {
            e.setCancelled(true);
        }

        custom.onInteract(p, action, item);
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();
        SF sf = SF.sf();
        sf.runLater(() -> applyEquipState(p, inv), 1L);
    }

    public void applyEquipState(Player p, PlayerInventory inv) {
        Set<String> current = new HashSet<>();
        ItemStack main = inv.getItemInMainHand();
        ItemStack off = inv.getItemInOffHand();

        for (ItemStack item : new ItemStack[]{main, off}) {
            if (item == null || item.getType().isAir()) continue;
            SItem custom = manager.find(item);
            if (custom != null) {
                current.add(custom.id());
                applyAttributes(p, custom);
            }
        }

        Set<String> prev = activeItems.getOrDefault(p.getUniqueId(), Collections.emptySet());
        for (String id : prev) {
            if (!current.contains(id)) {
                SItem custom = manager.get(id);
                if (custom != null) {
                    removeAttributes(p, custom);
                    custom.onUnequip(p, findItem(p, id));
                }
            }
        }

        for (String id : current) {
            if (!prev.contains(id)) {
                SItem custom = manager.get(id);
                if (custom != null) {
                    custom.onEquip(p, findItem(p, id));
                }
            }
        }

        activeItems.put(p.getUniqueId(), current);
    }

    private void applyAttributes(Player p, SItem item) {
        SF sf = SF.sf();
        for (SItem.ItemAttributeBonus a : item.attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = SItem.findAttribute(a.attribute);
                if (attr == null) continue;
                org.bukkit.attribute.AttributeInstance inst = p.getAttribute(attr);
                if (inst == null) continue;
                UUID modId = UUID.nameUUIDFromBytes(("sf_item_" + item.id() + "_" + a.name).getBytes());
                if (inst.getModifier(modId) != null) continue;
                inst.addModifier(new org.bukkit.attribute.AttributeModifier(
                        modId, a.name, a.baseValue, a.operation, a.slot));
            } catch (Throwable t) {
                sf.error("[Item] attribute apply error: " + a.attribute, t);
            }
        }
    }

    private void removeAttributes(Player p, SItem item) {
        for (SItem.ItemAttributeBonus a : item.attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = SItem.findAttribute(a.attribute);
                if (attr == null) continue;
                org.bukkit.attribute.AttributeInstance inst = p.getAttribute(attr);
                if (inst == null) continue;
                UUID modId = UUID.nameUUIDFromBytes(("sf_item_" + item.id() + "_" + a.name).getBytes());
                org.bukkit.attribute.AttributeModifier m = inst.getModifier(modId);
                if (m != null) inst.removeModifier(m);
            } catch (Throwable ignored) {}
        }
    }

    private ItemStack findItem(Player p, String id) {
        SItem item = manager.get(id);
        if (item == null) return null;
        for (ItemStack invItem : p.getInventory().getContents()) {
            if (item.is(invItem)) return invItem;
        }
        return null;
    }

    public void shutdown() {
        SF sf = SF.sf();
        for (Player p : sf.bukkit().getOnlinePlayers()) {
            for (String id : activeItems.getOrDefault(p.getUniqueId(), Collections.emptySet())) {
                SItem custom = manager.get(id);
                if (custom != null) {
                    removeAttributes(p, custom);
                    custom.onUnequip(p, findItem(p, id));
                }
            }
        }
        activeItems.clear();
    }
}
