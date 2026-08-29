package cn.ZeroEngine.Engine.api.v3.feature.item;

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
import cn.ZeroEngine.Engine.api.v3.SF;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.SFAttr;

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

    public List<DropSource> dropSources() { return Collections.emptyList(); }

    public boolean onRightClick(PlayerInteractEvent e) { return false; }

    public boolean onLeftClick(PlayerInteractEvent e) { return false; }

    public boolean isFood() { return false; }

    public int foodNutrition() { return 0; }

    public float foodSaturation() { return 0; }

    public boolean canAlwaysEat() { return false; }

    public void onEat(org.bukkit.event.player.PlayerItemConsumeEvent e) {}

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

    // NamespacedKey 在 Paper 26.x 严格只允许 [a-z0-9_-.\/]（Bukkit NamespacedKey.lambda$checkError$0）
    // 对中文/非 ASCII 的 id() 做安全 slug：合法字符保留 + 8 位 SHA-1 哈希兜底；前缀 "item_"。
    // —— 兼容策略：
    //    * 写入（create）：只用新 key（安全 slug 版）
    //    * 读取（is/getLevel/getAttribute）：先查新 key，未命中再回退旧 key（"item_"+id()），防止已存在
    //      玩家背包里的老 SItem（比如旧版中文 id 直接拼 NamespacedKey）突然识别不到。
    private NamespacedKey itemKey() {
        String raw = id();
        StringBuilder valid = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = Character.toLowerCase(raw.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.' || c == '/') valid.append(c);
        }
        String slug = valid.toString().replaceAll("[/._-]+", "_")
                .replaceAll("^_+", "").replaceAll("_+$", "");
        String hash = toHex(unsha1(raw), 8);
        String tail = slug.isEmpty() ? hash : (slug.length() > 40 ? slug.substring(0, 40) + "_" + hash : slug + "_" + hash);
        return new NamespacedKey(plugin, "item_" + tail);
    }

    // 旧 key（Paper 26.x 之前直接拼 id()；如果 id 包含中文，在 Paper 26.x 会抛异常）
    // 仅用于读取路径的回退；如果构造时会抛就返回 null，调用方会忽略这个回退。
    private NamespacedKey itemKeyLegacyOrNull() {
        try {
            return new NamespacedKey(plugin, "item_" + id());
        } catch (IllegalArgumentException paper26KeyReject) {
            return null;
        }
    }

    private static String readPdc(PersistentDataContainer pdc, NamespacedKey... keys) {
        if (pdc == null) return null;
        for (NamespacedKey k : keys) {
            if (k == null) continue;
            String v;
            try { v = pdc.get(k, PersistentDataType.STRING); } catch (Throwable ignore) { continue; }
            if (v != null) return v;
        }
        return null;
    }

    private static byte[] unsha1(String s) {
        try {
            return java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            long h = (long) s.hashCode() * 0x9E3779B97F4A7C15L;
            byte[] out = new byte[8];
            for (int i = 7; i >= 0; i--, h >>>= 8) out[i] = (byte) (h & 0xFF);
            return out;
        }
    }

    private static String toHex(byte[] b, int n) {
        int len = Math.min(n, b.length);
        StringBuilder sb = new StringBuilder(len * 2);
        char[] hex = "0123456789abcdef".toCharArray();
        for (int i = 0; i < len; i++) {
            sb.append(hex[(b[i] >> 4) & 0xF]);
            sb.append(hex[b[i] & 0xF]);
        }
        return sb.toString();
    }

    public boolean is(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        // 读：先新 key，再回退旧 key（兼容中文 id 的老玩家物品）
        String val = readPdc(pdc, itemKey(), itemKeyLegacyOrNull());
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
