package cn.ZeroEngine.Engine.api.v2.feature.item;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v2.SF;
import cn.ZeroEngine.Engine.api.v2.feature.enchant.SFAttr;

import java.util.*;

public abstract class SItem {

    private static Plugin plugin;

    public static void init(Plugin p) {
        plugin = p;
        SFAttr.ensureLoaded();
    }

    public static org.bukkit.attribute.Attribute findAttribute(String name) {
        SFAttr.ensureLoaded();
        org.bukkit.attribute.Attribute a = SFAttr.get(name);
        if (plugin != null) {
            SF sf = SF.sf();
            if (a != null) {
                sf.info("[Item] Attribute lookup: '" + name + "' -> matched '" + a.name() + "'");
            } else {
                sf.warn("[Item] Attribute lookup FAILED: '" + name + "'");
            }
        }
        return a;
    }

    public abstract String id();

    public abstract String displayName();

    public abstract Material material();

    public String description() { return ""; }

    public int maxStackSize() { return 64; }

    public boolean isUnbreakable() { return false; }

    public Set<String> tags() { return Collections.emptySet(); }

    public List<ItemAttributeBonus> attributes() { return Collections.emptyList(); }

    public boolean onRightClick(PlayerInteractEvent e) { return false; }

    public boolean onLeftClick(PlayerInteractEvent e) { return false; }

    public void onInteract(Player player, Action action, ItemStack item) {}

    public void onEquip(Player player, ItemStack item) {}

    public void onUnequip(Player player, ItemStack item) {}

    public String loreLine(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("§f").append(displayName());
        if (level > 1) sb.append(" ").append(level);
        return sb.toString();
    }

    public void applyDisplay(ItemMeta meta) {
        meta.setDisplayName(displayName());
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        if (!description().isEmpty()) {
            lore.add("§8" + description());
        }
        meta.setLore(lore);
    }

    public void applyAttributes(ItemMeta meta, int level) {
        for (ItemAttributeBonus a : attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = findAttribute(a.attribute);
                if (attr == null) continue;
                meta.addAttributeModifier(attr, new AttributeModifier(
                        modifierUuid(a.name), a.name, a.valueAt(level), a.operation, a.slot));
            } catch (Throwable t) {
            }
        }
    }

    public void clearAttributes(ItemMeta meta) {
        for (ItemAttributeBonus a : attributes()) {
            try {
                org.bukkit.attribute.Attribute attr = findAttribute(a.attribute);
                if (attr == null) continue;
                meta.removeAttributeModifier(attr);
            } catch (Throwable t) {
            }
        }
    }

    private UUID modifierUuid(String name) {
        return UUID.nameUUIDFromBytes(("sf_item_" + id() + "_" + name).getBytes());
    }

    private NamespacedKey itemKey() {
        return new NamespacedKey(plugin, "item_" + id());
    }

    public boolean is(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String val = pdc.get(itemKey(), PersistentDataType.STRING);
        return id().equals(val);
    }

    public int getLevel(ItemStack item) {
        if (!is(item)) return 1;
        return 1;
    }

    public ItemStack create() {
        return create(1);
    }

    public ItemStack create(int amount) {
        ItemStack item = new ItemStack(material(), Math.max(1, Math.min(amount, maxStackSize())));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName());
            List<String> lore = new ArrayList<>();
            if (!description().isEmpty()) {
                lore.add("§8" + description());
            }
            for (String tag : tags()) {
                lore.add("§7✦ " + tag);
            }
            meta.setLore(lore);
            meta.setUnbreakable(isUnbreakable());
            meta.setMaxStackSize(maxStackSize());
            for (ItemAttributeBonus a : attributes()) {
                try {
                    org.bukkit.attribute.Attribute attr = findAttribute(a.attribute);
                    if (attr == null) continue;
                    meta.addAttributeModifier(attr, new AttributeModifier(
                            modifierUuid(a.name), a.name, a.baseValue, a.operation, a.slot));
                } catch (Throwable t) {
                }
            }
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(itemKey(), PersistentDataType.STRING, id());
            item.setItemMeta(meta);
        }
        return item;
    }

    public static class ItemAttributeBonus {
        public final String name;
        public final String attribute;
        public final double baseValue;
        public final double perLevel;
        public final AttributeModifier.Operation operation;
        public final org.bukkit.inventory.EquipmentSlot slot;

        public ItemAttributeBonus(String name, String attribute, double baseValue, double perLevel,
                                  AttributeModifier.Operation operation, org.bukkit.inventory.EquipmentSlot slot) {
            this.name = name;
            this.attribute = attribute;
            this.baseValue = baseValue;
            this.perLevel = perLevel;
            this.operation = operation;
            this.slot = slot;
        }

        public ItemAttributeBonus(String name, String attribute, double baseValue, double perLevel,
                                  AttributeModifier.Operation operation) {
            this(name, attribute, baseValue, perLevel, operation, org.bukkit.inventory.EquipmentSlot.HAND);
        }

        public double valueAt(int level) {
            return baseValue + perLevel * (level - 1);
        }
    }
}
