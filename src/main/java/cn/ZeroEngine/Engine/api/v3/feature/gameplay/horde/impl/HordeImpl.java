package cn.ZeroEngine.Engine.api.v3.feature.gameplay.horde.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.horde.Horde;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class HordeImpl implements Horde, Listener {

    private final JavaPlugin plugin;
    private final Map<String, ArenaImpl> arenas = new ConcurrentHashMap<>();
    private final Map<String, GameImpl> games = new ConcurrentHashMap<>();
    private final List<Consumer<HordeEvent>> listeners = new CopyOnWriteArrayList<>();
    private final Map<Difficulty, Map<String, Double>> diffMults = new ConcurrentHashMap<>();
    private final Map<Integer, WaveReward> waveRewards = new ConcurrentHashMap<>();
    private final Map<EntityType, KillReward> killRewards = new ConcurrentHashMap<>();
    private final Map<String, EliteDef> elites = new ConcurrentHashMap<>();
    private final Map<String, BossDef> bosses = new ConcurrentHashMap<>();
    private final Map<String, Map<String, PlayerStats>> stats = new ConcurrentHashMap<>();
    private BukkitTask mainTask;

    public HordeImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        for (Difficulty d : Difficulty.values()) {
            Map<String, Double> m = new ConcurrentHashMap<>();
            m.put("health", 1.0); m.put("damage", 1.0); m.put("speed", 1.0);
            m.put("count", 1.0); m.put("reward", 1.0);
            diffMults.put(d, m);
        }
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (mainTask != null) mainTask.cancel();
        for (GameImpl g : games.values()) g.endInternal();
        arenas.clear(); games.clear(); listeners.clear();
        elites.clear(); bosses.clear();
    }

    private void tick() {
        for (GameImpl g : games.values()) g.tick();
    }

    @Override
    public void registerArena(String id, String name, String world, int minPlayers, int maxPlayers,
                              List<Location> spawnPoints, Location lobbyLocation, Location spectatorLocation,
                              List<Location> mobSpawnPoints, int borderRadius, Location borderCenter,
                              Difficulty defaultDifficulty, int maxWaves, int prepareSeconds,
                              int waveIntervalSeconds, boolean allowBuild) {
        arenas.put(id, new ArenaImpl(id, name, world, minPlayers, maxPlayers, spawnPoints,
                lobbyLocation, spectatorLocation, mobSpawnPoints, borderRadius, borderCenter,
                defaultDifficulty, maxWaves, prepareSeconds, waveIntervalSeconds, allowBuild));
    }

    @Override
    public void unregisterArena(String id) {
        arenas.remove(id);
        GameImpl g = games.remove(id);
        if (g != null) g.endInternal();
    }

    @Override
    public Arena getArena(String id) { return arenas.get(id); }

    @Override
    public Collection<Arena> allArenas() { return Collections.unmodifiableCollection(arenas.values()); }

    @Override
    public Game createGame(String arenaId, Difficulty difficulty) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return null;
        Difficulty d = difficulty != null ? difficulty : a.defaultDifficulty;
        GameImpl g = new GameImpl(arenaId, a, d);
        games.put(arenaId, g);
        return g;
    }

    @Override
    public Game getGame(String arenaId) { return games.get(arenaId); }

    @Override
    public Game gameOf(Player p) {
        UUID u = p.getUniqueId();
        for (GameImpl g : games.values()) {
            if (g.survivors.containsKey(u)) return g;
        }
        return null;
    }

    @Override
    public boolean join(Player p, String arenaId) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return false;
        GameImpl g = games.computeIfAbsent(arenaId, id -> {
            GameImpl ng = new GameImpl(id, a, a.defaultDifficulty);
            return ng;
        });
        if (g.state != GameState.WAITING && g.state != GameState.COUNTDOWN) return false;
        if (g.survivors.size() >= a.maxPlayers) return false;
        SurvivorImpl s = new SurvivorImpl(p.getUniqueId(), p.getName());
        g.survivors.put(p.getUniqueId(), s);
        if (a.lobbyLocation != null) p.teleport(a.lobbyLocation);
        fire(new HordeEventImpl(HordeEvent.Type.PLAYER_JOIN, g, p, null));
        return true;
    }

    @Override
    public boolean leave(Player p) {
        Game hg = gameOf(p);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return false;
        SurvivorImpl s = g.survivors.remove(p.getUniqueId());
        if (s != null) saveStats(g.arena.id, s);
        ArenaImpl a = g.arena;
        if (a.spectatorLocation != null) p.teleport(a.spectatorLocation);
        fire(new HordeEventImpl(HordeEvent.Type.PLAYER_LEAVE, g, p, null));
        return true;
    }

    @Override
    public boolean startCountdown(String arenaId, int seconds) {
        GameImpl g = games.get(arenaId);
        if (g == null || g.state != GameState.WAITING) return false;
        g.state = GameState.COUNTDOWN;
        g.countdownTicks = seconds * 20L;
        fire(new HordeEventImpl(HordeEvent.Type.COUNTDOWN_START, g, null, seconds));
        return true;
    }

    @Override
    public void forceStart(String arenaId) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        g.state = GameState.PREPARING;
        g.countdownTicks = (long) g.arena.prepareSeconds * 20;
        g.timerSeconds = g.arena.prepareSeconds;
        fire(new HordeEventImpl(HordeEvent.Type.PREPARE_START, g, null, null));
    }

    @Override
    public void forceEnd(String arenaId) {
        GameImpl g = games.remove(arenaId);
        if (g != null) g.endInternal();
    }

    @Override
    public void addWaveMobRule(String arenaId, int waveNumber, MobRule rule) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return;
        if (rule instanceof MobRuleImpl) {
            a.waveRules.computeIfAbsent(waveNumber, k -> new CopyOnWriteArrayList<>()).add((MobRuleImpl) rule);
        }
    }

    @Override
    public void removeWaveMobRule(String arenaId, int waveNumber, EntityType type) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return;
        List<MobRuleImpl> rules = a.waveRules.get(waveNumber);
        if (rules != null) rules.removeIf(r -> r.type == type);
    }

    @Override
    public void setDifficultyMultiplier(Difficulty d, String key, double value) {
        diffMults.computeIfAbsent(d, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    public double getDifficultyMultiplier(Difficulty d, String key) {
        return diffMults.getOrDefault(d, Collections.emptyMap()).getOrDefault(key, 1.0);
    }

    @Override
    public void registerWaveReward(int wave, List<ItemStack> items, double moneyPerPlayer, int scorePerPlayer) {
        waveRewards.put(wave, new WaveReward(new ArrayList<>(items), moneyPerPlayer, scorePerPlayer));
    }

    @Override
    public void registerKillReward(EntityType type, int score, double money, List<ItemStack> drops) {
        killRewards.put(type, new KillReward(score, money, drops != null ? new ArrayList<>(drops) : null));
    }

    @Override
    public void revivePlayer(Player dead, Player reviver) {
        Game hg = gameOf(dead);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return;
        SurvivorImpl s = g.survivors.get(dead.getUniqueId());
        if (s == null || !s.downed) return;
        s.downed = false;
        s.downTimer = 0;
        s.alive = true;
        s.revived++;
        if (reviver != null) {
            SurvivorImpl rs = g.survivors.get(reviver.getUniqueId());
            if (rs != null) rs.score += 50;
        }
        dead.setHealth(Objects.requireNonNull(dead.getAttribute(Attribute.MAX_HEALTH)).getValue() * 0.5);
        fire(new HordeEventImpl(HordeEvent.Type.PLAYER_REVIVE, g, dead, reviver));
    }

    @Override
    public void downPlayer(Player p, int timerSeconds) {
        Game hg = gameOf(p);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return;
        SurvivorImpl s = g.survivors.get(p.getUniqueId());
        if (s == null) return;
        s.downed = true;
        s.downTimer = timerSeconds;
        fire(new HordeEventImpl(HordeEvent.Type.PLAYER_DOWN, g, p, timerSeconds));
    }

    @Override
    public boolean isPlayerDowned(Player p) {
        Game hg = gameOf(p);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return false;
        SurvivorImpl s = g.survivors.get(p.getUniqueId());
        return s != null && s.downed;
    }

    @Override
    public int spawnWaveMobs(Game g, Wave w) {
        if (!(g instanceof GameImpl) || !(w instanceof WaveImpl)) return 0;
        GameImpl gi = (GameImpl) g;
        WaveImpl wi = (WaveImpl) w;
        int count = 0;
        double hMul = getDifficultyMultiplier(gi.difficulty, "health");
        double dMul = getDifficultyMultiplier(gi.difficulty, "damage");
        double sMul = getDifficultyMultiplier(gi.difficulty, "speed");
        World world = Bukkit.getWorld(gi.arena.world);
        if (world == null) return 0;
        for (MobRuleImpl rule : wi.rules) {
            int base = rule.minCount + (int) (Math.random() * (rule.maxCount - rule.minCount + 1));
            for (int i = 0; i < base; i++) {
                Location loc = pickSpawn(gi.arena);
                if (loc == null) continue;
                LivingEntity e = (LivingEntity) world.spawnEntity(loc, rule.type);
                applyMobStats(e, rule, hMul, dMul, sMul);
                gi.aliveMobs.add(e.getUniqueId());
                wi.spawned++;
                count++;
                fire(new HordeEventImpl(HordeEvent.Type.MOB_SPAWN, gi, null, e));
                if (rule.spawnType == SpawnType.ELITE) {
                    fire(new HordeEventImpl(HordeEvent.Type.ELITE_SPAWN, gi, null, e));
                } else if (rule.spawnType == SpawnType.BOSS) {
                    fire(new HordeEventImpl(HordeEvent.Type.BOSS_SPAWN, gi, null, e));
                }
            }
        }
        return count;
    }

    private Location pickSpawn(ArenaImpl a) {
        if (a.mobSpawnPoints.isEmpty()) return null;
        return a.mobSpawnPoints.get((int) (Math.random() * a.mobSpawnPoints.size())).clone();
    }

    private void applyMobStats(LivingEntity e, MobRuleImpl rule, double hMul, double dMul, double sMul) {
        try {
            Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH))
                    .setBaseValue(Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue() * rule.healthMul * hMul);
            e.setHealth(Objects.requireNonNull(e.getAttribute(Attribute.MAX_HEALTH)).getValue());
        } catch (Exception ignored) {}
        try {
            Objects.requireNonNull(e.getAttribute(Attribute.ATTACK_DAMAGE))
                    .setBaseValue(Objects.requireNonNull(e.getAttribute(Attribute.ATTACK_DAMAGE)).getDefaultValue() * rule.damageMul * dMul);
        } catch (Exception ignored) {}
        try {
            Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED))
                    .setBaseValue(Objects.requireNonNull(e.getAttribute(Attribute.MOVEMENT_SPEED)).getDefaultValue() * rule.speedMul * sMul);
        } catch (Exception ignored) {}
        if (rule.effects != null) {
            for (String effStr : rule.effects) {
                try {
                    String[] parts = effStr.split(":");
                    PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                    if (type == null) continue;
                    int dur = parts.length > 1 ? Integer.parseInt(parts[1]) * 20 : Integer.MAX_VALUE;
                    int amp = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : 0;
                    e.addPotionEffect(new PotionEffect(type, dur, amp, true, false));
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void clearArenaMobs(String arenaId) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        World w = Bukkit.getWorld(g.arena.world);
        if (w == null) return;
        for (UUID uid : new ArrayList<>(g.aliveMobs)) {
            Entity e = Bukkit.getEntity(uid);
            if (e != null) e.remove();
        }
        g.aliveMobs.clear();
    }

    @Override
    public void registerElite(EntityType base, String eliteId, String displayName,
                              double healthMul, double damageMul, double speedMul,
                              List<String> effects, Map<String, Object> skills) {
        elites.put(eliteId, new EliteDef(base, displayName, healthMul, damageMul, speedMul,
                effects != null ? new ArrayList<>(effects) : null, skills));
    }

    @Override
    public void registerBoss(EntityType base, String bossId, String displayName,
                             double healthMul, double damageMul, double speedMul,
                             List<String> effects, Map<String, Object> skills,
                             List<ItemStack> drops, int scoreReward) {
        bosses.put(bossId, new BossDef(base, displayName, healthMul, damageMul, speedMul,
                effects != null ? new ArrayList<>(effects) : null, skills,
                drops != null ? new ArrayList<>(drops) : null, scoreReward));
    }

    @Override
    public void onEvent(Consumer<HordeEvent> listener) { listeners.add(listener); }

    @Override
    public <T extends HordeEvent> void onEvent(Class<T> type, Consumer<T> listener) {
        listeners.add(e -> {
            if (type.isInstance(e)) listener.accept(type.cast(e));
        });
    }

    private void fire(HordeEvent e) {
        for (Consumer<HordeEvent> l : listeners) {
            try { l.accept(e); } catch (Exception ignored) {}
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        LivingEntity le = e.getEntity();
        if (le instanceof Player) return;
        UUID uid = le.getUniqueId();
        for (GameImpl g : games.values()) {
            if (g.aliveMobs.remove(uid)) {
                Player killer = le.getKiller();
                if (killer != null) {
                    SurvivorImpl s = g.survivors.get(killer.getUniqueId());
                    if (s != null) {
                        s.kills++;
                        s.damageDealt += (int) Math.max(0, le.getHealth());
                        KillReward kr = killRewards.get(le.getType());
                        if (kr != null) {
                            s.score += kr.score;
                            if (kr.money > 0) {
                                try {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                            "eco give " + killer.getName() + " " + kr.money);
                                } catch (Exception ignored) {}
                            }
                            if (kr.drops != null && !kr.drops.isEmpty()) {
                                for (ItemStack it : kr.drops) {
                                    le.getWorld().dropItemNaturally(le.getLocation(), it.clone());
                                }
                            }
                        }
                    }
                }
                WaveImpl w = g.currentWave instanceof WaveImpl ? (WaveImpl) g.currentWave : null;
                if (w != null) w.killed++;
                fire(new HordeEventImpl(HordeEvent.Type.MOB_KILL, g, killer, le));
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Game hg = gameOf(p);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return;
        SurvivorImpl s = g.survivors.get(p.getUniqueId());
        if (s == null) return;
        s.deaths++;
        s.alive = false;
        s.downed = false;
        Player killer = p.getKiller();
        if (killer != null && g.survivors.containsKey(killer.getUniqueId())) {
            SurvivorImpl ks = g.survivors.get(killer.getUniqueId());
            ks.kills++;
        }
        fire(new HordeEventImpl(HordeEvent.Type.PLAYER_DEATH, g, p, killer));
        long aliveCount = g.survivors.values().stream().filter(sv -> sv.alive).count();
        if (aliveCount == 0) g.endInternal();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { leave(e.getPlayer()); }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof LivingEntity)) return;
        Player p = (Player) e.getEntity();
        Game hg = gameOf(p);
        GameImpl g = hg instanceof GameImpl ? (GameImpl) hg : null;
        if (g == null) return;
        SurvivorImpl s = g.survivors.get(p.getUniqueId());
        if (s == null || !s.alive || s.downed) return;
        double dmg = e.getFinalDamage();
        if (p.getHealth() - dmg <= 0.5) {
            e.setCancelled(true);
            downPlayer(p, 30);
            return;
        }
        s.damageTaken += (int) dmg;
    }

    private void saveStats(String arenaId, SurvivorImpl s) {
        stats.computeIfAbsent(arenaId, k -> new ConcurrentHashMap<>());
        Map<String, PlayerStats> am = stats.get(arenaId);
        PlayerStats ps = am.computeIfAbsent(s.uuid.toString(), k -> new PlayerStats());
        ps.gamesPlayed++;
        ps.totalKills += s.kills;
        ps.totalDeaths += s.deaths;
        ps.bestWave = Math.max(ps.bestWave, s.waveReached);
        ps.bestScore = Math.max(ps.bestScore, s.score);
    }

    @Override public int getWavesSurvived(OfflinePlayer p, String arenaId) { return getBestWave(p, arenaId); }
    @Override public int getTotalKills(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.totalKills : 0;
    }
    @Override public int getTotalDeaths(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.totalDeaths : 0;
    }
    @Override public int getBestWave(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.bestWave : 0;
    }
    @Override public int getBestScore(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.bestScore : 0;
    }
    @Override public int getGamesPlayed(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.gamesPlayed : 0;
    }
    @Override public int getGamesWon(OfflinePlayer p, String arenaId) {
        PlayerStats ps = getStats(arenaId, p.getUniqueId());
        return ps != null ? ps.gamesWon : 0;
    }
    @Override public void resetStats(String arenaId, OfflinePlayer p) {
        Map<String, PlayerStats> am = stats.get(arenaId);
        if (am != null) am.remove(p.getUniqueId().toString());
    }

    private PlayerStats getStats(String arenaId, UUID uuid) {
        Map<String, PlayerStats> am = stats.get(arenaId);
        return am != null ? am.get(uuid.toString()) : null;
    }

    @Override public void setWorldTimeSpeed(Game g, int multiplier) {
        if (g instanceof GameImpl) ((GameImpl) g).timeSpeed = Math.max(1, multiplier);
    }
    @Override public void setBloodMoonChance(String arenaId, double chance) {
        ArenaImpl a = arenas.get(arenaId);
        if (a != null) a.bloodMoonChance = Math.max(0, Math.min(1, chance));
    }
    @Override public boolean isBloodMoon(Game g) {
        return g instanceof GameImpl && ((GameImpl) g).bloodMoon;
    }

    // ==== Internal impl classes ====

    static class ArenaImpl implements Arena {
        final String id, name, world;
        final int minPlayers, maxPlayers, borderRadius, maxWaves, prepareSeconds, waveIntervalSeconds;
        final List<Location> spawnPoints, mobSpawnPoints;
        final Location lobbyLocation, spectatorLocation, borderCenter;
        final Difficulty defaultDifficulty;
        final boolean allowBuild;
        final Map<Integer, List<MobRuleImpl>> waveRules = new ConcurrentHashMap<>();
        volatile boolean inUse;
        double bloodMoonChance = 0.15;

        ArenaImpl(String id, String name, String world, int minPlayers, int maxPlayers,
                  List<Location> spawnPoints, Location lobbyLocation, Location spectatorLocation,
                  List<Location> mobSpawnPoints, int borderRadius, Location borderCenter,
                  Difficulty defaultDifficulty, int maxWaves, int prepareSeconds,
                  int waveIntervalSeconds, boolean allowBuild) {
            this.id = id; this.name = name; this.world = world;
            this.minPlayers = minPlayers; this.maxPlayers = maxPlayers;
            this.spawnPoints = new ArrayList<>(spawnPoints);
            this.lobbyLocation = lobbyLocation; this.spectatorLocation = spectatorLocation;
            this.mobSpawnPoints = new ArrayList<>(mobSpawnPoints);
            this.borderRadius = borderRadius; this.borderCenter = borderCenter;
            this.defaultDifficulty = defaultDifficulty != null ? defaultDifficulty : Difficulty.NORMAL;
            this.maxWaves = maxWaves; this.prepareSeconds = prepareSeconds;
            this.waveIntervalSeconds = waveIntervalSeconds;
            this.allowBuild = allowBuild;
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String world() { return world; }
        @Override public int minPlayers() { return minPlayers; }
        @Override public int maxPlayers() { return maxPlayers; }
        @Override public List<Location> spawnPoints() { return Collections.unmodifiableList(spawnPoints); }
        @Override public Location lobbyLocation() { return lobbyLocation; }
        @Override public Location spectatorLocation() { return spectatorLocation; }
        @Override public List<Location> mobSpawnPoints() { return Collections.unmodifiableList(mobSpawnPoints); }
        @Override public int borderRadius() { return borderRadius; }
        @Override public Location borderCenter() { return borderCenter; }
        @Override public Difficulty defaultDifficulty() { return defaultDifficulty; }
        @Override public int maxWaves() { return maxWaves; }
        @Override public int prepareSeconds() { return prepareSeconds; }
        @Override public int waveIntervalSeconds() { return waveIntervalSeconds; }
        @Override public boolean isInUse() { return inUse; }
        @Override public boolean allowBuild() { return allowBuild; }
    }

    class GameImpl implements Game {
        final String id;
        final ArenaImpl arena;
        final Difficulty difficulty;
        volatile GameState state = GameState.WAITING;
        WaveImpl currentWave;
        final List<Wave> allWavesList = new CopyOnWriteArrayList<>();
        final Map<UUID, SurvivorImpl> survivors = new ConcurrentHashMap<>();
        final Set<UUID> aliveMobs = Collections.newSetFromMap(new ConcurrentHashMap<>());
        long countdownTicks;
        long waveIntervalTicks;
        int timerSeconds;
        int score;
        int timeSpeed = 1;
        boolean bloodMoon;
        List<Consumer<List<Survivor>>> endCallbacks = new CopyOnWriteArrayList<>();

        GameImpl(String id, ArenaImpl arena, Difficulty difficulty) {
            this.id = id; this.arena = arena; this.difficulty = difficulty;
            arena.inUse = true;
        }

        void tick() {
            switch (state) {
                case COUNTDOWN:
                    if (--countdownTicks <= 0) {
                        state = GameState.PREPARING;
                        timerSeconds = arena.prepareSeconds;
                        countdownTicks = (long) arena.prepareSeconds * 20;
                        fire(new HordeEventImpl(HordeEvent.Type.PREPARE_START, this, null, null));
                    }
                    break;
                case PREPARING:
                    if (--countdownTicks <= 0) startNextWave();
                    break;
                case WAVE_ACTIVE:
                case BOSS_WAVE:
                    if (currentWave != null) {
                        currentWave.elapsedTicks++;
                        int aliveMobCount = 0;
                        for (UUID uid : aliveMobs) {
                            Entity e = Bukkit.getEntity(uid);
                            if (e != null && !e.isDead()) aliveMobCount++;
                        }
                        if (aliveMobCount == 0 && currentWave.spawned >= currentWave.totalMobs) endWave();
                    }
                    break;
                case WAVE_INTERVAL:
                    if (--waveIntervalTicks <= 0) startNextWave();
                    break;
                default: break;
            }
            for (SurvivorImpl s : survivors.values()) {
                if (s.downed && s.downTimer > 0 && --s.downTimer == 0) {
                    Player p = Bukkit.getPlayer(s.uuid);
                    if (p != null) p.setHealth(0);
                }
            }
        }

        void startNextWave() {
            int nextNum = (currentWave == null ? 0 : currentWave.number) + 1;
            if (nextNum > arena.maxWaves) { endInternal(); return; }
            if (nextNum == 1 && Math.random() < arena.bloodMoonChance) bloodMoon = true;
            WaveImpl w = buildWave(nextNum);
            currentWave = w;
            allWavesList.add(w);
            for (SurvivorImpl s : survivors.values()) s.waveReached = Math.max(s.waveReached, nextNum);
            boolean isBoss = nextNum % 5 == 0 || nextNum == arena.maxWaves;
            state = isBoss ? GameState.BOSS_WAVE : GameState.WAVE_ACTIVE;
            spawnWaveMobs(this, w);
            fire(new HordeEventImpl(HordeEvent.Type.WAVE_START, this, null, w));
            if (isBoss) fire(new HordeEventImpl(HordeEvent.Type.BOSS_WAVE_START, this, null, w));
        }

        WaveImpl buildWave(int num) {
            double hMul = 1 + 0.08 * (num - 1);
            double dMul = 1 + 0.05 * (num - 1);
            double countMul = 1 + 0.15 * (num - 1);
            boolean isBoss = num % 5 == 0;
            List<MobRuleImpl> rules = arena.waveRules.getOrDefault(num, new ArrayList<>());
            if (rules.isEmpty()) {
                rules.add(new MobRuleImpl(EntityType.ZOMBIE, SpawnType.NORMAL, 40,
                        (int)(3*countMul), (int)(6*countMul), hMul, dMul, 1.0, null, null));
                if (num >= 3) rules.add(new MobRuleImpl(EntityType.SKELETON, SpawnType.NORMAL, 25,
                        (int)(1*countMul), (int)(3*countMul), hMul, dMul*0.9, 1.0, null, null));
                if (num >= 5) rules.add(new MobRuleImpl(EntityType.CREEPER, SpawnType.NORMAL, 20,
                        1, (int)(2*countMul), hMul, dMul*0.8, 1.1, null, null));
                if (num >= 7) rules.add(new MobRuleImpl(EntityType.WITCH, SpawnType.SPECIAL, 10,
                        0, (int)(1+countMul*0.3), hMul*0.8, dMul*1.5, 0.8, Arrays.asList("SLOW:10:1"), null));
                if (num % 3 == 0) rules.add(new MobRuleImpl(EntityType.ZOMBIE, SpawnType.ELITE, 5,
                        0, 1, hMul*3, dMul*2, 1.2, Arrays.asList("STRENGTH:60:2","SPEED:60:1"), null));
                if (isBoss) {
                    EntityType bt = num % 10 == 0 ? EntityType.WITHER : EntityType.IRON_GOLEM;
                    rules.add(new MobRuleImpl(bt, SpawnType.BOSS, 1,
                            1, 1, hMul*8, dMul*3, 0.8, Arrays.asList("STRENGTH:99999:3"), null));
                }
            }
            int total = 0;
            for (MobRuleImpl r : rules) total += r.maxCount;
            return new WaveImpl(num, difficulty, total, rules, isBoss, 300L * 20);
        }

        void endWave() {
            WaveImpl w = currentWave;
            fire(new HordeEventImpl(HordeEvent.Type.WAVE_END, this, null, w));
            WaveReward wr = waveRewards.get(w.number);
            double rMul = getDifficultyMultiplier(difficulty, "reward");
            for (SurvivorImpl s : survivors.values()) {
                if (!s.alive) continue;
                s.score += 100 * w.number;
                Player p = Bukkit.getPlayer(s.uuid);
                if (wr != null) {
                    s.score += (int) (wr.scorePerPlayer * rMul);
                    if (wr.moneyPerPlayer > 0 && p != null) {
                        try {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    "eco give " + p.getName() + " " + (wr.moneyPerPlayer * rMul));
                        } catch (Exception ignored) {}
                    }
                    if (wr.items != null && p != null) {
                        for (ItemStack it : wr.items) p.getInventory().addItem(it.clone());
                    }
                }
                fire(new HordeEventImpl(HordeEvent.Type.SCORE_CHANGE, this, p, s.score));
                fire(new HordeEventImpl(HordeEvent.Type.REWARD_GRANT, this, p, w.number));
            }
            if (w.number >= arena.maxWaves) { endInternal(); return; }
            state = GameState.WAVE_INTERVAL;
            waveIntervalTicks = (long) arena.waveIntervalSeconds * 20;
        }

        void endInternal() {
            if (state == GameState.ENDING) return;
            state = GameState.ENDING;
            clearArenaMobs(id);
            boolean won = currentWave != null && currentWave.number >= arena.maxWaves
                    && survivors.values().stream().anyMatch(s -> s.alive);
            for (SurvivorImpl s : new ArrayList<>(survivors.values())) {
                saveStats(arena.id, s);
                if (won) {
                    PlayerStats ps = getStats(arena.id, s.uuid);
                    if (ps != null) ps.gamesWon++;
                }
            }
            if (won) fire(new HordeEventImpl(HordeEvent.Type.SURVIVOR_WIN, this, null, null));
            List<Survivor> snapshot = new ArrayList<>(survivors.values());
            for (Consumer<List<Survivor>> cb : endCallbacks) {
                try { cb.accept(snapshot); } catch (Exception ignored) {}
            }
            fire(new HordeEventImpl(HordeEvent.Type.GAME_END, this, null, won));
            arena.inUse = false;
        }

        @Override public String id() { return id; }
        @Override public Arena arena() { return arena; }
        @Override public GameState state() { return state; }
        @Override public Difficulty difficulty() { return difficulty; }
        @Override public Wave currentWave() { return currentWave; }
        @Override public List<Wave> allWaves() { return Collections.unmodifiableList(allWavesList); }
        @Override public List<Survivor> survivors() { return new ArrayList<>(survivors.values()); }
        @Override public List<LivingEntity> aliveMobs() {
            List<LivingEntity> list = new ArrayList<>();
            for (UUID uid : aliveMobs) {
                Entity e = Bukkit.getEntity(uid);
                if (e instanceof LivingEntity && !e.isDead()) list.add((LivingEntity) e);
            }
            return list;
        }
        @Override public int aliveCount() { return (int) survivors.values().stream().filter(s -> s.alive).count(); }
        @Override public int timerSeconds() { return timerSeconds; }
        @Override public int score() { return score; }
        @Override public Survivor survivor(UUID uuid) { return survivors.get(uuid); }
        @Override public boolean isInGame(Player p) { return survivors.containsKey(p.getUniqueId()); }
        @Override public void onEnd(Consumer<List<Survivor>> callback) { endCallbacks.add(callback); }
    }

    static class WaveImpl implements Wave {
        final int number;
        final Difficulty difficulty;
        final int totalMobs;
        final List<MobRuleImpl> rules;
        final boolean bossWave;
        final long durationTicks;
        long elapsedTicks;
        int spawned, killed;

        WaveImpl(int number, Difficulty difficulty, int totalMobs, List<MobRuleImpl> rules, boolean bossWave, long durationTicks) {
            this.number = number; this.difficulty = difficulty;
            this.totalMobs = totalMobs; this.rules = rules;
            this.bossWave = bossWave; this.durationTicks = durationTicks;
        }
        @Override public int number() { return number; }
        @Override public Difficulty difficulty() { return difficulty; }
        @Override public int totalMobs() { return totalMobs; }
        @Override public int spawned() { return spawned; }
        @Override public int remaining() { return Math.max(0, totalMobs - killed); }
        @Override public int killed() { return killed; }
        @Override public int intervalSeconds() { return 0; }
        @Override public boolean isBossWave() { return bossWave; }
        @Override public List<MobRule> mobRules() { return new ArrayList<>(rules); }
        @Override public long durationTicks() { return durationTicks; }
        @Override public long elapsedTicks() { return elapsedTicks; }
    }

    public static class MobRuleImpl implements MobRule {
        public EntityType type;
        public SpawnType spawnType;
        public int weight, minCount, maxCount;
        public double healthMul, damageMul, speedMul;
        public List<String> effects;
        public Map<EntityType, Double> equipmentChance;

        public MobRuleImpl(EntityType type, SpawnType spawnType, int weight,
                           int minCount, int maxCount, double healthMul, double damageMul, double speedMul,
                           List<String> effects, Map<EntityType, Double> equipmentChance) {
            this.type = type; this.spawnType = spawnType; this.weight = weight;
            this.minCount = minCount; this.maxCount = maxCount;
            this.healthMul = healthMul; this.damageMul = damageMul; this.speedMul = speedMul;
            this.effects = effects; this.equipmentChance = equipmentChance;
        }
        @Override public EntityType type() { return type; }
        @Override public SpawnType spawnType() { return spawnType; }
        @Override public int weight() { return weight; }
        @Override public int minCount() { return minCount; }
        @Override public int maxCount() { return maxCount; }
        @Override public double healthMul() { return healthMul; }
        @Override public double damageMul() { return damageMul; }
        @Override public double speedMul() { return speedMul; }
        @Override public List<String> effects() { return effects; }
        @Override public Map<EntityType, Double> equipmentChance() { return equipmentChance; }
    }

    static class SurvivorImpl implements Survivor {
        final UUID uuid;
        final String name;
        int kills, deaths, damageDealt, damageTaken, revived, score, waveReached;
        boolean alive = true, downed;
        int downTimer;

        SurvivorImpl(UUID uuid, String name) {
            this.uuid = uuid; this.name = name;
        }
        @Override public UUID uuid() { return uuid; }
        @Override public String name() { return name; }
        @Override public int kills() { return kills; }
        @Override public int deaths() { return deaths; }
        @Override public int damageDealt() { return damageDealt; }
        @Override public int damageTaken() { return damageTaken; }
        @Override public int revived() { return revived; }
        @Override public boolean isAlive() { return alive; }
        @Override public boolean isDowned() { return downed; }
        @Override public int downTimerSeconds() { return downTimer; }
        @Override public int score() { return score; }
        @Override public int waveReached() { return waveReached; }
    }

    static class HordeEventImpl implements HordeEvent {
        final Type type;
        final Game game;
        final Player player;
        final Object data;

        HordeEventImpl(Type type, Game game, Player player, Object data) {
            this.type = type; this.game = game; this.player = player; this.data = data;
        }
        @Override public Type type() { return type; }
        @Override public Game game() { return game; }
        @Override public Player player() { return player; }
        @Override public Object data() { return data; }
    }

    static class WaveReward {
        final List<ItemStack> items;
        final double moneyPerPlayer;
        final int scorePerPlayer;
        WaveReward(List<ItemStack> items, double moneyPerPlayer, int scorePerPlayer) {
            this.items = items; this.moneyPerPlayer = moneyPerPlayer; this.scorePerPlayer = scorePerPlayer;
        }
    }

    static class KillReward {
        final int score;
        final double money;
        final List<ItemStack> drops;
        KillReward(int score, double money, List<ItemStack> drops) {
            this.score = score; this.money = money; this.drops = drops;
        }
    }

    static class EliteDef {
        final EntityType base;
        final String displayName;
        final double healthMul, damageMul, speedMul;
        final List<String> effects;
        final Map<String, Object> skills;
        EliteDef(EntityType base, String displayName, double healthMul, double damageMul, double speedMul,
                 List<String> effects, Map<String, Object> skills) {
            this.base = base; this.displayName = displayName;
            this.healthMul = healthMul; this.damageMul = damageMul; this.speedMul = speedMul;
            this.effects = effects; this.skills = skills;
        }
    }

    static class BossDef {
        final EntityType base;
        final String displayName;
        final double healthMul, damageMul, speedMul;
        final List<String> effects;
        final Map<String, Object> skills;
        final List<ItemStack> drops;
        final int scoreReward;
        BossDef(EntityType base, String displayName, double healthMul, double damageMul, double speedMul,
                List<String> effects, Map<String, Object> skills, List<ItemStack> drops, int scoreReward) {
            this.base = base; this.displayName = displayName;
            this.healthMul = healthMul; this.damageMul = damageMul; this.speedMul = speedMul;
            this.effects = effects; this.skills = skills;
            this.drops = drops; this.scoreReward = scoreReward;
        }
    }

    static class PlayerStats {
        int gamesPlayed, gamesWon, totalKills, totalDeaths, bestWave, bestScore;
    }
}