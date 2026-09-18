package cn.ZeroEngine.Engine.api.v2.event;

import org.bukkit.event.EventPriority;
import org.bukkit.event.world.*;

import java.util.function.Consumer;

public final class WorldEventBindings {

    private final EventRegistrar reg;

    WorldEventBindings(EventRegistrar reg) {
        this.reg = reg;
    }

    public WorldEventBindings load(Consumer<WorldLoadEvent> h) {
        reg.register(WorldLoadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings unload(Consumer<WorldUnloadEvent> h) {
        reg.register(WorldUnloadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings save(Consumer<WorldSaveEvent> h) {
        reg.register(WorldSaveEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings init(Consumer<WorldInitEvent> h) {
        reg.register(WorldInitEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings chunkLoad(Consumer<ChunkLoadEvent> h) {
        reg.register(ChunkLoadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings chunkUnload(Consumer<ChunkUnloadEvent> h) {
        reg.register(ChunkUnloadEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings chunkPopulate(Consumer<ChunkPopulateEvent> h) {
        reg.register(ChunkPopulateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings spawnChange(Consumer<SpawnChangeEvent> h) {
        reg.register(SpawnChangeEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings portalCreate(Consumer<PortalCreateEvent> h) {
        reg.register(PortalCreateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings structureGrow(Consumer<StructureGrowEvent> h) {
        reg.register(StructureGrowEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings timeSkip(Consumer<TimeSkipEvent> h) {
        reg.register(TimeSkipEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings lootGenerate(Consumer<LootGenerateEvent> h) {
        reg.register(LootGenerateEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }

    public WorldEventBindings genericGameEvent(Consumer<GenericGameEvent> h) {
        reg.register(GenericGameEvent.class, EventPriority.NORMAL, false, h);
        return this;
    }
}
