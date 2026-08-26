package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.*;

public class ItemListener implements Listener {

    private final ItemManager manager;
    private final Map<UUID, Set<String>> activeItems = new HashMap<>();
    private final Random random = new Random();

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Material broken = b.getType();
        if (broken == null || broken.isAir()) return;
        Player p = e.getPlayer();
        if (p.getGameMode().toString().contains("CREATIVE")) return;
        World w = b.getWorld();
        for (SItem item : manager.all()) {
            for (DropSource src : item.dropSources()) {
                if (src.type != DropSource.Type.BLOCK_BREAK) continue;
                if (!(src.target instanceof Material)) continue;
                if (src.target != broken) continue;
                if (!src.roll(random)) continue;
                int amt = src.rollAmount(random);
                w.dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), item.create(amt));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity ent = e.getEntity();
        org.bukkit.entity.EntityType et = ent.getType();
        World w = ent.getWorld();
        for (SItem item : manager.all()) {
            for (DropSource src : item.dropSources()) {
                if (src.type != DropSource.Type.ENTITY_DEATH) continue;
                if (src.target != null && src.target != et) continue;
                if (!src.roll(random)) continue;
                int amt = src.rollAmount(random);
                w.dropItemNaturally(ent.getLocation(), item.create(amt));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        org.bukkit.entity.Entity caught = e.getCaught();
        if (!(caught instanceof org.bukkit.entity.Item)) return;
        org.bukkit.entity.Item drop = (org.bukkit.entity.Item) caught;
        for (SItem item : manager.all()) {
            for (DropSource src : item.dropSources()) {
                if (src.type != DropSource.Type.FISHING) continue;
                if (!src.roll(random)) continue;
                int amt = src.rollAmount(random);
                drop.setItemStack(item.create(amt));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent e) {
        List<ItemStack> generated = e.getLoot();
        if (generated == null) return;
        List<ItemStack> extra = new ArrayList<>();
        for (SItem item : manager.all()) {
            for (DropSource src : item.dropSources()) {
                if (src.type != DropSource.Type.CHEST_LOOT) continue;
                if (!src.roll(random)) continue;
                int amt = src.rollAmount(random);
                extra.add(item.create(amt));
            }
        }
        if (extra.isEmpty()) return;
        List<ItemStack> merged = new ArrayList<>(generated);
        merged.addAll(extra);
        e.setLoot(merged);
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
