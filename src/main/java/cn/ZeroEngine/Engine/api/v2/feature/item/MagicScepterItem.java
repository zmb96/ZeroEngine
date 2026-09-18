package cn.ZeroEngine.Engine.api.v2.feature.item;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.*;

public class MagicScepterItem extends SItem {

    @Override
    public String id() { return "magic_scepter"; }

    @Override
    public String displayName() { return "§6魔法权杖"; }

    @Override
    public Material material() { return Material.BLAZE_ROD; }

    @Override
    public String description() { return "右键传送至目标位置"; }

    @Override
    public int maxStackSize() { return 1; }

    @Override
    public boolean isUnbreakable() { return true; }

    @Override
    public Set<String> tags() {
        Set<String> t = new LinkedHashSet<>();
        t.add("§c传说");
        t.add("§b空间之力");
        return t;
    }

    @Override
    public List<ItemAttributeBonus> attributes() {
        return Arrays.asList(
                new ItemAttributeBonus("scepter_damage", "GENERIC_ATTACK_DAMAGE", 10.0, 0,
                        org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                        org.bukkit.inventory.EquipmentSlot.HAND),
                new ItemAttributeBonus("scepter_speed", "GENERIC_MOVEMENT_SPEED", 0.1, 0,
                        org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                        org.bukkit.inventory.EquipmentSlot.HAND)
        );
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();
        SF sf = SF.sf();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            var clicked = e.getClickedBlock();
            if (clicked != null) {
                p.teleport(clicked.getLocation().add(0, 1, 0));
                p.getWorld().spawnParticle(Particle.PORTAL,
                        clicked.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
                sf.msg(p, ChatColor.GREEN + "✦ 瞬移成功 ✦");
            }
        } else if (action == Action.RIGHT_CLICK_AIR) {
            var target = p.getEyeLocation().getDirection().multiply(10).add(p.getLocation().toVector());
            p.teleport(target.toLocation(p.getWorld()));
            p.getWorld().spawnParticle(Particle.PORTAL,
                    p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
            sf.msg(p, ChatColor.GREEN + "✦ 瞬移成功 ✦");
        }

        return true;
    }

    @Override
    public boolean onLeftClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        SF sf = SF.sf();
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                p.getLocation().add(0, 1, 0), 30, 1.0, 1.0, 1.0, 0.1);
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
        sf.msg(p, ChatColor.GOLD + "✦ 你挥动了权杖 ✦");
        return true;
    }

    @Override
    public void onEquip(Player player, ItemStack item) {
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.0f);
    }

    @Override
    public void onUnequip(Player player, ItemStack item) {
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
    }
}
