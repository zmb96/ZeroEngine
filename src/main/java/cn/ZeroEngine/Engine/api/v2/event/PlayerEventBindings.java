package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;

import java.util.function.Consumer;

public final class PlayerEventBindings {

    private final EventRegistrar reg;

    PlayerEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public PlayerEventBindings join(Consumer<PlayerJoinEvent> h) {
        reg.register(PlayerJoinEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings quit(Consumer<PlayerQuitEvent> h) {
        reg.register(PlayerQuitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings chat(Consumer<AsyncPlayerChatEvent> h) {
        reg.register(AsyncPlayerChatEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings chatSync(Consumer<PlayerChatEvent> h) {
        reg.register(PlayerChatEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings preLogin(Consumer<AsyncPlayerPreLoginEvent> h) {
        reg.register(AsyncPlayerPreLoginEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings login(Consumer<PlayerLoginEvent> h) {
        reg.register(PlayerLoginEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings respawn(Consumer<PlayerRespawnEvent> h) {
        reg.register(PlayerRespawnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings move(Consumer<PlayerMoveEvent> h) {
        reg.register(PlayerMoveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings teleport(Consumer<PlayerTeleportEvent> h) {
        reg.register(PlayerTeleportEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings interact(Consumer<PlayerInteractEvent> h) {
        reg.register(PlayerInteractEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings interactEntity(Consumer<PlayerInteractEntityEvent> h) {
        reg.register(PlayerInteractEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings interactAtEntity(Consumer<PlayerInteractAtEntityEvent> h) {
        reg.register(PlayerInteractAtEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings itemHeld(Consumer<PlayerItemHeldEvent> h) {
        reg.register(PlayerItemHeldEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings itemConsume(Consumer<PlayerItemConsumeEvent> h) {
        reg.register(PlayerItemConsumeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings dropItem(Consumer<PlayerDropItemEvent> h) {
        reg.register(PlayerDropItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings pickupItem(Consumer<PlayerPickupItemEvent> h) {
        reg.register(PlayerPickupItemEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings bucketFill(Consumer<PlayerBucketFillEvent> h) {
        reg.register(PlayerBucketFillEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings bucketEmpty(Consumer<PlayerBucketEmptyEvent> h) {
        reg.register(PlayerBucketEmptyEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings fish(Consumer<PlayerFishEvent> h) {
        reg.register(PlayerFishEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings shears(Consumer<PlayerShearEntityEvent> h) {
        reg.register(PlayerShearEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings toggleFlight(Consumer<PlayerToggleFlightEvent> h) {
        reg.register(PlayerToggleFlightEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings toggleSneak(Consumer<PlayerToggleSneakEvent> h) {
        reg.register(PlayerToggleSneakEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings toggleSprint(Consumer<PlayerToggleSprintEvent> h) {
        reg.register(PlayerToggleSprintEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings kick(Consumer<PlayerKickEvent> h) {
        reg.register(PlayerKickEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings command(Consumer<PlayerCommandPreprocessEvent> h) {
        reg.register(PlayerCommandPreprocessEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings portal(Consumer<PlayerPortalEvent> h) {
        reg.register(PlayerPortalEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings velocity(Consumer<PlayerVelocityEvent> h) {
        reg.register(PlayerVelocityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings expChange(Consumer<PlayerExpChangeEvent> h) {
        reg.register(PlayerExpChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings levelChange(Consumer<PlayerLevelChangeEvent> h) {
        reg.register(PlayerLevelChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings gameMode(Consumer<PlayerGameModeChangeEvent> h) {
        reg.register(PlayerGameModeChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings bedEnter(Consumer<PlayerBedEnterEvent> h) {
        reg.register(PlayerBedEnterEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings bedLeave(Consumer<PlayerBedLeaveEvent> h) {
        reg.register(PlayerBedLeaveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings eggThrow(Consumer<PlayerEggThrowEvent> h) {
        reg.register(PlayerEggThrowEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings arrowPickup(Consumer<PlayerPickupArrowEvent> h) {
        reg.register(PlayerPickupArrowEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings swapHands(Consumer<PlayerSwapHandItemsEvent> h) {
        reg.register(PlayerSwapHandItemsEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings riptide(Consumer<PlayerRiptideEvent> h) {
        reg.register(PlayerRiptideEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings harvest(Consumer<PlayerHarvestBlockEvent> h) {
        reg.register(PlayerHarvestBlockEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings advancementDone(Consumer<PlayerAdvancementDoneEvent> h) {
        reg.register(PlayerAdvancementDoneEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings recipeDiscover(Consumer<PlayerRecipeDiscoverEvent> h) {
        reg.register(PlayerRecipeDiscoverEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings resourcePack(Consumer<PlayerResourcePackStatusEvent> h) {
        reg.register(PlayerResourcePackStatusEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings armorStandManipulate(Consumer<PlayerArmorStandManipulateEvent> h) {
        reg.register(PlayerArmorStandManipulateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public PlayerEventBindings unleashEntity(Consumer<PlayerUnleashEntityEvent> h) {
        reg.register(PlayerUnleashEntityEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
