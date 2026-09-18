package cn.ZeroEngine.Engine.api.v3.feature.perf;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class PerformanceManager {

    private final JavaPlugin plugin;
    private final MemoryMXBean memoryMX = ManagementFactory.getMemoryMXBean();

    private boolean memoryMonitor = true;
    private boolean chunkManager = true;
    private boolean entityCleaner = true;
    private boolean autoThrottle = true;

    private double memoryWarnThreshold = 0.85;
    private double memoryGcThreshold = 0.90;
    private int chunkIdleTicks = 6000;
    private int chunkMaxIdlePerCycle = 50;
    private int itemExpireTicks = 1200;
    private int projectileExpireTicks = 200;
    private int maxEntitiesPerChunk = 50;

    private double tpsWarnThreshold = 18.0;
    private double tpsCriticalThreshold = 15.0;
    private int minViewDistance = 4;
    private int maxViewDistance = 32;
    private int minSimulationDistance = 3;
    private int maxSimulationDistance = 32;

    private long monitorIntervalTicks = 200;
    private long cleanupIntervalTicks = 600;

    private volatile double lastTps1m = 20.0;
    private volatile double lastTps5m = 20.0;
    private volatile double lastMspt = 0.0;
    private volatile long lastUsedMemory = 0;
    private volatile long lastMaxMemory = 0;
    private volatile long lastGcCount = 0;
    private volatile int lastChunkCount = 0;
    private volatile int lastEntityCount = 0;
    private volatile int chunksUnloadedTotal = 0;
    private volatile int entitiesRemovedTotal = 0;
    private volatile int gcTriggeredTotal = 0;

    private long monitorTaskId = -1;
    private long cleanupTaskId = -1;
    private long throttleTaskId = -1;

    private int currentViewDistance = -1;
    private int currentSimulationDistance = -1;

    public PerformanceManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        SF sf = SF.sf();
        sf.info("[Perf] Performance system starting...");

        monitorTaskId = sf.tick().runTimer(this::runMonitor, monitorIntervalTicks);
        cleanupTaskId = sf.tick().runTimer(this::runCleanupSync, cleanupIntervalTicks);
        throttleTaskId = sf.tick().runTimer(this::runAutoThrottle, monitorIntervalTicks);

        sf.tick().runSync(() -> {
            for (World w : Bukkit.getWorlds()) {
                currentViewDistance = w.getViewDistance();
                currentSimulationDistance = w.getSimulationDistance();
                break;
            }
        });

        sf.info("[Perf] Memory monitor: " + memoryMonitor);
        sf.info("[Perf] Chunk manager: " + chunkManager);
        sf.info("[Perf] Entity cleaner: " + entityCleaner);
        sf.info("[Perf] Auto throttle: " + autoThrottle);
        sf.info("[Perf] Performance system initialized");
    }

    public void shutdown() {
        SF sf = SF.sf();
        if (monitorTaskId != -1) sf.tick().cancel(monitorTaskId);
        if (cleanupTaskId != -1) sf.tick().cancel(cleanupTaskId);
        if (throttleTaskId != -1) sf.tick().cancel(throttleTaskId);
        sf.info("[Perf] Performance system stopped");
    }

    private void runMonitor(long sfTick) {
        SF sf = SF.sf();
        try {
            collectStats();
            if (memoryMonitor) {
                checkMemory();
            }
        } catch (Throwable t) {
            sf.error("[Perf] Monitor error", t);
        }
    }

    private void collectStats() {
        try {
            var server = Bukkit.getServer();
            double[] tps = server.getTPS();
            if (tps != null && tps.length >= 2) {
                lastTps1m = tps[0];
                lastTps5m = tps[1];
            }
        } catch (Throwable ignored) {}

        try {
            lastMspt = Bukkit.getServer().getAverageTickTime();
        } catch (Throwable ignored) {}

        MemoryUsage heap = memoryMX.getHeapMemoryUsage();
        lastUsedMemory = heap.getUsed();
        lastMaxMemory = heap.getMax();

        try {
            long gcCount = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(b -> b.getCollectionCount() > 0 ? b.getCollectionCount() : 0)
                    .sum();
            lastGcCount = gcCount;
        } catch (Throwable ignored) {}

        lastChunkCount = 0;
        lastEntityCount = 0;
        for (World w : Bukkit.getWorlds()) {
            lastChunkCount += w.getLoadedChunks().length;
            lastEntityCount += w.getEntityCount();
        }
    }

    private void checkMemory() {
        SF sf = SF.sf();
        if (lastMaxMemory <= 0) return;
        double ratio = (double) lastUsedMemory / lastMaxMemory;

        if (ratio >= memoryGcThreshold) {
            sf.warn("[Perf] Memory critical: %.1f%% (%d/%d MB), triggering GC",
                    ratio * 100, lastUsedMemory / 1048576, lastMaxMemory / 1048576);
            System.gc();
            gcTriggeredTotal++;
        } else if (ratio >= memoryWarnThreshold) {
            sf.warn("[Perf] Memory high: %.1f%% (%d/%d MB)",
                    ratio * 100, lastUsedMemory / 1048576, lastMaxMemory / 1048576);
        }
    }

    private void runCleanupSync(long sfTick) {
        SF sf = SF.sf();
        sf.tick().runSync(() -> {
            try {
                if (chunkManager) cleanIdleChunks();
                if (entityCleaner) cleanEntities();
            } catch (Throwable t) {
                sf.error("[Perf] Cleanup error", t);
            }
        });
    }

    private void cleanIdleChunks() {
        SF sf = SF.sf();
        int unloaded = 0;
        for (World world : Bukkit.getWorlds()) {
            if (world.getPlayers().isEmpty()) continue;

            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk chunk : chunks) {
                if (unloaded >= chunkMaxIdlePerCycle) break;
                if (isChunkIdle(chunk)) {
                    if (world.unloadChunk(chunk)) {
                        unloaded++;
                    }
                }
            }
        }
        if (unloaded > 0) {
            chunksUnloadedTotal += unloaded;
            sf.info("[Perf] Unloaded %d idle chunks (total: %d)", unloaded, chunksUnloadedTotal);
        }
    }

    private boolean isChunkIdle(Chunk chunk) {
        if (chunk.getEntities().length > 0) return false;
        for (Entity e : chunk.getEntities()) {
            if (e instanceof org.bukkit.entity.Player) return false;
        }
        return true;
    }

    private void cleanEntities() {
        SF sf = SF.sf();
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    int age = item.getTicksLived();
                    if (age > itemExpireTicks) {
                        item.remove();
                        removed++;
                    }
                } else if (entity instanceof Projectile proj) {
                    int age = proj.getTicksLived();
                    if (age > projectileExpireTicks && proj.getShooter() == null) {
                        proj.remove();
                        removed++;
                    }
                }
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                Entity[] entities = chunk.getEntities();
                if (entities.length > maxEntitiesPerChunk) {
                    int toRemove = entities.length - maxEntitiesPerChunk;
                    for (Entity e : entities) {
                        if (toRemove <= 0) break;
                        if (e instanceof Item || e instanceof Projectile) {
                            e.remove();
                            removed++;
                            toRemove--;
                        }
                    }
                }
            }
        }
        if (removed > 0) {
            entitiesRemovedTotal += removed;
            sf.info("[Perf] Cleaned %d entities (total: %d)", removed, entitiesRemovedTotal);
        }
    }

    private void runAutoThrottle(long sfTick) {
        SF sf = SF.sf();
        if (!autoThrottle) return;

        double tps = lastTps1m;
        int targetViewDist;
        int targetSimDist;

        if (tps < tpsCriticalThreshold) {
            targetViewDist = minViewDistance;
            targetSimDist = minSimulationDistance;
            sf.warn("[Perf] TPS critical (%.1f), throttling view/sim distance", tps);
        } else if (tps < tpsWarnThreshold) {
            int current = currentViewDistance > 0 ? currentViewDistance : maxViewDistance;
            targetViewDist = Math.max(minViewDistance, current - 2);
            int currentSim = currentSimulationDistance > 0 ? currentSimulationDistance : maxSimulationDistance;
            targetSimDist = Math.max(minSimulationDistance, currentSim - 1);
        } else {
            targetViewDist = maxViewDistance;
            targetSimDist = maxSimulationDistance;
        }

        if (targetViewDist != currentViewDistance || targetSimDist != currentSimulationDistance) {
            final int fv = targetViewDist;
            final int fs = targetSimDist;
            sf.tick().runSync(() -> {
                for (World w : Bukkit.getWorlds()) {
                    try {
                        if (w.getViewDistance() != fv) {
                            w.setViewDistance(fv);
                        }
                        if (w.getSimulationDistance() != fs) {
                            w.setSimulationDistance(fs);
                        }
                    } catch (Throwable t) {
                        sf.error("[Perf] Failed to adjust world distance", t);
                    }
                }
            });
            currentViewDistance = targetViewDist;
            currentSimulationDistance = targetSimDist;
            sf.info("[Perf] View distance -> %d, Simulation distance -> %d", targetViewDist, targetSimDist);
        }
    }

    public String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6===== SF Performance Report =====\n");
        sb.append(String.format("§eTPS: §a%.2f§7/20 (1m) §a%.2f§7/20 (5m)\n", lastTps1m, lastTps5m));
        sb.append(String.format("§eMSPT: §a%.2f§7ms\n", lastMspt));
        sb.append(String.format("§eMemory: §a%d§7/§a%d §7MB §a(%.1f%%)\n",
                lastUsedMemory / 1048576, lastMaxMemory / 1048576,
                lastMaxMemory > 0 ? (double) lastUsedMemory / lastMaxMemory * 100 : 0));
        sb.append(String.format("§eGC count: §a%d\n", lastGcCount));
        sb.append(String.format("§eLoaded chunks: §a%d\n", lastChunkCount));
        sb.append(String.format("§eTotal entities: §a%d\n", lastEntityCount));
        sb.append(String.format("§eView dist: §a%d §7| §eSim dist: §a%d\n", currentViewDistance, currentSimulationDistance));
        sb.append("§6----- Cumulative -----\n");
        sb.append(String.format("§eChunks unloaded: §a%d\n", chunksUnloadedTotal));
        sb.append(String.format("§eEntities removed: §a%d\n", entitiesRemovedTotal));
        sb.append(String.format("§eGC triggered: §a%d\n", gcTriggeredTotal));
        sb.append("§6----- Features -----\n");
        sb.append(String.format("§eMemory monitor: %s\n", memoryMonitor ? "§aON" : "§cOFF"));
        sb.append(String.format("§eChunk manager: %s\n", chunkManager ? "§aON" : "§cOFF"));
        sb.append(String.format("§eEntity cleaner: %s\n", entityCleaner ? "§aON" : "§cOFF"));
        sb.append(String.format("§eAuto throttle: %s\n", autoThrottle ? "§aON" : "§cOFF"));
        sb.append("§6=================================");
        return sb.toString();
    }

    public void manualGc() {
        SF sf = SF.sf();
        sf.info("[Perf] Manual GC triggered");
        long before = Runtime.getRuntime().freeMemory();
        System.gc();
        long after = Runtime.getRuntime().freeMemory();
        sf.info("[Perf] GC freed %d MB", (after - before) / 1048576);
        gcTriggeredTotal++;
    }

    public void manualChunkCleanup() {
        SF sf = SF.sf();
        sf.tick().runSync(() -> {
            int before = lastChunkCount;
            cleanIdleChunks();
            collectStats();
            sf.info("[Perf] Manual chunk cleanup done: %d -> %d", before, lastChunkCount);
        });
    }

    public void manualEntityCleanup() {
        SF sf = SF.sf();
        sf.tick().runSync(() -> {
            int before = lastEntityCount;
            cleanEntities();
            collectStats();
            sf.info("[Perf] Manual entity cleanup done: %d -> %d", before, lastEntityCount);
        });
    }

    public boolean toggleMemoryMonitor() { memoryMonitor = !memoryMonitor; return memoryMonitor; }
    public boolean toggleChunkManager() { chunkManager = !chunkManager; return chunkManager; }
    public boolean toggleEntityCleaner() { entityCleaner = !entityCleaner; return entityCleaner; }
    public boolean toggleAutoThrottle() { autoThrottle = !autoThrottle; return autoThrottle; }

    public double getLastTps1m() { return lastTps1m; }
    public double getLastTps5m() { return lastTps5m; }
    public double getLastMspt() { return lastMspt; }
    public long getLastUsedMemory() { return lastUsedMemory; }
    public long getLastMaxMemory() { return lastMaxMemory; }
    public int getLastChunkCount() { return lastChunkCount; }
    public int getLastEntityCount() { return lastEntityCount; }
    public int getChunksUnloadedTotal() { return chunksUnloadedTotal; }
    public int getEntitiesRemovedTotal() { return entitiesRemovedTotal; }
    public int getGcTriggeredTotal() { return gcTriggeredTotal; }
}
