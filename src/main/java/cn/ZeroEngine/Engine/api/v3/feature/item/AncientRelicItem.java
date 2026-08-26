package cn.ZeroEngine.Engine.api.v3.feature.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AncientRelicItem extends SItem {

    @Override
    public String id() {
        return "ancient_relic";
    }

    @Override
    public String displayName() {
        return "§6§l远古遗物";
    }

    @Override
    public Material material() {
        return Material.SUNFLOWER;
    }

    @Override
    public String description() {
        return "§7蕴含远古力量的神秘碎片";
    }

    @Override
    public int maxStackSize() {
        return 16;
    }

    @Override
    public boolean isUnbreakable() {
        return true;
    }

    @Override
    public Set<String> tags() {
        return new HashSet<>(Arrays.asList("传说", "远古之力"));
    }

    @Override
    public List<DropSource> dropSources() {
        return Arrays.asList(
                DropSource.block(Material.STONE, 0.005, 1, 1),
                DropSource.block(Material.DEEPSLATE, 0.01, 1, 1),
                DropSource.block(Material.ANCIENT_DEBRIS, 0.5, 1, 2),
                DropSource.mob(EntityType.WITHER_SKELETON, 0.2, 1, 1),
                DropSource.mob(EntityType.ENDER_DRAGON, 1.0, 2, 4),
                DropSource.mob(EntityType.ELDER_GUARDIAN, 0.8, 1, 3),
                DropSource.fishing(0.05, 1, 1),
                DropSource.chest(0.15, 1, 2)
        );
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PotionEffectType resistance = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("resistance"));
        PotionEffectType strength = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("strength"));
        PotionEffectType haste = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("haste"));
        if (resistance != null) p.addPotionEffect(new PotionEffect(resistance, 200, 2));
        if (strength != null) p.addPotionEffect(new PotionEffect(strength, 200, 1));
        if (haste != null) p.addPotionEffect(new PotionEffect(haste, 200, 1));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        return true;
    }

    @Override
    public boolean onLeftClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PotionEffectType speed = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("speed"));
        PotionEffectType jump = org.bukkit.Registry.EFFECT.get(org.bukkit.NamespacedKey.minecraft("jump_boost"));
        if (speed != null) p.addPotionEffect(new PotionEffect(speed, 200, 3));
        if (jump != null) p.addPotionEffect(new PotionEffect(jump, 200, 3));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.2f);
        return true;
    }

    @Override
    public void onInteract(Player player, Action action, ItemStack item) {
    }

    @Override
    public void onEquip(Player player, ItemStack item) {
        player.sendMessage("§6远古之力在你手中苏醒...");
    }

    @Override
    public void onUnequip(Player player, ItemStack item) {
        player.sendMessage("§7远古之力沉寂了下去。");
    }
}
