package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;

import java.util.function.Consumer;

public final class EntityEventBindings {

    private final EventRegistrar reg;

    EntityEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public EntityEventBindings damage(Consumer<EntityDamageEvent> h) {
        reg.register(EntityDamageEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings damageByEntity(Consumer<EntityDamageByEntityEvent> h) {
        reg.register(EntityDamageByEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings damageByBlock(Consumer<EntityDamageByBlockEvent> h) {
        reg.register(EntityDamageByBlockEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings death(Consumer<EntityDeathEvent> h) {
        reg.register(EntityDeathEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings playerDeath(Consumer<PlayerDeathEvent> h) {
        reg.register(PlayerDeathEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings spawn(Consumer<EntitySpawnEvent> h) {
        reg.register(EntitySpawnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    @SuppressWarnings("deprecation")
    public EntityEventBindings despawn(Consumer<EntityRemoveEvent> h) {
        reg.register(EntityRemoveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings target(Consumer<EntityTargetEvent> h) {
        reg.register(EntityTargetEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings targetLiving(Consumer<EntityTargetLivingEntityEvent> h) {
        reg.register(EntityTargetLivingEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings teleport(Consumer<EntityTeleportEvent> h) {
        reg.register(EntityTeleportEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings combust(Consumer<EntityCombustEvent> h) {
        reg.register(EntityCombustEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings explode(Consumer<EntityExplodeEvent> h) {
        reg.register(EntityExplodeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings regainHealth(Consumer<EntityRegainHealthEvent> h) {
        reg.register(EntityRegainHealthEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings shootBow(Consumer<EntityShootBowEvent> h) {
        reg.register(EntityShootBowEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings toggleGlide(Consumer<EntityToggleGlideEvent> h) {
        reg.register(EntityToggleGlideEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings toggleSwim(Consumer<EntityToggleSwimEvent> h) {
        reg.register(EntityToggleSwimEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings potionEffect(Consumer<EntityPotionEffectEvent> h) {
        reg.register(EntityPotionEffectEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings breed(Consumer<EntityBreedEvent> h) {
        reg.register(EntityBreedEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings tame(Consumer<EntityTameEvent> h) {
        reg.register(EntityTameEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings changeBlock(Consumer<EntityChangeBlockEvent> h) {
        reg.register(EntityChangeBlockEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings interact(Consumer<EntityInteractEvent> h) {
        reg.register(EntityInteractEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings dropItem(Consumer<EntityDropItemEvent> h) {
        reg.register(EntityDropItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings pickupItem(Consumer<EntityPickupItemEvent> h) {
        reg.register(EntityPickupItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings portal(Consumer<EntityPortalEvent> h) {
        reg.register(EntityPortalEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings portalEnter(Consumer<EntityPortalEnterEvent> h) {
        reg.register(EntityPortalEnterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings portalExit(Consumer<EntityPortalExitEvent> h) {
        reg.register(EntityPortalExitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings mount(Consumer<EntityMountEvent> h) {
        reg.register(EntityMountEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings dismount(Consumer<EntityDismountEvent> h) {
        reg.register(EntityDismountEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings transform(Consumer<EntityTransformEvent> h) {
        reg.register(EntityTransformEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings resurrect(Consumer<EntityResurrectEvent> h) {
        reg.register(EntityResurrectEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings spellCast(Consumer<EntitySpellCastEvent> h) {
        reg.register(EntitySpellCastEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings projectileHit(Consumer<ProjectileHitEvent> h) {
        reg.register(ProjectileHitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings projectileLaunch(Consumer<ProjectileLaunchEvent> h) {
        reg.register(ProjectileLaunchEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings itemDespawn(Consumer<ItemDespawnEvent> h) {
        reg.register(ItemDespawnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings itemMerge(Consumer<ItemMergeEvent> h) {
        reg.register(ItemMergeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings itemSpawn(Consumer<ItemSpawnEvent> h) {
        reg.register(ItemSpawnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings fireworkExplode(Consumer<FireworkExplodeEvent> h) {
        reg.register(FireworkExplodeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings pigZap(Consumer<PigZapEvent> h) {
        reg.register(PigZapEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings creeperPower(Consumer<CreeperPowerEvent> h) {
        reg.register(CreeperPowerEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings expBottle(Consumer<ExpBottleEvent> h) {
        reg.register(ExpBottleEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings piglinBarter(Consumer<PiglinBarterEvent> h) {
        reg.register(PiglinBarterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings villagerAcquireTrade(Consumer<VillagerAcquireTradeEvent> h) {
        reg.register(VillagerAcquireTradeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings villagerReplenishTrade(Consumer<VillagerReplenishTradeEvent> h) {
        reg.register(VillagerReplenishTradeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings foodLevel(Consumer<FoodLevelChangeEvent> h) {
        reg.register(FoodLevelChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings sheepRegrowWool(Consumer<SheepRegrowWoolEvent> h) {
        reg.register(SheepRegrowWoolEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings sheepDyeWool(Consumer<SheepDyeWoolEvent> h) {
        reg.register(SheepDyeWoolEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings slimeSplit(Consumer<SlimeSplitEvent> h) {
        reg.register(SlimeSplitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings enderDragonChangePhase(Consumer<EnderDragonChangePhaseEvent> h) {
        reg.register(EnderDragonChangePhaseEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public EntityEventBindings horseJump(Consumer<HorseJumpEvent> h) {
        reg.register(HorseJumpEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
