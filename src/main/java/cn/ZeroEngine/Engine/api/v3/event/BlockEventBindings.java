package cn.ZeroEngine.Engine.api.v3.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;

import java.util.function.Consumer;

public final class BlockEventBindings {

    private final EventRegistrar reg;

    BlockEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public BlockEventBindings break_(Consumer<BlockBreakEvent> h) {
        reg.register(BlockBreakEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings place(Consumer<BlockPlaceEvent> h) {
        reg.register(BlockPlaceEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings burn(Consumer<BlockBurnEvent> h) {
        reg.register(BlockBurnEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings ignite(Consumer<BlockIgniteEvent> h) {
        reg.register(BlockIgniteEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings redstone(Consumer<BlockRedstoneEvent> h) {
        reg.register(BlockRedstoneEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings grow(Consumer<BlockGrowEvent> h) {
        reg.register(BlockGrowEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings fromTo(Consumer<BlockFromToEvent> h) {
        reg.register(BlockFromToEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings physics(Consumer<BlockPhysicsEvent> h) {
        reg.register(BlockPhysicsEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings fade(Consumer<BlockFadeEvent> h) {
        reg.register(BlockFadeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings form(Consumer<BlockFormEvent> h) {
        reg.register(BlockFormEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings spread(Consumer<BlockSpreadEvent> h) {
        reg.register(BlockSpreadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings damage(Consumer<BlockDamageEvent> h) {
        reg.register(BlockDamageEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings dispense(Consumer<BlockDispenseEvent> h) {
        reg.register(BlockDispenseEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings canBuild(Consumer<BlockCanBuildEvent> h) {
        reg.register(BlockCanBuildEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings exp(Consumer<BlockExpEvent> h) {
        reg.register(BlockExpEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings multiPlace(Consumer<BlockMultiPlaceEvent> h) {
        reg.register(BlockMultiPlaceEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings leavesDecay(Consumer<LeavesDecayEvent> h) {
        reg.register(LeavesDecayEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings signChange(Consumer<SignChangeEvent> h) {
        reg.register(SignChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings notePlay(Consumer<NotePlayEvent> h) {
        reg.register(NotePlayEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings pistonExtend(Consumer<BlockPistonExtendEvent> h) {
        reg.register(BlockPistonExtendEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings pistonRetract(Consumer<BlockPistonRetractEvent> h) {
        reg.register(BlockPistonRetractEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings fluidLevel(Consumer<FluidLevelChangeEvent> h) {
        reg.register(FluidLevelChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public BlockEventBindings moisture(Consumer<MoistureChangeEvent> h) {
        reg.register(MoistureChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
