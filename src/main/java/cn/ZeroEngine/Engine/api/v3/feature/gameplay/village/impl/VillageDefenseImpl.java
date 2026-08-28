package cn.ZeroEngine.Engine.api.v3.feature.gameplay.village.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.village.VillageDefense;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class VillageDefenseImpl implements VillageDefense, Listener {

    private final JavaPlugin plugin;
    private final Map<String, ArenaImpl> arenas = new ConcurrentHashMap<>();
    private final Map<String, GameImpl> games = new ConcurrentHashMap<>();
    private final List<Consumer<VillageEvent>> listeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<Consumer>> typedListeners = new ConcurrentHashMap<>();
    private final Map<BuildingType, Map<Integer, Map<String, Integer>>> buildingCosts = new ConcurrentHashMap<>();
    private final Map<BuildingType, Map<Integer, Map<String, Object>>> buildingStatsMap = new ConcurrentHashMap<>();
    private final Map<UnitType, Map<String, Integer>> unitCosts = new ConcurrentHashMap<>();
    private final Map<UnitType, Map<String, Object>> unitStats = new ConcurrentHashMap<>();
    private final Map<EnemyType, Map<String, Object>> enemyStats = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, List<EnemySpawn>>> waveSpawnsRegistry = new ConcurrentHashMap<>();
    private final Map<Integer, Map<String, Integer>> waveRewards = new ConcurrentHashMap<>();
    private final Map<EnemyType, KillReward> killRewards = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Map<String, Integer>>> playerStats = new ConcurrentHashMap<>();
    private final Map<String, Map<BuildingType, Integer>> buildLimits = new ConcurrentHashMap<>();
    private BukkitTask tickTask;

    static final String STAT_WAVES = "waves";
    static final String STAT_BEST_WAVE = "best_wave";
    static final String STAT_BEST_SCORE = "best_score";
    static final String STAT_BUILDINGS = "buildings_built";
    static final String STAT_UNITS = "units_spawned";
    static final String STAT_PLAYED = "games_played";
    static final String STAT_WON = "games_won";
    static final String STAT_KILLS = "total_kills";

    public VillageDefenseImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        initDefaults();
    }

    public void start() {
        if (tickTask != null) return;
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
    }

    private void initDefaults() {
        for (BuildingType t : BuildingType.values()) {
            Map<Integer, Map<String, Integer>> lvl = new ConcurrentHashMap<>();
            Map<String, Integer> c1 = new HashMap<>();
            switch (t) {
                case CORE: c1.put("gold", 0); break;
                case TOWER_ARROW: c1.put("gold", 100); c1.put("wood", 50); break;
                case TOWER_MAGIC: c1.put("gold", 200); c1.put("iron", 30); break;
                case TOWER_CANNON: c1.put("gold", 300); c1.put("stone", 80); break;
                case WALL: c1.put("stone", 20); break;
                case GATE: c1.put("wood", 40); c1.put("iron", 10); break;
                case GOLD_MINE: c1.put("wood", 60); c1.put("stone", 40); break;
                case LUMBER_CAMP: c1.put("gold", 50); c1.put("stone", 20); break;
                case BARRACKS: c1.put("gold", 250); c1.put("wood", 100); c1.put("stone", 80); break;
                case BLACKSMITH: c1.put("gold", 200); c1.put("iron", 50); break;
                case WELL: c1.put("stone", 50); break;
                case FARM: c1.put("wood", 40); c1.put("gold", 30); break;
                case VILLAGER_HOUSE: c1.put("wood", 60); c1.put("stone", 30); break;
            }
            lvl.put(1, c1);
            buildingCosts.put(t, lvl);
        }
        Map<String, Integer> gCost = new HashMap<>(); gCost.put("gold", 20); unitCosts.put(UnitType.VILLAGER, gCost);
        Map<String, Integer> gdCost = new HashMap<>(); gdCost.put("gold", 50); gdCost.put("iron", 5); unitCosts.put(UnitType.GUARD, gdCost);
        Map<String, Integer> arCost = new HashMap<>(); arCost.put("gold", 60); arCost.put("wood", 10); unitCosts.put(UnitType.ARCHER, arCost);
        Map<String, Integer> knCost = new HashMap<>(); knCost.put("gold", 100); knCost.put("iron", 20); unitCosts.put(UnitType.KNIGHT, knCost);
        Map<String, Integer> mgCost = new HashMap<>(); mgCost.put("gold", 120); mgCost.put("iron", 30); unitCosts.put(UnitType.MAGE, mgCost);
        Map<String, Integer> hlCost = new HashMap<>(); hlCost.put("gold", 80); hlCost.put("iron", 10); unitCosts.put(UnitType.HEALER, hlCost);
        Map<String, Integer> wkCost = new HashMap<>(); wkCost.put("gold", 30); wkCost.put("wood", 20); unitCosts.put(UnitType.WORKER, wkCost);
        for (EnemyType et : EnemyType.values()) {
            Map<String, Object> m = new HashMap<>();
            switch (et) {
                case RAIDER: m.put("health", 20.0); m.put("damage", 3.0); m.put("speed", 1.0); break;
                case ARCHER: m.put("health", 16.0); m.put("damage", 2.0); m.put("speed", 1.0); break;
                case GRUNT: m.put("health", 40.0); m.put("damage", 5.0); m.put("speed", 0.9); break;
                case BRUTE: m.put("health", 80.0); m.put("damage", 8.0); m.put("speed", 0.8); break;
                case SHAMAN: m.put("health", 30.0); m.put("damage", 4.0); m.put("speed", 1.0); m.put("heal", 5.0); break;
                case BOSS_WARCHIEF: m.put("health", 300.0); m.put("damage", 15.0); m.put("speed", 0.9); break;
                case BOSS_BEHEMOTH: m.put("health", 600.0); m.put("damage", 20.0); m.put("speed", 0.7); break;
            }
            enemyStats.put(et, m);
        }
    }

    private void tick() {
        for (GameImpl g : games.values()) {
            try { g.tick(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void registerArena(String id, String name, String world, int minPlayers, int maxPlayers,
                              Location lobbyLocation, Location spectatorLocation, Location coreSpawn,
                              List<Location> playerSpawns, List<Location> enemySpawnPoints,
                              int mapRadius, int maxWaves, int buildPhaseSeconds,
                              int waveIntervalSeconds, double coreMaxHealth, boolean allowBuild) {
        arenas.put(id, new ArenaImpl(id, name, world, minPlayers, maxPlayers,
                lobbyLocation, spectatorLocation, coreSpawn, playerSpawns, enemySpawnPoints,
                mapRadius, maxWaves, buildPhaseSeconds, waveIntervalSeconds, coreMaxHealth, allowBuild));
    }

    @Override
    public void unregisterArena(String id) { arenas.remove(id); }

    @Override
    public Arena getArena(String id) { return arenas.get(id); }

    @Override
    public Collection<Arena> allArenas() { return Collections.unmodifiableCollection(arenas.values()); }

    @Override
    public Game createGame(String arenaId) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return null;
        GameImpl g = new GameImpl(a);
        games.put(arenaId, g);
        return g;
    }

    @Override
    public Game getGame(String arenaId) { return games.get(arenaId); }

    @Override
    public Game gameOf(Player p) {
        for (GameImpl g : games.values()) {
            if (g.playerGame.containsKey(p.getUniqueId())) return g;
        }
        return null;
    }

    @Override
    public boolean join(Player p, String arenaId) {
        GameImpl g = games.get(arenaId);
        if (g == null) {
            ArenaImpl a = arenas.get(arenaId);
            if (a == null) return false;
            g = new GameImpl(a);
            games.put(arenaId, g);
        }
        if (g.state != GameState.WAITING && g.state != GameState.COUNTDOWN) return false;
        if (g.defenders.size() >= g.arena.maxPlayers) return false;
        DefenderImpl d = new DefenderImpl(p);
        g.defenders.put(p.getUniqueId(), d);
        g.playerGame.put(p.getUniqueId(), g);
        incrementStat(arenaId, p, STAT_PLAYED, 1);
        Location spawn = g.arena.lobbyLocation != null ? g.arena.lobbyLocation : p.getLocation();
        p.teleport(spawn);
        fireEvent(new VillageEventImpl(VillageEvent.Type.PLAYER_JOIN, g, p, d));
        return true;
    }

    @Override
    public boolean leave(Player p) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return false; GameImpl g = (GameImpl) _g;
        if (g == null) return false;
        DefenderImpl d = g.defenders.remove(p.getUniqueId());
        g.playerGame.remove(p.getUniqueId());
        p.teleport(g.arena.spectatorLocation);
        fireEvent(new VillageEventImpl(VillageEvent.Type.PLAYER_LEAVE, g, p, d));
        return true;
    }

    @Override
    public boolean startCountdown(String arenaId, int seconds) {
        GameImpl g = games.get(arenaId);
        if (g == null || g.state != GameState.WAITING) return false;
        g.state = GameState.COUNTDOWN;
        g.timer = seconds * 20;
        g.countdownTarget = seconds * 20;
        return true;
    }

    @Override
    public void forceStart(String arenaId) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        g.startBuildPhase();
    }

    @Override
    public void forceEnd(String arenaId, boolean victory) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        g.endInternal(victory);
    }

    @Override
    public Building build(Player p, BuildingType type, Location loc) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return null; GameImpl g = (GameImpl) _g;
        if (g == null) return null;
        if (g.state != GameState.BUILD_PHASE && g.state != GameState.WAVE_ACTIVE
                && g.state != GameState.WAVE_INTERVAL) return null;
        if (!g.arena.allowBuild && type != BuildingType.CORE) return null;
        DefenderImpl d = g.defenders.get(p.getUniqueId());
        if (d == null) return null;
        Map<String, Integer> cost = getBuildingCost(type, 1);
        if (cost != null && !d.spendResources(cost)) return null;
        int limit = getBuildLimitPerPlayer(g.arena.id(), type);
        if (limit > 0) {
            long cnt = d.buildings.stream().filter(b -> b.type() == type).count();
            if (cnt >= limit) return null;
        }
        BuildingImpl b = new BuildingImpl(g, type, loc, 1, d);
        g.buildings.add(b);
        d.buildings.add(b);
        if (type == BuildingType.CORE) {
            g.coreBuilding = b;
            g.coreHealth = b.maxHealth();
        }
        fireEvent(new VillageEventImpl(VillageEvent.Type.BUILDING_BUILT, g, p, b));
        incrementStat(g.arena.id(), p, STAT_BUILDINGS, 1);
        return b;
    }

    @Override
    public boolean demolish(Player p, Building building) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return false; GameImpl g = (GameImpl) _g;
        if (g == null || !(building instanceof BuildingImpl)) return false;
        BuildingImpl b = (BuildingImpl) building;
        if (b.game != g) return false;
        if (b.type() == BuildingType.CORE) return false;
        g.buildings.remove(b);
        DefenderImpl d = g.defenders.get(p.getUniqueId());
        if (d != null) d.buildings.remove(b);
        fireEvent(new VillageEventImpl(VillageEvent.Type.BUILDING_DEMOLISHED, g, p, b));
        return true;
    }

    @Override
    public boolean upgradeBuilding(Player p, Building building) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return false; GameImpl g = (GameImpl) _g;
        if (g == null || !(building instanceof BuildingImpl)) return false;
        BuildingImpl b = (BuildingImpl) building;
        if (b.game != g) return false;
        if (b.level >= b.maxLevel) return false;
        int nextLevel = b.level + 1;
        Map<String, Integer> cost = getBuildingCost(b.type, nextLevel);
        DefenderImpl d = b.owner;
        if (d == null) d = g.defenders.get(p.getUniqueId());
        if (cost != null && (d == null || !d.spendResources(cost))) return false;
        b.level = nextLevel;
        b.applyStats();
        fireEvent(new VillageEventImpl(VillageEvent.Type.BUILDING_UPGRADED, g, p, b));
        return true;
    }

    @Override
    public boolean repairBuilding(Player p, Building building, double amount) {
        if (!(building instanceof BuildingImpl)) return false;
        BuildingImpl b = (BuildingImpl) building;
        if (b.health >= b.maxHealth) return false;
        b.health = Math.min(b.maxHealth, b.health + amount);
        return true;
    }

    @Override
    public Unit spawnUnit(Game g, UnitType type, Location spawn, Building home) {
        if (!(g instanceof GameImpl)) return null;
        GameImpl gi = (GameImpl) g;
        Map<String, Integer> cost = getUnitCost(type);
        DefenderImpl owner = null;
        if (home instanceof BuildingImpl) owner = ((BuildingImpl)home).owner;
        if (owner != null && cost != null && !owner.spendResources(cost)) return null;
        UnitImpl u = new UnitImpl(gi, type, spawn != null ? spawn : gi.arena.coreSpawn, home);
        gi.units.add(u);
        if (owner != null) owner.units.add(u);
        incrementStat(gi.arena.id(), u.uuid(), STAT_UNITS, 1);
        fireEvent(new VillageEventImpl(VillageEvent.Type.UNIT_SPAWNED, gi, null, u));
        return u;
    }

    @Override
    public boolean removeUnit(Unit unit) {
        if (!(unit instanceof UnitImpl)) return false;
        UnitImpl u = (UnitImpl) unit;
        if (u.game != null) u.game.units.remove(u);
        if (u.bukkitEntity != null) u.bukkitEntity.remove();
        return true;
    }

    @Override
    public void setBuildingCost(BuildingType type, Map<String, Integer> cost) {
        buildingCosts.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(1, cost);
    }

    @Override
    public Map<String, Integer> getBuildingCost(BuildingType type, int level) {
        Map<Integer, Map<String, Integer>> lv = buildingCosts.get(type);
        if (lv == null) return null;
        Map<String, Integer> c = lv.get(level);
        if (c != null) return c;
        Map<String, Integer> base = lv.get(1);
        if (base == null) return null;
        double mul = Math.pow(1.5, level - 1);
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<String, Integer> e : base.entrySet()) {
            out.put(e.getKey(), (int) Math.round(e.getValue() * mul));
        }
        return out;
    }

    @Override
    public void setBuildingStats(BuildingType type, int level, Map<String, Object> stats) {
        buildingStatsMap.computeIfAbsent(type, k -> new ConcurrentHashMap<>()).put(level, stats);
    }

    @Override
    public Map<String, Object> getBuildingStats(BuildingType type, int level) {
        Map<Integer, Map<String, Object>> lv = buildingStatsMap.get(type);
        if (lv == null) return null;
        Map<String, Object> c = lv.get(level);
        if (c != null) return c;
        return lv.get(1);
    }

    @Override
    public void setUnitCost(UnitType type, Map<String, Integer> cost) { unitCosts.put(type, cost); }

    @Override
    public Map<String, Integer> getUnitCost(UnitType type) { return unitCosts.get(type); }

    @Override
    public void setUnitStats(UnitType type, Map<String, Object> stats) { unitStats.put(type, stats); }

    @Override
    public Map<String, Object> getUnitStats(UnitType type) { return unitStats.get(type); }

    @Override
    public void setEnemyStats(EnemyType type, Map<String, Object> stats) { enemyStats.put(type, stats); }

    @Override
    public Map<String, Object> getEnemyStats(EnemyType type) { return enemyStats.get(type); }

    @Override
    public void addWaveSpawn(String arenaId, int waveNumber, EnemySpawn spawn) {
        waveSpawnsRegistry.computeIfAbsent(arenaId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(waveNumber, k -> new CopyOnWriteArrayList<>()).add(spawn);
    }

    @Override
    public void removeWaveSpawns(String arenaId, int waveNumber) {
        Map<Integer, List<EnemySpawn>> m = waveSpawnsRegistry.get(arenaId);
        if (m != null) m.remove(waveNumber);
    }

    @Override
    public void grantResource(Player p, String type, int amount) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return; GameImpl g = (GameImpl) _g;
        DefenderImpl d = g != null ? g.defenders.get(p.getUniqueId()) : null;
        if (d == null) return;
        d.addResource(type, amount);
        fireEvent(new VillageEventImpl(VillageEvent.Type.RESOURCE_GAIN, g, p,
                Collections.singletonMap(type, amount)));
    }

    @Override
    public boolean spendResource(Player p, String type, int amount) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return false; GameImpl g = (GameImpl) _g;
        DefenderImpl d = g != null ? g.defenders.get(p.getUniqueId()) : null;
        if (d == null) return false;
        Map<String, Integer> c = Collections.singletonMap(type, amount);
        if (!d.spendResources(c)) return false;
        fireEvent(new VillageEventImpl(VillageEvent.Type.RESOURCE_SPEND, g, p, c));
        return true;
    }

    @Override
    public int getResource(Player p, String type) {
        Game _g = gameOf(p); if (!(_g instanceof GameImpl)) return 0; GameImpl g = (GameImpl) _g;
        DefenderImpl d = g != null ? g.defenders.get(p.getUniqueId()) : null;
        if (d == null) return 0;
        return d.resources.getOrDefault(type, 0);
    }

    @Override
    public List<ItemStack> getBuildingDrops(BuildingType type, int level) {
        List<ItemStack> drops = new ArrayList<>();
        Material m = Material.AIR;
        switch (type) {
            case WALL: case CORE: m = Material.STONE; break;
            case GATE: case LUMBER_CAMP: case VILLAGER_HOUSE: case FARM: m = Material.OAK_PLANKS; break;
            case TOWER_ARROW: case TOWER_MAGIC: case TOWER_CANNON: case BARRACKS: m = Material.COBBLESTONE; break;
            case GOLD_MINE: m = Material.GOLD_INGOT; break;
            case BLACKSMITH: m = Material.IRON_INGOT; break;
            case WELL: m = Material.COBBLESTONE; break;
        }
        if (m != Material.AIR) drops.add(new ItemStack(m, Math.max(1, level * 2)));
        return drops;
    }

    @Override
    public void registerWaveReward(int wave, Map<String, Integer> resourcesPerPlayer, int scorePerPlayer) {
        Map<String, Integer> m = new HashMap<>();
        if (resourcesPerPlayer != null) m.putAll(resourcesPerPlayer);
        m.put("__score__", scorePerPlayer);
        waveRewards.put(wave, m);
    }

    @Override
    public void registerKillReward(EnemyType type, int score, int gold, List<ItemStack> drops) {
        killRewards.put(type, new KillReward(score, gold, drops != null ? drops : Collections.emptyList()));
    }

    @Override
    public void healAllBuildings(String arenaId, double percent) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        for (BuildingImpl b : g.buildings) {
            b.health = Math.min(b.maxHealth, b.health + b.maxHealth * percent);
        }
    }

    @Override
    public void damageCore(String arenaId, double damage, LivingEntity source) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        g.coreHealth = Math.max(0, g.coreHealth - damage);
        fireEvent(new VillageEventImpl(VillageEvent.Type.CORE_DAMAGED, g, null, source));
        if (g.coreHealth <= 0) {
            fireEvent(new VillageEventImpl(VillageEvent.Type.CORE_DESTROYED, g, null, source));
            g.endInternal(false);
        }
    }

    @Override
    public List<LivingEntity> findEnemiesNear(Location center, double radius) {
        List<LivingEntity> out = new ArrayList<>();
        World w = center.getWorld();
        if (w == null) return out;
        double r2 = radius * radius;
        for (Entity e : w.getNearbyEntities(center, radius, radius, radius)) {
            if (e instanceof Monster && e instanceof LivingEntity) {
                if (e.getLocation().distanceSquared(center) <= r2) out.add((LivingEntity) e);
            }
        }
        return out;
    }

    @Override
    public List<Building> findBuildingsNear(Location center, double radius, BuildingType type) {
        List<Building> out = new ArrayList<>();
        double r2 = radius * radius;
        for (GameImpl g : games.values()) {
            for (BuildingImpl b : g.buildings) {
                if (type != null && b.type != type) continue;
                if (b.location.getWorld() == null || center.getWorld() == null) continue;
                if (!b.location.getWorld().equals(center.getWorld())) continue;
                if (b.location.distanceSquared(center) <= r2) out.add(b);
            }
        }
        return out;
    }

    @Override
    public void onEvent(Consumer<VillageEvent> listener) { listeners.add(listener); }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends VillageEvent> void onEvent(Class<T> type, Consumer<T> listener) {
        typedListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    private void fireEvent(VillageEvent e) {
        for (Consumer<VillageEvent> cb : listeners) { try { cb.accept(e); } catch (Exception ignored) {} }
        List<Consumer> list = typedListeners.get(e.getClass());
        if (list != null) {
            for (Consumer cb : list) { try { cb.accept(e); } catch (Exception ignored) {} }
        }
    }

    private int getStatInternal(UUID uuid, String arenaId, String key) {
        Map<String, Map<String, Integer>> a = playerStats.get(uuid);
        if (a == null) return 0;
        Map<String, Integer> s = a.get(arenaId);
        if (s == null) return 0;
        return s.getOrDefault(key, 0);
    }

    private void incrementStat(String arenaId, Player p, String key, int by) {
        incrementStat(arenaId, p.getUniqueId(), key, by);
    }

    private void incrementStat(String arenaId, UUID uuid, String key, int by) {
        Map<String, Map<String, Integer>> a = playerStats.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Map<String, Integer> s = a.computeIfAbsent(arenaId, k -> new ConcurrentHashMap<>());
        s.merge(key, by, (x,y) -> x + y);
    }

    @Override
    public int getWavesSurvived(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_WAVES);
    }

    @Override
    public int getBestWave(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_BEST_WAVE);
    }

    @Override
    public int getBestScore(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_BEST_SCORE);
    }

    @Override
    public int getBuildingsBuilt(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_BUILDINGS);
    }

    @Override
    public int getUnitsSpawned(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_UNITS);
    }

    @Override
    public int getGamesPlayed(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_PLAYED);
    }

    @Override
    public int getGamesWon(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_WON);
    }

    @Override
    public int getTotalKills(OfflinePlayer p, String arenaId) {
        return getStatInternal(p.getUniqueId(), arenaId, STAT_KILLS);
    }

    @Override
    public void resetStats(String arenaId, OfflinePlayer p) {
        Map<String, Map<String, Integer>> a = playerStats.get(p.getUniqueId());
        if (a != null) a.remove(arenaId);
    }

    @Override
    public void setBuildLimitPerPlayer(String arenaId, BuildingType type, int max) {
        buildLimits.computeIfAbsent(arenaId, k -> new ConcurrentHashMap<>()).put(type, max);
    }

    @Override
    public int getBuildLimitPerPlayer(String arenaId, BuildingType type) {
        Map<BuildingType, Integer> m = buildLimits.get(arenaId);
        if (m == null) return 0;
        return m.getOrDefault(type, 0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        leave(e.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Game _g = gameOf(p);
        if (!(_g instanceof GameImpl)) return;
        GameImpl g = (GameImpl) _g;
        if (g.state == GameState.WAVE_ACTIVE || g.state == GameState.BUILD_PHASE) {
            if (!g.arena.allowBuild) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Game _g = gameOf(p);
        if (!(_g instanceof GameImpl)) return;
        GameImpl g = (GameImpl) _g;
        if (g.state == GameState.WAVE_ACTIVE || g.state == GameState.BUILD_PHASE) {
            if (!g.arena.allowBuild) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) return;
        for (GameImpl g : games.values()) {
            for (BuildingImpl b : g.buildings) {
                if (e.getEntity().equals(b.bukkitStand)) {
                    double dmg = e.getFinalDamage();
                    b.health = Math.max(0, b.health - dmg);
                    fireEvent(new VillageEventImpl(VillageEvent.Type.BUILDING_DAMAGED, g, null, b));
                    if (b.health <= 0) {
                        g.buildings.remove(b);
                        if (b.type == BuildingType.CORE) {
                            fireEvent(new VillageEventImpl(VillageEvent.Type.CORE_DESTROYED, g, null, null));
                            g.endInternal(false);
                        } else {
                            fireEvent(new VillageEventImpl(VillageEvent.Type.BUILDING_DESTROYED, g, null, b));
                        }
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity le = e.getEntity();
        for (GameImpl g : games.values()) {
            g.onEnemyDeath(le);
        }
    }

    static class KillReward {
        final int score, gold;
        final List<ItemStack> drops;
        KillReward(int s, int g, List<ItemStack> d) { score = s; gold = g; drops = d; }
    }

    static class VillageEventImpl implements VillageEvent {
        final Type type; final Game game; final Player player; final Object data;
        VillageEventImpl(Type t, Game g, Player p, Object d) { type = t; game = g; player = p; data = d; }
        @Override public Type type() { return type; }
        @Override public Game game() { return game; }
        @Override public Player player() { return player; }
        @Override public Object data() { return data; }
    }

    static class EnemySpawnImpl implements EnemySpawn {
        final EnemyType type; final int count, interval;
        final double healthMul, damageMul, speedMul;
        EnemySpawnImpl(EnemyType t, int c, int i, double h, double d, double s) {
            type = t; count = c; interval = i; healthMul = h; damageMul = d; speedMul = s;
        }
        @Override public EnemyType type() { return type; }
        @Override public int count() { return count; }
        @Override public int intervalTicks() { return interval; }
        @Override public double healthMul() { return healthMul; }
        @Override public double damageMul() { return damageMul; }
        @Override public double speedMul() { return speedMul; }
    }

    static class WaveImpl implements Wave {
        final int number; final long durationTicks; final List<EnemySpawn> spawns;
        long elapsedTicks; int spawned, killed;
        WaveImpl(int n, long d, List<EnemySpawn> s) { number = n; durationTicks = d; spawns = s; }
        @Override public int number() { return number; }
        @Override public int totalEnemies() { int s = 0; for (EnemySpawn e : spawns) s += e.count(); return s; }
        @Override public int spawned() { return spawned; }
        @Override public int killed() { return killed; }
        @Override public int remaining() { return totalEnemies() - spawned + (spawned - killed); }
        @Override public List<EnemySpawn> spawns() { return spawns; }
        @Override public long durationTicks() { return durationTicks; }
        @Override public long elapsedTicks() { return elapsedTicks; }
    }

    static class ArenaImpl implements Arena {
        final String id, name, world;
        final int minPlayers, maxPlayers, mapRadius, maxWaves;
        final int buildPhaseSeconds, waveIntervalSeconds;
        final double coreMaxHealth;
        final boolean allowBuild;
        final Location lobbyLocation, spectatorLocation, coreSpawn;
        final List<Location> playerSpawns, enemySpawnPoints;
        final Map<Integer, List<EnemySpawn>> waveSpawns = new ConcurrentHashMap<>();
        ArenaImpl(String id, String name, String world, int minP, int maxP, Location lobby, Location spec,
                  Location core, List<Location> ps, List<Location> esp, int mapR, int maxW,
                  int buildSec, int intervalSec, double coreHp, boolean ab) {
            this.id = id; this.name = name; this.world = world;
            this.minPlayers = minP; this.maxPlayers = maxP; this.mapRadius = mapR; this.maxWaves = maxW;
            this.buildPhaseSeconds = buildSec; this.waveIntervalSeconds = intervalSec;
            this.coreMaxHealth = coreHp; this.allowBuild = ab;
            this.lobbyLocation = lobby; this.spectatorLocation = spec; this.coreSpawn = core;
            this.playerSpawns = ps != null ? ps : Collections.emptyList();
            this.enemySpawnPoints = esp != null ? esp : Collections.emptyList();
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String world() { return world; }
        @Override public int minPlayers() { return minPlayers; }
        @Override public int maxPlayers() { return maxPlayers; }
        @Override public Location lobbyLocation() { return lobbyLocation; }
        @Override public Location spectatorLocation() { return spectatorLocation; }
        @Override public Location coreSpawn() { return coreSpawn; }
        @Override public List<Location> playerSpawns() { return playerSpawns; }
        @Override public List<Location> enemySpawnPoints() { return enemySpawnPoints; }
        @Override public int mapRadius() { return mapRadius; }
        @Override public int maxWaves() { return maxWaves; }
        @Override public int buildPhaseSeconds() { return buildPhaseSeconds; }
        @Override public int waveIntervalSeconds() { return waveIntervalSeconds; }
        @Override public double coreMaxHealth() { return coreMaxHealth; }
        @Override public boolean allowBuild() { return allowBuild; }
        @Override public boolean isInUse() { return false; }
    }

    static class DefenderImpl implements Defender {
        final UUID uuid; final String name;
        final Map<String, Integer> resources = new ConcurrentHashMap<>();
        final List<Building> buildings = new CopyOnWriteArrayList<>();
        final List<Unit> units = new CopyOnWriteArrayList<>();
        int kills, deaths, score;
        DefenderImpl(Player p) { this.uuid = p.getUniqueId(); this.name = p.getName();
            resources.put("gold", 200); resources.put("wood", 100); resources.put("stone", 80); resources.put("iron", 20);
        }
        @Override public UUID uuid() { return uuid; }
        @Override public String name() { return name; }
        @Override public int gold() { return resources.getOrDefault("gold", 0); }
        @Override public int wood() { return resources.getOrDefault("wood", 0); }
        @Override public int stone() { return resources.getOrDefault("stone", 0); }
        @Override public int iron() { return resources.getOrDefault("iron", 0); }
        @Override public int kills() { return kills; }
        @Override public int deaths() { return deaths; }
        @Override public int score() { return score; }
        @Override public List<Building> buildings() { return Collections.unmodifiableList(buildings); }
        @Override public List<Unit> units() { return Collections.unmodifiableList(units); }
        @Override public void addResource(String type, int amount) {
            resources.merge(type, amount, (a,b) -> a + b);
        }
        @Override public boolean spendGold(int amount) {
            int cur = resources.getOrDefault("gold", 0);
            if (cur < amount) return false;
            resources.put("gold", cur - amount);
            return true;
        }
        @Override public boolean spendResources(Map<String, Integer> costs) {
            if (costs == null) return true;
            for (Map.Entry<String, Integer> e : costs.entrySet()) {
                if (resources.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
            }
            for (Map.Entry<String, Integer> e : costs.entrySet()) {
                resources.merge(e.getKey(), -e.getValue(), (a,b) -> a + b);
            }
            return true;
        }
    }

    class BuildingImpl implements Building {
        final GameImpl game;
        final BuildingType type;
        final Location location;
        final DefenderImpl owner;
        int level, maxLevel;
        double health, maxHealth;
        int attackRange, attackDamage;
        double attackSpeedTicks;
        int resourcePerTick;
        boolean enabled = true;
        LivingEntity bukkitStand;
        long attackCooldown;
        BuildingImpl(GameImpl g, BuildingType t, Location l, int lv, DefenderImpl o) {
            game = g; type = t; location = l; level = lv; owner = o; maxLevel = 5;
            applyStats();
            health = maxHealth;
        }
        void applyStats() {
            double levelMul = 1.0 + (level - 1) * 0.25;
            switch (type) {
                case CORE:
                    maxHealth = (game.arena.coreMaxHealth > 0 ? game.arena.coreMaxHealth : 200.0) * levelMul;
                    attackDamage = 0; attackRange = 0; attackSpeedTicks = 0; resourcePerTick = 0;
                    break;
                case TOWER_ARROW:
                    maxHealth = 100 * levelMul; attackDamage = (int)(5 * levelMul);
                    attackRange = 15; attackSpeedTicks = 20; resourcePerTick = 0; break;
                case TOWER_MAGIC:
                    maxHealth = 80 * levelMul; attackDamage = (int)(8 * levelMul);
                    attackRange = 20; attackSpeedTicks = 40; resourcePerTick = 0; break;
                case TOWER_CANNON:
                    maxHealth = 150 * levelMul; attackDamage = (int)(15 * levelMul);
                    attackRange = 12; attackSpeedTicks = 60; resourcePerTick = 0; break;
                case WALL:
                    maxHealth = 200 * levelMul; attackDamage = 0; attackRange = 0; break;
                case GATE:
                    maxHealth = 120 * levelMul; attackDamage = 0; attackRange = 0; break;
                case GOLD_MINE:
                    maxHealth = 80 * levelMul; resourcePerTick = (int)(1 + 0.5 * (level-1)); break;
                case LUMBER_CAMP:
                    maxHealth = 70 * levelMul; resourcePerTick = level; break;
                case BARRACKS:
                    maxHealth = 150 * levelMul; resourcePerTick = 0; break;
                case BLACKSMITH:
                    maxHealth = 120 * levelMul; resourcePerTick = (level-1); break;
                case WELL:
                    maxHealth = 80 * levelMul; resourcePerTick = 0; break;
                case FARM:
                    maxHealth = 60 * levelMul; resourcePerTick = level; break;
                case VILLAGER_HOUSE:
                    maxHealth = 90 * levelMul; resourcePerTick = 0; break;
            }
        }
        void tick() {
            if (!enabled || !isAlive()) return;
            if (resourcePerTick > 0 && owner != null && game.tickCounter % 20 == 0) {
                String resource = "gold";
                if (type == BuildingType.LUMBER_CAMP) resource = "wood";
                else if (type == BuildingType.FARM) resource = "gold";
                else if (type == BuildingType.BLACKSMITH) resource = "iron";
                owner.addResource(resource, resourcePerTick);
            }
            if (attackDamage > 0 && attackRange > 0) {
                if (attackCooldown > 0) attackCooldown--;
                else {
                    List<LivingEntity> near = findEnemiesNear(location, attackRange);
                    if (!near.isEmpty()) {
                        LivingEntity target = near.get(0);
                        target.damage(attackDamage);
                        attackCooldown = (int) attackSpeedTicks;
                    }
                }
            }
        }
        @Override public String id() { return type.name() + "_" + Integer.toHexString(System.identityHashCode(this)); }
        @Override public BuildingType type() { return type; }
        @Override public Location location() { return location; }
        @Override public int level() { return level; }
        @Override public int maxLevel() { return maxLevel; }
        @Override public double health() { return health; }
        @Override public double maxHealth() { return maxHealth; }
        @Override public boolean isAlive() { return health > 0; }
        @Override public int attackRange() { return attackRange; }
        @Override public int attackDamage() { return attackDamage; }
        @Override public double attackSpeedTicks() { return attackSpeedTicks; }
        @Override public int resourcePerTick() { return resourcePerTick; }
        @Override public void upgrade() { if (level < maxLevel) { level++; applyStats(); } }
        @Override public void repair(double amount) { health = Math.min(maxHealth, health + amount); }
        @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }
        @Override public boolean isEnabled() { return enabled; }
    }

    class UnitImpl implements Unit {
        final UUID uuid = UUID.randomUUID();
        final GameImpl game;
        final UnitType type;
        Location location, home;
        Building assignedBuilding;
        double health, maxHealth;
        int attackDamage, armor;
        double speed;
        LivingEntity bukkitEntity;
        UnitImpl(GameImpl g, UnitType t, Location l, Building b) {
            game = g; type = t; location = l; home = l; assignedBuilding = b;
            Map<String, Object> stats = unitStats.get(t);
            if (stats != null) {
                maxHealth = ((Number) stats.getOrDefault("health", 20.0)).doubleValue();
                attackDamage = ((Number) stats.getOrDefault("damage", 2)).intValue();
                armor = ((Number) stats.getOrDefault("armor", 0)).intValue();
                speed = ((Number) stats.getOrDefault("speed", 1.0)).doubleValue();
            } else {
                maxHealth = 20; attackDamage = 2; armor = 0; speed = 1.0;
            }
            health = maxHealth;
            spawnBukkit();
        }
        private void spawnBukkit() {
            World w = location.getWorld();
            if (w == null) return;
            EntityType et = EntityType.VILLAGER;
            switch (type) {
                case GUARD: case KNIGHT: et = EntityType.IRON_GOLEM; break;
                case ARCHER: case MAGE: et = EntityType.SKELETON; break;
                case HEALER: case WORKER: case VILLAGER: default: et = EntityType.VILLAGER; break;
            }
            Entity e = w.spawnEntity(location, et);
            if (e instanceof LivingEntity) {
                bukkitEntity = (LivingEntity) e;
                try { bukkitEntity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth); } catch (Exception ignored) {}
                bukkitEntity.setHealth(health);
            }
        }
        @Override public UUID uuid() { return uuid; }
        @Override public UnitType type() { return type; }
        @Override public String name() { return type.name(); }
        @Override public Location location() { return bukkitEntity != null ? bukkitEntity.getLocation() : location; }
        @Override public Location home() { return home; }
        @Override public Building assignedBuilding() { return assignedBuilding; }
        @Override public double health() { return bukkitEntity != null ? bukkitEntity.getHealth() : health; }
        @Override public double maxHealth() { return maxHealth; }
        @Override public int attackDamage() { return attackDamage; }
        @Override public int armor() { return armor; }
        @Override public double speed() { return speed; }
        @Override public boolean isAlive() { return bukkitEntity != null && !bukkitEntity.isDead(); }
        @Override public void attack(LivingEntity target) { if (bukkitEntity != null && target != null) bukkitEntity.attack(target); }
        @Override public void moveTo(Location loc) { if (bukkitEntity != null) { try { bukkitEntity.teleport(loc); } catch (Exception ignored) {} } }
        @Override public void heal(double amount) { if (bukkitEntity != null) bukkitEntity.setHealth(Math.min(maxHealth, bukkitEntity.getHealth() + amount)); }
    }

    class GameImpl implements Game {
        final ArenaImpl arena;
        GameState state = GameState.WAITING;
        WaveImpl currentWave;
        List<Wave> allWaves = new CopyOnWriteArrayList<>();
        BuildingImpl coreBuilding;
        double coreHealth;
        List<BuildingImpl> buildings = new CopyOnWriteArrayList<>();
        List<UnitImpl> units = new CopyOnWriteArrayList<>();
        List<LivingEntity> enemyEntities = new CopyOnWriteArrayList<>();
        Map<UUID, DefenderImpl> defenders = new ConcurrentHashMap<>();
        Map<UUID, GameImpl> playerGame = new ConcurrentHashMap<>();
        List<Consumer<GameResult>> endCbs = new CopyOnWriteArrayList<>();
        int timer = 0;
        int countdownTarget = 0;
        int waveNumber = 0;
        int waveSpawnCursor = 0;
        int waveSpawnCooldown = 0;
        int waveKilled = 0;
        int score = 0;
        long tickCounter = 0;
        GameImpl(ArenaImpl a) {
            arena = a; coreHealth = a.coreMaxHealth;
        }
        @Override public String id() { return arena.id; }
        @Override public Arena arena() { return arena; }
        @Override public GameState state() { return state; }
        @Override public Wave currentWave() { return currentWave; }
        @Override public List<Wave> allWaves() { return Collections.unmodifiableList(allWaves); }
        @Override public Building coreBuilding() { return coreBuilding; }
        @Override public double coreHealth() { return coreHealth; }
        @Override public double coreMaxHealth() { return arena.coreMaxHealth; }
        @Override public List<Building> allBuildings() { return Collections.unmodifiableList(new ArrayList<>(buildings)); }
        @Override public List<Unit> allUnits() { return Collections.unmodifiableList(new ArrayList<>(units)); }
        @Override public List<LivingEntity> aliveEnemies() {
            List<LivingEntity> out = new ArrayList<>();
            for (LivingEntity le : enemyEntities) if (le != null && !le.isDead()) out.add(le);
            return out;
        }
        @Override public List<Defender> defenders() { return Collections.unmodifiableList(new ArrayList<>(defenders.values())); }
        @Override public Defender defender(UUID uuid) { return defenders.get(uuid); }
        @Override public boolean isInGame(Player p) { return playerGame.containsKey(p.getUniqueId()); }
        @Override public int timerSeconds() { return timer / 20; }
        @Override public int score() { return score; }
        @Override public void onEnd(Consumer<GameResult> callback) { if (callback != null) endCbs.add(callback); }

        void tick() {
            tickCounter++;
            switch (state) {
                case COUNTDOWN:
                    timer--;
                    if (timer <= 0) startBuildPhase();
                    break;
                case BUILD_PHASE:
                    timer--;
                    for (BuildingImpl b : buildings) b.tick();
                    if (timer <= 0) startNextWave();
                    break;
                case WAVE_ACTIVE:
                    for (BuildingImpl b : buildings) b.tick();
                    spawnEnemiesFromWave();
                    long alive = enemyEntities.stream().filter(e -> e != null && !e.isDead()).count();
                    if (waveSpawnCursor >= totalSpawnsCurrent() && alive == 0) {
                        endWave();
                    }
                    break;
                case WAVE_INTERVAL:
                    timer--;
                    for (BuildingImpl b : buildings) b.tick();
                    if (timer <= 0) startNextWave();
                    break;
                default: break;
            }
        }

        int totalSpawnsCurrent() {
            int s = 0;
            if (currentWave != null) for (EnemySpawn es : currentWave.spawns) s += es.count();
            return s;
        }

        void startBuildPhase() {
            state = GameState.BUILD_PHASE;
            timer = arena.buildPhaseSeconds * 20;
            for (DefenderImpl d : defenders.values()) {
                Player p = Bukkit.getPlayer(d.uuid);
                if (p != null && !arena.playerSpawns.isEmpty()) {
                    int idx = new ArrayList<>(defenders.keySet()).indexOf(d.uuid) % arena.playerSpawns.size();
                    p.teleport(arena.playerSpawns.get(idx));
                }
            }
            fireEvent(new VillageEventImpl(VillageEvent.Type.BUILD_START, this, null, null));
        }

        void startNextWave() {
            waveNumber++;
            if (waveNumber > arena.maxWaves) {
                endInternal(true);
                return;
            }
            List<EnemySpawn> spawns = arena.waveSpawns != null ?
                    arena.waveSpawns.getOrDefault(waveNumber, defaultWaveSpawns(waveNumber)) :
                    defaultWaveSpawns(waveNumber);
            Map<Integer, List<EnemySpawn>> reg = waveSpawnsRegistry.get(arena.id);
            if (reg != null && reg.containsKey(waveNumber)) {
                List<EnemySpawn> copy = new ArrayList<>(spawns);
                copy.addAll(reg.get(waveNumber));
                spawns = copy;
            }
            currentWave = new WaveImpl(waveNumber, 1200L, spawns);
            allWaves.add(currentWave);
            state = GameState.WAVE_ACTIVE;
            waveSpawnCursor = 0;
            waveSpawnCooldown = 0;
            waveKilled = 0;
            fireEvent(new VillageEventImpl(VillageEvent.Type.WAVE_START, this, null, currentWave));
        }

        List<EnemySpawn> defaultWaveSpawns(int wave) {
            List<EnemySpawn> out = new ArrayList<>();
            int base = 3 + wave * 2;
            double hm = 1.0 + (wave - 1) * 0.1;
            double dm = 1.0 + (wave - 1) * 0.05;
            out.add(new EnemySpawnImpl(EnemyType.RAIDER, base, 40, hm, dm, 1.0));
            if (wave >= 2) out.add(new EnemySpawnImpl(EnemyType.ARCHER, base / 2, 60, hm, dm, 1.0));
            if (wave >= 4) out.add(new EnemySpawnImpl(EnemyType.GRUNT, base / 3, 50, hm * 1.2, dm * 1.1, 1.0));
            if (wave >= 6) out.add(new EnemySpawnImpl(EnemyType.BRUTE, Math.max(1, base / 5), 80, hm * 1.5, dm * 1.3, 0.9));
            if (wave >= 8) out.add(new EnemySpawnImpl(EnemyType.SHAMAN, Math.max(1, base / 6), 100, hm * 1.1, dm * 1.2, 1.0));
            if (wave % 10 == 0) {
                out.add(new EnemySpawnImpl(wave % 20 == 0 ? EnemyType.BOSS_BEHEMOTH : EnemyType.BOSS_WARCHIEF, 1, 0, hm * 2.0, dm * 1.5, 0.8));
            }
            return out;
        }

        void spawnEnemiesFromWave() {
            if (currentWave == null) return;
            if (waveSpawnCooldown > 0) { waveSpawnCooldown--; return; }
            int remaining = currentWave.totalEnemies() - waveSpawnCursor;
            if (remaining <= 0) return;
            int index = 0;
            int cumulative = 0;
            EnemySpawn targetSpawn = null;
            for (EnemySpawn es : currentWave.spawns) {
                cumulative += es.count();
                if (waveSpawnCursor < cumulative) { targetSpawn = es; break; }
                index++;
            }
            if (targetSpawn == null) return;
            spawnEnemyAtRandomPoint(targetSpawn);
            waveSpawnCursor++;
            currentWave.spawned++;
            waveSpawnCooldown = targetSpawn.intervalTicks();
        }

        void spawnEnemyAtRandomPoint(EnemySpawn es) {
            if (arena.enemySpawnPoints.isEmpty()) return;
            Location loc = arena.enemySpawnPoints.get((int)(Math.random() * arena.enemySpawnPoints.size()));
            if (loc == null || loc.getWorld() == null) return;
            EntityType et;
            switch (es.type()) {
                case RAIDER: et = EntityType.ZOMBIE; break;
                case ARCHER: et = EntityType.SKELETON; break;
                case GRUNT: et = EntityType.CREEPER; break;
                case BRUTE: et = EntityType.RAVAGER; break;
                case SHAMAN: et = EntityType.WITCH; break;
                case BOSS_WARCHIEF: case BOSS_BEHEMOTH: et = EntityType.WITHER; break;
                default: et = EntityType.ZOMBIE; break;
            }
            Entity e = loc.getWorld().spawnEntity(loc, et);
            if (e instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) e;
                Map<String, Object> base = getEnemyStats(es.type());
                double hp = ((Number) base.getOrDefault("health", 20.0)).doubleValue() * es.healthMul();
                double dmg = ((Number) base.getOrDefault("damage", 2.0)).doubleValue() * es.damageMul();
                try { le.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp); } catch (Exception ignored) {}
                le.setHealth(hp);
                enemyEntities.add(le);
                fireEvent(new VillageEventImpl(VillageEvent.Type.ENEMY_SPAWN, this, null, le));
            }
        }

        void endWave() {
            if (currentWave != null) {
                fireEvent(new VillageEventImpl(VillageEvent.Type.WAVE_END, this, null, currentWave));
                int w = currentWave.number();
                for (DefenderImpl d : defenders.values()) {
                    incrementStat(arena.id, d.uuid, STAT_WAVES, 1);
                    int bw = getStatInternal(d.uuid, arena.id, STAT_BEST_WAVE);
                    if (w > bw) setStatInternal(d.uuid, arena.id, STAT_BEST_WAVE, w);
                }
                Map<String, Integer> reward = waveRewards.get(w);
                if (reward == null) {
                    reward = new HashMap<>();
                    reward.put("gold", 30 + w * 10);
                    reward.put("wood", 10 + w * 2);
                    reward.put("__score__", 100 + w * 50);
                }
                for (DefenderImpl d : defenders.values()) {
                    for (Map.Entry<String, Integer> e : reward.entrySet()) {
                        if ("__score__".equals(e.getKey())) {
                            d.score += e.getValue();
                            score += e.getValue();
                        } else {
                            d.addResource(e.getKey(), e.getValue());
                        }
                    }
                }
                fireEvent(new VillageEventImpl(VillageEvent.Type.REWARD_GRANT, this, null, reward));
            }
            for (LivingEntity le : enemyEntities) if (le != null && !le.isDead()) le.remove();
            enemyEntities.clear();
            if (waveNumber >= arena.maxWaves) {
                endInternal(true);
            } else {
                state = GameState.WAVE_INTERVAL;
                timer = arena.waveIntervalSeconds * 20;
            }
        }

        void onEnemyDeath(LivingEntity le) {
            if (!enemyEntities.contains(le)) return;
            enemyEntities.remove(le);
            waveKilled++;
            if (currentWave != null) currentWave.killed++;
            KillReward kr = null;
            EntityType et = le.getType();
            for (EnemyType ek : EnemyType.values()) {
                if (matchEnemy(ek, et)) { kr = killRewards.get(ek); break; }
            }
            Player killer = le.getKiller();
            DefenderImpl d = null;
            if (killer != null) d = defenders.get(killer.getUniqueId());
            if (d != null) {
                d.kills++;
                incrementStat(arena.id, d.uuid, STAT_KILLS, 1);
                int s = kr != null ? kr.score : 10;
                int g = kr != null ? kr.gold : 5;
                d.score += s;
                d.addResource("gold", g);
                score += s;
                int bs = getStatInternal(d.uuid, arena.id, STAT_BEST_SCORE);
                if (d.score > bs) setStatInternal(d.uuid, arena.id, STAT_BEST_SCORE, d.score);
                fireEvent(new VillageEventImpl(VillageEvent.Type.SCORE_CHANGE, this, killer, d.score));
                fireEvent(new VillageEventImpl(VillageEvent.Type.ENEMY_KILL, this, killer, le));
            }
        }

        boolean matchEnemy(EnemyType e, EntityType t) {
            switch (e) {
                case RAIDER: return t == EntityType.ZOMBIE;
                case ARCHER: return t == EntityType.SKELETON;
                case GRUNT: return t == EntityType.CREEPER;
                case BRUTE: return t == EntityType.RAVAGER;
                case SHAMAN: return t == EntityType.WITCH;
                case BOSS_WARCHIEF: case BOSS_BEHEMOTH: return t == EntityType.WITHER;
            }
            return false;
        }

        void setStatInternal(UUID u, String a, String k, int v) {
            playerStats.computeIfAbsent(u, k2 -> new ConcurrentHashMap<>())
                    .computeIfAbsent(a, k2 -> new ConcurrentHashMap<>()).put(k, v);
        }

        void endInternal(boolean victory) {
            if (state == GameState.ENDING) return;
            state = GameState.ENDING;
            if (victory) {
                for (DefenderImpl d : defenders.values()) {
                    incrementStat(arena.id, d.uuid, STAT_WON, 1);
                }
            }
            GameResultImpl r = new GameResultImpl(victory, waveNumber, score, defenders());
            for (Consumer<GameResult> cb : endCbs) { try { cb.accept(r); } catch (Exception ignored) {} }
            fireEvent(new VillageEventImpl(VillageEvent.Type.GAME_END, this, null, r));
            fireEvent(new VillageEventImpl(victory ? VillageEvent.Type.VICTORY : VillageEvent.Type.DEFEAT, this, null, r));
            for (LivingEntity le : enemyEntities) if (le != null && !le.isDead()) le.remove();
            for (UnitImpl u : units) if (u.bukkitEntity != null && !u.bukkitEntity.isDead()) u.bukkitEntity.remove();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (DefenderImpl d : defenders.values()) {
                    Player p = Bukkit.getPlayer(d.uuid);
                    if (p != null && arena.spectatorLocation != null) p.teleport(arena.spectatorLocation);
                }
                games.remove(arena.id);
            }, 200L);
        }
    }

    static class GameResultImpl implements GameResult {
        final boolean victory;
        final int waveReached, totalScore;
        final List<Defender> participants;
        final Map<UUID, Integer> playerScores;
        GameResultImpl(boolean v, int w, int s, List<Defender> p) {
            victory = v; waveReached = w; totalScore = s; participants = p;
            playerScores = new ConcurrentHashMap<>();
            for (Defender d : p) playerScores.put(d.uuid(), d.score());
        }
        @Override public boolean victory() { return victory; }
        @Override public int waveReached() { return waveReached; }
        @Override public int totalScore() { return totalScore; }
        @Override public List<Defender> participants() { return participants; }
        @Override public Map<UUID, Integer> playerScores() { return playerScores; }
    }
}
