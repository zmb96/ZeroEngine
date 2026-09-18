package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.lang.reflect.Field;
import java.util.*;

public class EnchantAttributeListener implements Listener {

    private final EnchantManager manager;
    private final Map<UUID, Set<String>> activeMods = new HashMap<>();
    private final Map<String, org.bukkit.attribute.Attribute> attrCache = new HashMap<>();
    private BukkitRunnable tickTask;

    public EnchantAttributeListener(EnchantManager manager) {
        this.manager = manager;
    }

    private org.bukkit.attribute.Attribute findAttribute(String name) {
        if (attrCache.containsKey(name)) return attrCache.get(name);
        org.bukkit.attribute.Attribute result = SEnchantment.findAttribute(name);
        attrCache.put(name, result);
        return result;
    }

    public void startTick(SF sf, long period) {
        stopTick();
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : sf.bukkit().getOnlinePlayers()) {
                    tickPlayer(p);
                }
            }
        };
        tickTask.runTaskTimer(sf.plugin(), 0L, period);
    }

    public void stopTick() {
        if (tickTask != null) {
            try { tickTask.cancel(); } catch (IllegalStateException ignored) {}
            tickTask = null;
        }
    }

    private void tickPlayer(Player p) {
        SF sf = SF.sf();
        PlayerInventory inv = p.getInventory();
        applyEquipment(p, inv);
        ItemStack main = inv.getItemInMainHand();
        if (main != null && !main.getType().isAir()) {
            for (Map.Entry<SEnchantment, Integer> e : manager.getOn(main).entrySet()) {
                try { e.getKey().onTick(p, main, e.getValue()); } catch (Throwable t) {
                    sf.error("[Enchant] onTick error in " + e.getKey().id(), t);
                }
            }
        }
    }

    private void applyEquipment(Player p, PlayerInventory inv) {
        Set<String> current = new HashSet<>();
        ItemStack[] armor = inv.getArmorContents();
        for (int slot = 0; slot < armor.length; slot++) {
            ItemStack piece = armor[slot];
            if (piece == null || piece.getType().isAir()) continue;
            for (Map.Entry<SEnchantment, Integer> e : manager.getOn(piece).entrySet()) {
                String key = e.getKey().id() + "#" + slot;
                current.add(key);
                applyAttributes(p, e.getKey(), e.getValue());
            }
        }
        Set<String> prev = activeMods.getOrDefault(p.getUniqueId(), Collections.emptySet());
        for (String k : prev) if (!current.contains(k)) {
            String[] parts = k.split("#");
            SEnchantment en = manager.get(parts[0]);
            if (en != null) {
                removeAllMods(p, en);
            }
        }
        activeMods.put(p.getUniqueId(), current);
    }

    private void applyAttributes(Player p, SEnchantment enchant, int level) {
        SF sf = SF.sf();
        for (SEnchantment.AttributeBonus a : enchant.attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = findAttribute(a.attribute);
                if (attr == null) {
                    sf.error("[Enchant] attribute not found: " + a.attribute);
                    continue;
                }
                AttributeInstance inst = p.getAttribute(attr);
                if (inst == null) continue;
                UUID modId = modUuid(enchant.id());
                if (inst.getModifier(modId) != null) continue;
                inst.addModifier(new AttributeModifier(modId,
                        enchant.id(), a.valueAt(level), a.operation, getSlot(a.slot)));
            } catch (Throwable t) {
                sf.error("[Enchant] attribute apply error: " + a.attribute, t);
            }
        }
    }

    private void removeAllMods(Player p, SEnchantment enchant) {
        for (SEnchantment.AttributeBonus a : enchant.attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = findAttribute(a.attribute);
                if (attr == null) continue;
                AttributeInstance inst = p.getAttribute(attr);
                if (inst == null) continue;
                AttributeModifier m = inst.getModifier(modUuid(enchant.id()));
                if (m != null) inst.removeModifier(m);
            } catch (Throwable ignored) {}
        }
    }

    private UUID modUuid(String id) {
        return UUID.nameUUIDFromBytes(("sf_enchant_" + id).getBytes());
    }

    private org.bukkit.inventory.EquipmentSlot getSlot(String slot) {
        try { return org.bukkit.inventory.EquipmentSlot.valueOf(slot.toUpperCase()); }
        catch (Exception e) { return null; }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        SF sf = SF.sf();
        if (!(e.getDamager() instanceof Player p)) return;
        ItemStack weapon = p.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;
        for (Map.Entry<SEnchantment, Integer> en : manager.getOn(weapon).entrySet()) {
            LivingEntity target = e.getEntity() instanceof LivingEntity le ? le : null;
            EnchantContext ctx = new EnchantContext(p, weapon, en.getValue(), target, e);
            try { en.getKey().onAttack(ctx); } catch (Throwable t) {
                sf.error("[Enchant] onAttack error in " + en.getKey().id(), t);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent e) {
        SF sf = SF.sf();
        if (!(e.getEntity() instanceof Player p)) return;
        PlayerInventory inv = p.getInventory();
        for (ItemStack armor : inv.getArmorContents()) {
            if (armor == null || armor.getType().isAir()) continue;
            for (Map.Entry<SEnchantment, Integer> en : manager.getOn(armor).entrySet()) {
                EnchantContext ctx = new EnchantContext(p, armor, en.getValue(), null, e);
                try { en.getKey().onDamaged(ctx); } catch (Throwable t) {
                    sf.error("[Enchant] onDamaged error in " + en.getKey().id(), t);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMine(BlockBreakEvent e) {
        SF sf = SF.sf();
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool == null || tool.getType().isAir()) return;
        for (Map.Entry<SEnchantment, Integer> en : manager.getOn(tool).entrySet()) {
            EnchantContext ctx = new EnchantContext(p, tool, en.getValue(), null, e);
            try { en.getKey().onMine(ctx); } catch (Throwable t) {
                sf.error("[Enchant] onMine error in " + en.getKey().id(), t);
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            SF sf = SF.sf();
            sf.runLater(() -> applyEquipment(p, p.getInventory()), 1L);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            SF sf = SF.sf();
            sf.runLater(() -> applyEquipment(p, p.getInventory()), 1L);
        }
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        SF sf = SF.sf();
        sf.runLater(() -> applyEquipment(p, p.getInventory()), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        SF sf = SF.sf();
        sf.runLater(() -> applyEquipment(p, p.getInventory()), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        for (SEnchantment en : manager.all()) removeAllMods(p, en);
        activeMods.remove(p.getUniqueId());
    }

    public void shutdown() {
        stopTick();
        SF sf = SF.sf();
        for (Player p : sf.bukkit().getOnlinePlayers()) {
            for (SEnchantment en : manager.all()) removeAllMods(p, en);
        }
        activeMods.clear();
    }
}
