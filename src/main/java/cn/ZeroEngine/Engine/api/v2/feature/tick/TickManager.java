package cn.ZeroEngine.Engine.api.v2.feature.tick;

import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TickManager {

    public static final int TICKS_PER_SECOND = 100;
    public static final long TICK_INTERVAL_MS = 10L;

    private final JavaPlugin plugin;
    private final ScheduledExecutorService executor;
    private final AtomicLong currentTick = new AtomicLong(0);
    private final Map<Long, TickEntry> tasks = new ConcurrentHashMap<>();
    private long nextId = 1;
    private volatile boolean running = false;

    private static class TickEntry {
        final long id;
        final TickTask task;
        final long startTick;
        final long period;
        final boolean repeat;

        TickEntry(long id, TickTask task, long startTick, long period, boolean repeat) {
            this.id = id;
            this.task = task;
            this.startTick = startTick;
            this.period = period;
            this.repeat = repeat;
        }
    }

    public TickManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SF-TickThread");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (running) return;
        running = true;
        executor.scheduleAtFixedRate(this::doTick, 0, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        running = false;
        executor.shutdownNow();
        tasks.clear();
    }

    private void doTick() {
        if (!running) return;
        long tick = currentTick.incrementAndGet();
        for (TickEntry entry : tasks.values()) {
            long elapsed = tick - entry.startTick;
            if (elapsed < 0) continue;
            if (entry.repeat) {
                if (elapsed % entry.period == 0) {
                    safeRun(entry.task, tick);
                }
            } else {
                if (elapsed == entry.period) {
                    safeRun(entry.task, tick);
                    tasks.remove(entry.id);
                }
            }
        }
    }

    private void safeRun(TickTask task, long tick) {
        try {
            task.tick(tick);
        } catch (Throwable t) {
            SF sf = SF.sf();
            sf.error("[Tick] Task error at sfTick=" + tick, t);
        }
    }

    public long now() {
        return currentTick.get();
    }

    public long nextTick() {
        return currentTick.get() + 1;
    }

    public long toSeconds(long sfTicks) {
        return sfTicks / TICKS_PER_SECOND;
    }

    public long fromSeconds(long seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public long toBukkitTicks(long sfTicks) {
        return sfTicks / 5;
    }

    public long fromBukkitTicks(long bukkitTicks) {
        return bukkitTicks * 5;
    }

    public long runLater(TickTask task, long delayTicks) {
        long id = nextId++;
        tasks.put(id, new TickEntry(id, task, currentTick.get(), delayTicks, false));
        return id;
    }

    public long runTimer(TickTask task, long delayTicks, long periodTicks) {
        long id = nextId++;
        tasks.put(id, new TickEntry(id, task, currentTick.get() + delayTicks, periodTicks, true));
        return id;
    }

    public long runTimer(TickTask task, long periodTicks) {
        return runTimer(task, 0, periodTicks);
    }

    public boolean cancel(long id) {
        return tasks.remove(id) != null;
    }

    public void cancelAll() {
        tasks.clear();
    }

    public int activeTasks() {
        return tasks.size();
    }

    public void runSync(Runnable r) {
        plugin.getServer().getScheduler().runTask(plugin, r);
    }

    public void runSyncLater(Runnable r, long sfTicks) {
        long bukkitTicks = Math.max(1, toBukkitTicks(sfTicks));
        plugin.getServer().getScheduler().runTaskLater(plugin, r, bukkitTicks);
    }

    public void runSyncTimer(Runnable r, long delayTicks, long periodTicks) {
        long bukkitDelay = Math.max(1, toBukkitTicks(delayTicks));
        long bukkitPeriod = Math.max(1, toBukkitTicks(periodTicks));
        plugin.getServer().getScheduler().runTaskTimer(plugin, r, bukkitDelay, bukkitPeriod);
    }
}
