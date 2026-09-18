package cn.ZeroEngine.Engine.api.v3.feature.enchant;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantContext {

    private final Player player;
    private final ItemStack item;
    private final int level;
    private final LivingEntity target;
    private final Event event;

    public EnchantContext(Player player, ItemStack item, int level, LivingEntity target, Event event) {
        this.player = player;
        this.item = item;
        this.level = level;
        this.target = target;
        this.event = event;
    }

    public Player player() { return player; }
    public ItemStack item() { return item; }
    public int level() { return level; }
    public LivingEntity target() { return target; }
    public Event event() { return event; }

    public boolean isAttack() { return event instanceof EntityDamageByEntityEvent; }
    public boolean isDamaged() { return event instanceof EntityDamageEvent && !isAttack(); }
    public boolean isMine() { return event instanceof BlockBreakEvent; }

    @SuppressWarnings("unchecked")
    public <T extends Event> T eventAs(Class<T> type) {
        return type.isInstance(event) ? (T) event : null;
    }

    public EntityDamageByEntityEvent asAttack() { return eventAs(EntityDamageByEntityEvent.class); }
    public EntityDamageEvent asDamaged() { return eventAs(EntityDamageEvent.class); }
    public BlockBreakEvent asMine() { return eventAs(BlockBreakEvent.class); }

    public double damage() {
        EntityDamageEvent e = asDamaged();
        return e != null ? e.getDamage() : 0;
    }

    public void multiplyDamage(double factor) {
        EntityDamageByEntityEvent e = asAttack();
        if (e != null) e.setDamage(e.getDamage() * factor);
    }

    public void addDamage(double extra) {
        EntityDamageByEntityEvent e = asAttack();
        if (e != null) e.setDamage(e.getDamage() + extra);
    }
}
