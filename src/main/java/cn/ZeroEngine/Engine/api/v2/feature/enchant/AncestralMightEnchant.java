package cn.ZeroEngine.Engine.api.v2.feature.enchant;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class AncestralMightEnchant extends SEnchantment {

    
    @Override
    public String id() { return "ancestral_might"; }

    
    @Override
    public String displayName() { return "祖宗之力"; }

    
    @Override
    public int maxLevel() { return 3; }

    
    @Override
    public String description() {
        return "继承先祖之力，获得全方位属性强化";
    }

    
    @Override
    public Set<String> applicableItems() {
        return new HashSet<>(Arrays.asList(
                "NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS",
                "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS",
                "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS",
                "GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS",
                "CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS",
                "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"
        ));
    }

    
    @Override
    public Set<String> conflictGroups() {
        Set<String> g = new HashSet<>();
        g.add("armor_ancestral");
        return g;
    }

    
    @Override
    public int anvilCost() { return 4; }

    
    
    
    @Override
    public List<AttributeBonus> attributes() {
        return Arrays.asList(
                
                AttributeBonus.add("max_health", "GENERIC_MAX_HEALTH", 4.0, 2.0),
                
                AttributeBonus.add("attack_damage", "GENERIC_ATTACK_DAMAGE", 2.0, 1.0),
                
                AttributeBonus.add("armor", "GENERIC_ARMOR", 2.0, 1.0),
                
                AttributeBonus.add("armor_toughness", "GENERIC_ARMOR_TOUGHNESS", 1.0, 0.5),
                
                AttributeBonus.add("knockback_resistance", "GENERIC_KNOCKBACK_RESISTANCE", 0.1, 0.05),
                
                AttributeBonus.multiply("movement_speed", "GENERIC_MOVEMENT_SPEED", 0.05, 0.02)
        );
    }

    
    @Override
    public void onDamaged(EnchantContext ctx) {
        if (ctx.level() < 2) return;
        if (Math.random() < 0.05 * ctx.level()) {
            Player p = ctx.player();
            p.setHealth(Math.min(p.getHealth() + 2.0,
                    p.getAttribute(findAttribute("GENERIC_MAX_HEALTH")).getValue()));
            p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    p.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.5f);
        }
    }

    
    @Override
    public void onEquip(Player player, ItemStack item, int level) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 0.8f);
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
    }

    
    @Override
    public void onUnequip(Player player, ItemStack item, int level) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 0.8f);
    }

    
    
    @Override
    public void onTick(Player player, ItemStack item, int level) {
        if (level >= 3 && Math.random() < 0.02) {
            player.getWorld().spawnParticle(Particle.ENCHANT,
                    player.getLocation().add(0, 1.5, 0), 3, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
