package cn.ZeroEngine.Engine.api.v2.feature.enchant;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SEnchantment {

    private static Plugin plugin;
    private static final Map<String, Attribute> attrCache = new ConcurrentHashMap<>();
    private int levelCache;

    public static void init(Plugin p) {
        plugin = p;
        SFAttr.ensureLoaded();
    }

    public static Attribute findAttribute(String name) {
        if (name == null) return null;
        SFAttr.ensureLoaded();
        if (attrCache.containsKey(name)) return attrCache.get(name);

        Attribute direct = SFAttr.get(name);
        if (direct != null) {
            attrCache.put(name, direct);
            return direct;
        }

        String upper = name.toUpperCase();
        String base = upper.replaceAll("^(GENERIC_|PLAYER_|ZOMBIE_)", "");
        String[] candidates = {base, "GENERIC_" + base, "PLAYER_" + base, "ZOMBIE_" + base};

        Attribute result = null;
        String matched = null;
        for (String candidate : candidates) {
            Attribute found = SFAttr.get(candidate);
            if (found != null) { result = found; matched = candidate; break; }
        }

        if (result == null) {
            for (String candidate : candidates) {
                try {
                    Field f = Attribute.class.getField(candidate);
                    Object val = f.get(null);
                    if (val instanceof Attribute a) { result = a; matched = candidate; break; }
                } catch (Exception ignored) {}
            }
        }

        if (plugin != null) {
            SF sf = SF.sf();
            if (result != null) {
                sf.info("[Enchant] Attribute resolved: '" + name + "' -> '" + matched + "' (" + result.name() + ")");
            } else {
                sf.warn("[Enchant] Attribute not found: '" + name + "' (tried: " + String.join(", ", candidates) + ")");
            }
        }
        attrCache.put(name, result);
        return result;
    }

    public abstract String id();

    public abstract String displayName();

    public abstract int maxLevel();

    public int startLevel() { return 1; }

    public boolean isTreasure() { return false; }

    public boolean isCursed() { return false; }

    public String description() { return ""; }

    public String namespace() { return "sf"; }

    public int anvilCost() { return 1; }

    public abstract Set<String> applicableItems();

    public Set<String> conflicts() { return Collections.emptySet(); }

    public Set<String> conflictGroups() { return Collections.emptySet(); }

    public boolean conflictsWith(SEnchantment other) {
        if (conflicts().contains(other.id())) return true;
        if (other.conflicts().contains(id())) return true;
        for (String g : conflictGroups()) {
            if (other.conflictGroups().contains(g)) return true;
        }
        return false;
    }

    public boolean canEnchantItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        String typeName = item.getType().name();
        for (String pattern : applicableItems()) {
            if (pattern.equals("*")) return true;
            if (pattern.endsWith("*")) {
                if (typeName.startsWith(pattern.substring(0, pattern.length() - 1))) return true;
            } else if (pattern.equalsIgnoreCase(typeName)) {
                return true;
            }
        }
        return false;
    }

    public List<AttributeBonus> attributes() { return Collections.emptyList(); }

    public AttributeBonus attribute(String name) {
        for (AttributeBonus a : attributes()) if (a.name.equalsIgnoreCase(name)) return a;
        return null;
    }

    public String loreLine(int level) {
        StringBuilder sb = new StringBuilder();
        if (isCursed()) sb.append("§c§o").append(displayName());
        else sb.append("§7").append(displayName());
        if (maxLevel() > 1) sb.append(" ").append(roman(level));
        return sb.toString();
    }

    public void applyLore(ItemMeta meta, int level) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String line = loreLine(level);
        for (int i = lore.size() - 1; i >= 0; i--) {
            if (lore.get(i).equals(line)) { lore.remove(i); break; }
        }
        lore.add(line);
        meta.setLore(lore);
    }

    public void removeLore(ItemMeta meta) {
        if (!meta.hasLore()) return;
        List<String> lore = new ArrayList<>(meta.getLore());
        for (int lvl = 1; lvl <= maxLevel(); lvl++) lore.removeIf(loreLine(lvl)::equals);
        meta.setLore(lore);
    }

    private NamespacedKey dataKey() {
        return new NamespacedKey(plugin, "enchant_" + namespace() + "_" + id());
    }

    public boolean isOn(ItemStack item) { return getLevel(item) > 0; }

    public int getLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer val = pdc.get(dataKey(), PersistentDataType.INTEGER);
        return val == null ? 0 : val;
    }

    public void setLevel(ItemStack item, int level) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (level <= 0) {
            pdc.remove(dataKey());
            removeLore(meta);
        } else {
            int clamped = Math.max(1, Math.min(level, maxLevel()));
            pdc.set(dataKey(), PersistentDataType.INTEGER, clamped);
            applyLore(meta, clamped);
        }
        item.setItemMeta(meta);
    }

    public void removeFrom(ItemStack item) {
        setLevel(item, 0);
    }

    public static String roman(int n) {
        if (n <= 0) return "";
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n < r.length ? r[n] : String.valueOf(n);
    }

    public void onAttack(EnchantContext ctx) {}
    public void onDamaged(EnchantContext ctx) {}
    public void onMine(EnchantContext ctx) {}
    public void onEquip(Player player, ItemStack item, int level) {}
    public void onUnequip(Player player, ItemStack item, int level) {}
    public void onTick(Player player, ItemStack item, int level) {}

    public static class AttributeBonus {
        public final String name;
        public final String attribute;
        public final double base;
        public final double perLevel;
        public final AttributeModifier.Operation operation;
        public final String slot;

        public AttributeBonus(String name, String attribute, double base, double perLevel,
                              AttributeModifier.Operation operation, String slot) {
            this.name = name;
            this.attribute = attribute;
            this.base = base;
            this.perLevel = perLevel;
            this.operation = operation;
            this.slot = slot;
        }

        public AttributeBonus(String name, String attribute, double base, double perLevel,
                              AttributeModifier.Operation operation) {
            this(name, attribute, base, perLevel, operation, "ANY");
        }

        public double valueAt(int level) {
            return base + perLevel * (level - 1);
        }

        public static AttributeBonus add(String name, String attr, double base, double perLevel) {
            return new AttributeBonus(name, attr, base, perLevel, AttributeModifier.Operation.ADD_NUMBER);
        }

        public static AttributeBonus multiply(String name, String attr, double base, double perLevel) {
            return new AttributeBonus(name, attr, base, perLevel, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        }
    }
}
