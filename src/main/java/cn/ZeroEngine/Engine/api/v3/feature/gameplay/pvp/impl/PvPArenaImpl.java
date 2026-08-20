package cn.ZeroEngine.Engine.api.v3.feature.gameplay.pvp.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.pvp.PvPArena;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class PvPArenaImpl implements PvPArena, Listener {

    private final JavaPlugin plugin;
    private final Map<String, ArenaImpl> arenas = new ConcurrentHashMap<>();
    private final Map<String, MatchImpl> matches = new ConcurrentHashMap<>();
    private final Map<String, KitImpl> kits = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Object>> stats = new ConcurrentHashMap<>();
    private final Map<Mode, List<UUID>> queues = new ConcurrentHashMap<>();
    private final List<EventHolder> listeners = new CopyOnWriteArrayList<>();
    private final Map<Mode, Map<String, Object>> modeRules = new ConcurrentHashMap<>();
    private BukkitTask mainTask, matchmakingTask;
    private int matchmakerInterval = 100;
    private final List<RankImpl> ranks = new CopyOnWriteArrayList<>(Arrays.asList(
            new RankImpl(1, "青铜", "§8[青铜]", 0, 1099),
            new RankImpl(2, "白银", "§7[白银]", 1100, 1399),
            new RankImpl(3, "黄金", "§e[黄金]", 1400, 1699),
            new RankImpl(4, "铂金", "§b[铂金]", 1700, 1999),
            new RankImpl(5, "钻石", "§9[钻石]", 2000, 2299),
            new RankImpl(6, "大师", "§5[大师]", 2300, 2599),
            new RankImpl(7, "宗师", "§6[宗师]", 2600, Integer.MAX_VALUE)
    ));

    public PvPArenaImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        for (Mode m : Mode.values()) queues.put(m, new CopyOnWriteArrayList<>());
        for (Mode m : Mode.values()) modeRules.put(m, new ConcurrentHashMap<>());
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (mainTask != null) mainTask.cancel();
        if (matchmakingTask != null) matchmakingTask.cancel();
        arenas.clear(); matches.clear(); kits.clear(); listeners.clear();
    }

    @Override public void registerArena(String id, String name, List<Location> spawnsA, List<Location> spawnsB, Location waitingLobby, Location spectatorSpawn, int maxPlayers, List<String> allowedModes, boolean allowBuild, boolean allowInteract, int resetAfterSeconds) {
        arenas.put(id, new ArenaImpl(id, name, maxPlayers, spawnsA, spawnsB, waitingLobby, spectatorSpawn, allowedModes, allowBuild, allowInteract, resetAfterSeconds));
    }
    @Override public void unregisterArena(String id) { arenas.remove(id); }
    @Override public Arena getArena(String id) { return arenas.get(id); }
    @Override public Collection<Arena> allArenas() { return Collections.unmodifiableCollection(arenas.values()); }

    @Override public void registerKit(Kit kit) { kits.put(kit.id(), kit instanceof KitImpl ? (KitImpl)kit : new KitImpl(kit)); }
    @Override public Kit getKit(String id) { return kits.get(id); }
    @Override public Collection<Kit> allKits() { return Collections.unmodifiableCollection(kits.values()); }

    @Override public boolean giveKit(Player p, String kitId) {
        KitImpl k = kits.get(kitId);
        if (k == null) return false;
        if (k.permission != null && !k.permission.isEmpty() && !p.hasPermission(k.permission)) return false;
        p.getInventory().clear();
        for (ItemStack it : k.inventory()) p.getInventory().addItem(it);
        p.getInventory().setArmorContents(k.armor().toArray(new ItemStack[0]));
        for (String ef : k.effects) {
            try {
                String[] pcs = ef.split(":", 3);
                PotionEffectType pet = PotionEffectType.getByName(pcs[0].toUpperCase());
                if (pet == null) continue;
                int dur = pcs.length > 1 ? Integer.parseInt(pcs[1])*20 : 20*999999;
                int amp = pcs.length > 2 ? Integer.parseInt(pcs[2])-1 : 0;
                p.addPotionEffect(new PotionEffect(pet, dur, amp, true, false));
            } catch (Exception ignored) {}
        }
        if (k.healthScale > 0) {
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(k.healthScale);
            p.setHealth(k.healthScale);
        }
        if (k.walkSpeed > 0) p.setWalkSpeed((float) k.walkSpeed);
        return true;
    }

    @Override public boolean applyKitToTeam(Match m, List<UUID> team, String kitId) {
        boolean ok = true;
        for (UUID u : team) { Player p = Bukkit.getPlayer(u); if (p != null) ok &= giveKit(p, kitId); }
        return ok;
    }

    @Override public Match createMatch(String arenaId, Mode mode) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return null;
        MatchImpl m = new MatchImpl(arenaId, a, mode);
        matches.put(arenaId, m);
        return m;
    }
    @Override public Match getMatch(String arenaId) { return matches.get(arenaId); }
    @Override public Match matchOf(Player p) {
        for (MatchImpl m : matches.values())
            if (m.teamA.contains(p.getUniqueId()) || m.teamB.contains(p.getUniqueId())) return m;
        return null;
    }

    @Override public boolean joinTeamA(Player p, String arenaId) { return joinTeam(p, arenaId, true); }
    @Override public boolean joinTeamB(Player p, String arenaId) { return joinTeam(p, arenaId, false); }
    private boolean joinTeam(Player p, String arenaId, boolean a) {
        MatchImpl m = matches.computeIfAbsent(arenaId, id -> {
            ArenaImpl ar = arenas.get(id); if (ar == null) return null;
            return new MatchImpl(id, ar, Mode.DUEL_1V1);
        });
        if (m == null || m.state != GameState.WAITING && m.state != GameState.COUNTDOWN) return false;
        UUID u = p.getUniqueId();
        if (a) m.teamA.add(u); else m.teamB.add(u);
        fire(new MatchEventImpl(MatchEvent.Type.JOIN, m, p, a ? "A" : "B"));
        return true;
    }
    @Override public boolean joinFFA(Player p, String arenaId) {
        MatchImpl m = matches.computeIfAbsent(arenaId, id -> {
            ArenaImpl ar = arenas.get(id); if (ar == null) return null;
            return new MatchImpl(id, ar, Mode.FFA);
        });
        if (m == null || m.state != GameState.WAITING && m.state != GameState.COUNTDOWN) return false;
        if (m.teamA.size() >= m.arena.maxPlayers) return false;
        m.teamA.add(p.getUniqueId());
        fire(new MatchEventImpl(MatchEvent.Type.JOIN, m, p, "FFA"));
        return true;
    }
    @Override public boolean leave(Player p) {
        UUID u = p.getUniqueId();
        for (MatchImpl m : matches.values()) {
            boolean r = m.teamA.remove(u) || m.teamB.remove(u);
            if (r) { m.spectators.remove(p); fire(new MatchEventImpl(MatchEvent.Type.LEAVE, m, p, null)); return true; }
        }
        return false;
    }

    @Override public boolean setKit(Player p, String kitId) {
        Match m = matchOf(p);
        if (m == null) return false;
        boolean r = giveKit(p, kitId);
        if (r) fire(new MatchEventImpl(MatchEvent.Type.KIT_CHANGE, m, p, kitId));
        return r;
    }

    @Override public void startCountdown(String arenaId, int seconds) {
        MatchImpl m = matches.get(arenaId); if (m == null) return;
        m.state = GameState.COUNTDOWN;
        m.timeLeftSeconds = seconds;
        fire(new MatchEventImpl(MatchEvent.Type.COUNTDOWN_START, m, null, seconds));
    }
    @Override public void forceStart(String arenaId) {
        MatchImpl m = matches.get(arenaId); if (m == null) return;
        m.state = GameState.FIGHTING; m.timeLeftSeconds = 0;
        fire(new MatchEventImpl(MatchEvent.Type.START, m, null, null));
    }
    @Override public void forceEnd(String arenaId, MatchResult result) {
        MatchImpl m = matches.get(arenaId); if (m == null) return;
        endMatch(m, result);
    }

    @Override public void addKill(Match m, Player killer, Player victim) {
        MatchImpl im = (MatchImpl) m;
        UUID k = killer.getUniqueId();
        boolean isA = im.teamA.contains(k);
        if (isA) im.scoreA++; else im.scoreB++;
        mergeStat(killer, m.mode(), "kills", 1);
        mergeStat(victim, m.mode(), "deaths", 1);
        fire(new MatchEventImpl(MatchEvent.Type.KILL, m, killer, victim));
    }

    @Override public void addDeath(Match m, Player victim) {
        mergeStat(victim, m.mode(), "deaths", 1);
        fire(new MatchEventImpl(MatchEvent.Type.DEATH, m, victim, null));
    }

    @Override public boolean isPvPAllowedInMatch(Match m) {
        MatchImpl im = (MatchImpl) m;
        return im.state == GameState.FIGHTING;
    }

    @Override public void setModeRule(Mode m, String rule, Object value) { modeRules.computeIfAbsent(m, k->new ConcurrentHashMap<>()).put(rule, value); }
    @Override public Object getModeRule(Mode m, String rule) { return modeRules.computeIfAbsent(m, k->new ConcurrentHashMap<>()).get(rule); }

    @Override public int getElo(OfflinePlayer p) { return getElo(p, Mode.RANKED); }
    @Override public int getElo(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("elo_"+mode.name(), 1200); }
    @Override public void setElo(OfflinePlayer p, Mode mode, int elo) { playerMap(p).put("elo_"+mode.name(), elo); fireGlobal(null, MatchEvent.Type.ELO_CHANGE, p.getUniqueId()+"_"+mode.name(), elo); }
    @Override public void addElo(OfflinePlayer p, Mode mode, int delta) { setElo(p, mode, getElo(p, mode)+delta); }

    @Override public Rank getRank(int elo) {
        RankImpl best = ranks.get(0);
        for (RankImpl r : ranks) if (elo >= r.requiredElo) best = r;
        return best;
    }
    @Override public List<Rank> allRanks() { return new ArrayList<>(ranks); }

    @Override public int getWins(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("wins_"+mode.name(), 0); }
    @Override public int getLosses(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("losses_"+mode.name(), 0); }
    @Override public int getWinStreak(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("ws_"+mode.name(), 0); }
    @Override public int getBestWinStreak(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("bws_"+mode.name(), 0); }
    @Override public int getKills(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("kills_"+mode.name(), 0); }
    @Override public int getDeaths(OfflinePlayer p, Mode mode) { return (int) playerMap(p).getOrDefault("deaths_"+mode.name(), 0); }
    @Override public void resetStats(OfflinePlayer p) { stats.remove(p.getUniqueId()); }

    private Map<String, Object> playerMap(OfflinePlayer p) {
        return stats.computeIfAbsent(p.getUniqueId(), k -> new ConcurrentHashMap<>());
    }
    private void mergeStat(Player p, Mode mode, String k, int v) {
        mergeStat0(p.getUniqueId(), mode, k, v);
    }
    private void mergeStat(OfflinePlayer p, Mode mode, String k, int v) {
        mergeStat0(p.getUniqueId(), mode, k, v);
    }
    private void mergeStat0(UUID u, Mode mode, String k, int v) {
        stats.computeIfAbsent(u, x->new ConcurrentHashMap<>()).merge(k+"_"+mode.name(), v, (a,b) -> ((Integer)a)+((Integer)b));
    }

    @Override public void onEvent(Consumer<MatchEvent> listener) { listeners.add(new EventHolder(null, listener)); }
    @SuppressWarnings("unchecked")
    @Override public <T extends MatchEvent> void onEvent(Class<T> type, Consumer<T> listener) { listeners.add(new EventHolder(type, (Consumer<MatchEvent>) listener)); }

    @Override public boolean queuePlayer(Player p, Mode mode) {
        queues.get(mode).add(p.getUniqueId());
        return true;
    }
    @Override public boolean dequeuePlayer(Player p) {
        UUID u = p.getUniqueId();
        boolean r = false;
        for (List<UUID> l : queues.values()) r |= l.remove(u);
        return r;
    }
    @Override public Map<Mode, List<UUID>> queueSnapshot() { Map<Mode, List<UUID>> r = new EnumMap<>(Mode.class); for (Map.Entry<Mode, List<UUID>> e : queues.entrySet()) r.put(e.getKey(), new ArrayList<>(e.getValue())); return r; }
    @Override public void startAutoMatchmaking() {
        if (matchmakingTask != null) return;
        matchmakingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::matchmake, 0, matchmakerInterval);
    }
    @Override public void stopAutoMatchmaking() {
        if (matchmakingTask != null) { matchmakingTask.cancel(); matchmakingTask = null; }
    }
    @Override public void setMatchmakerInterval(int ticks) {
        this.matchmakerInterval = ticks;
        if (matchmakingTask != null) { matchmakingTask.cancel(); matchmakingTask = Bukkit.getScheduler().runTaskTimer(plugin, this::matchmake, 0, ticks); }
    }

    private void matchmake() {
        for (Map.Entry<Mode, List<UUID>> e : queues.entrySet()) {
            Mode m = e.getKey(); List<UUID> q = e.getValue();
            int need = neededPerMatch(m);
            while (q.size() >= need) {
                String freeArena = findFreeArena(m);
                if (freeArena == null) break;
                MatchImpl match = (MatchImpl) createMatch(freeArena, m);
                for (int i = 0; i < need; i++) {
                    UUID u = q.remove(0);
                    if (m == Mode.FFA) match.teamA.add(u);
                    else if (i < need/2) match.teamA.add(u);
                    else match.teamB.add(u);
                }
                startCountdown(freeArena, 5);
            }
        }
    }

    private int neededPerMatch(Mode m) {
        switch (m) {
            case DUEL_1V1: return 2;
            case TEAM_2V2: return 4;
            case TEAM_3V3: return 6;
            case TEAM_5V5: return 10;
            case BATTLE_ROYALE: return 16;
            case FFA: return 4;
            default: return 2;
        }
    }
    private String findFreeArena(Mode m) {
        for (ArenaImpl a : arenas.values()) {
            if (a.allowedModes.contains(m.name()) && !matches.containsKey(a.id)) return a.id;
        }
        return null;
    }

    @Override public void registerSpectator(Player p, Match m) { ((MatchImpl)m).spectators.add(p); }
    @Override public void removeSpectator(Player p, Match m) { ((MatchImpl)m).spectators.remove(p); }
    @Override public List<Player> spectators(Match m) {
        List<Player> r = new ArrayList<>();
        for (Player p : ((MatchImpl)m).spectators) if (p.isOnline()) r.add(p);
        return r;
    }

    @Override public void damageMatchPlayer(Match m, Player attacker, Player victim, double damage, EntityType cause) {
        if (isPvPAllowedInMatch(m)) {
            victim.damage(damage, attacker);
        }
    }

    private void fire(MatchEvent ev) {
        for (EventHolder h : listeners) {
            if (h.type == null || h.type.isInstance(ev)) h.cb.accept(ev);
        }
    }
    @SuppressWarnings("unused")
    private void fireGlobal(Match m, MatchEvent.Type t, Object d, Object data) { fire(new MatchEventImpl(t, m, null, data)); }

    private void tick() {
        for (MatchImpl m : new ArrayList<>(matches.values())) {
            if (m.state == GameState.COUNTDOWN) {
                if (m.timeLeftSeconds > 0) { if (m.timeLeftSeconds % 20 == 0) {} m.timeLeftSeconds--; }
                else { m.state = GameState.FIGHTING; fire(new MatchEventImpl(MatchEvent.Type.START, m, null, null)); }
            } else if (m.state == GameState.FIGHTING) {
                m.timeLeftSeconds++;
                List<UUID> aliveA = new ArrayList<>(), aliveB = new ArrayList<>();
                for (UUID u : m.teamA) if (Bukkit.getPlayer(u) != null) aliveA.add(u);
                for (UUID u : m.teamB) if (Bukkit.getPlayer(u) != null) aliveB.add(u);
                if (m.mode == Mode.FFA) {
                    if (aliveA.size() + aliveB.size() <= 1) endMatch(m, aliveA.isEmpty() ? MatchResult.WIN_B : MatchResult.WIN_A);
                } else {
                    if (aliveA.isEmpty() || aliveB.isEmpty()) {
                        MatchResult r = aliveA.isEmpty() ? (aliveB.isEmpty() ? MatchResult.DRAW : MatchResult.WIN_B) : MatchResult.WIN_A;
                        endMatch(m, r);
                    }
                }
            }
        }
    }

    private void endMatch(MatchImpl m, MatchResult r) {
        if (m.state == GameState.ENDING) return;
        m.state = GameState.ENDING;
        for (Consumer<MatchResult> cb : m.endCbs) cb.accept(r);
        fire(new MatchEventImpl(MatchEvent.Type.MATCH_END, m, null, r));
        List<UUID> winners = r == MatchResult.WIN_A ? m.teamA : r == MatchResult.WIN_B ? m.teamB : new ArrayList<>();
        for (UUID u : winners) {
            Map<String, Object> s = stats.computeIfAbsent(u, k->new ConcurrentHashMap<>());
            s.merge("wins_"+m.mode.name(), 1, (a,b) -> ((Integer)a) + ((Integer)b));
            s.merge("ws_"+m.mode.name(), 1, (a,b) -> ((Integer)a) + ((Integer)b));
            int ws = (int) s.getOrDefault("ws_"+m.mode.name(), 1);
            s.merge("bws_"+m.mode.name(), ws, (a,b) -> Math.max((Integer)a, (Integer)b));
            if (m.mode == Mode.RANKED) s.merge("elo_RANKED", 12, (a,b) -> ((Integer)a) + ((Integer)b));
        }
        List<UUID> losers = r == MatchResult.WIN_A ? m.teamB : r == MatchResult.WIN_B ? m.teamA : new ArrayList<>();
        for (UUID u : losers) {
            Map<String, Object> s = stats.computeIfAbsent(u, k->new ConcurrentHashMap<>());
            s.merge("losses_"+m.mode.name(), 1, (a,b) -> ((Integer)a) + ((Integer)b));
            s.put("ws_"+m.mode.name(), 0);
            if (m.mode == Mode.RANKED) s.merge("elo_RANKED", -10, (a,b) -> ((Integer)a) + ((Integer)b));
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> matches.remove(m.arenaId), 200L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player d = e.getEntity();
        Match m = matchOf(d);
        if (m == null) return;
        addDeath(m, d);
        Player k = d.getKiller();
        if (k != null && (m.teamA().contains(k.getUniqueId()) || m.teamB().contains(k.getUniqueId()))) addKill(m, k, d);
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Match m = matchOf(e.getPlayer());
        if (m == null) return;
        ArenaImpl a = ((MatchImpl)m).arena;
        UUID u = e.getPlayer().getUniqueId();
        List<Location> list = m.teamA().contains(u) ? a.spawnsA : a.spawnsB;
        if (list != null && !list.isEmpty()) e.setRespawnLocation(list.get(0));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) { leave(e.getPlayer()); dequeuePlayer(e.getPlayer()); }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Player v = (Player) e.getEntity();
        Match m = matchOf(v);
        if (m == null) return;
        if (!isPvPAllowedInMatch(m)) { e.setCancelled(true); return; }
        if (e.getDamager() instanceof Player) {
            Player a = (Player) e.getDamager();
            Match m2 = matchOf(a);
            if (m2 == null || m2 != m) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageFall(EntityDamageEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        Match m = matchOf((Player)e.getEntity());
        if (m != null && ((MatchImpl)m).state != GameState.FIGHTING) e.setCancelled(true);
    }

    static class EventHolder {
        final Class<?> type; final Consumer<MatchEvent> cb;
        EventHolder(Class<?> t, Consumer<MatchEvent> c) { type = t; cb = c; }
    }

    static class ArenaImpl implements Arena {
        final String id, name;
        final int maxPlayers, resetAfterSeconds;
        final List<Location> spawnsA, spawnsB;
        final Location waitingLobby, spectatorSpawn;
        final List<String> allowedModes;
        final boolean allowBuild, allowInteract;
        ArenaImpl(String id, String name, int max, List<Location> a, List<Location> b, Location w, Location s, List<String> am, boolean ab, boolean ai, int rs) {
            this.id = id; this.name = name; maxPlayers = max; spawnsA = a; spawnsB = b; waitingLobby = w; spectatorSpawn = s;
            allowedModes = am; allowBuild = ab; allowInteract = ai; resetAfterSeconds = rs;
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public int maxPlayers() { return maxPlayers; }
        @Override public List<Location> spawnsA() { return spawnsA; }
        @Override public List<Location> spawnsB() { return spawnsB; }
        @Override public Location waitingLobby() { return waitingLobby; }
        @Override public Location spectatorSpawn() { return spectatorSpawn; }
        @Override public List<String> allowedModes() { return allowedModes; }
        @Override public boolean isInUse() { return false; }
        @Override public boolean allowBuild() { return allowBuild; }
        @Override public boolean allowInteract() { return allowInteract; }
        @Override public int resetAfterSeconds() { return resetAfterSeconds; }
    }

    static class MatchImpl implements Match {
        final String arenaId;
        final ArenaImpl arena;
        final Mode mode;
        volatile GameState state = GameState.WAITING;
        volatile int timeLeftSeconds, round = 1;
        volatile int scoreA, scoreB;
        volatile UUID winner;
        final List<UUID> teamA = new CopyOnWriteArrayList<>();
        final List<UUID> teamB = new CopyOnWriteArrayList<>();
        final List<Player> spectators = new CopyOnWriteArrayList<>();
        final List<Consumer<MatchResult>> endCbs = new CopyOnWriteArrayList<>();
        MatchImpl(String id, ArenaImpl a, Mode m) { arenaId = id; arena = a; mode = m; }
        @Override public String id() { return arenaId+"_"+hashCode(); }
        @Override public Mode mode() { return mode; }
        @Override public GameState state() { return state; }
        @Override public Arena arena() { return arena; }
        @Override public List<UUID> teamA() { return teamA; }
        @Override public List<UUID> teamB() { return teamB; }
        @Override public List<UUID> allFighters() { List<UUID> r = new ArrayList<>(teamA); r.addAll(teamB); return r; }
        @Override public int timeLeftSeconds() { return timeLeftSeconds; }
        @Override public int round() { return round; }
        @Override public int scoreA() { return scoreA; }
        @Override public int scoreB() { return scoreB; }
        @Override public boolean isInMatch(UUID p) { return teamA.contains(p) || teamB.contains(p); }
        @Override public Player teamOf(UUID p) { return null; }
        @Override public UUID winner() { return winner; }
        @Override public void onEnd(Consumer<MatchResult> cb) { endCbs.add(cb); }
    }

    static class KitImpl implements Kit {
        final String id, name, permission;
        final int price;
        final List<ItemStack> armor, inventory;
        final List<String> effects;
        final double healthScale, walkSpeed;
        KitImpl(Kit k) { this(k.id(), k.name(), k.permission(), k.price(), k.armor(), k.inventory(), k.effects(), k.healthScale(), k.walkSpeed()); }
        KitImpl(String id, String name, String perm, int price, List<ItemStack> ar, List<ItemStack> inv, List<String> ef, double hs, double ws) {
            this.id = id; this.name = name; permission = perm; this.price = price; armor = ar; inventory = inv; effects = ef; healthScale = hs; walkSpeed = ws;
        }
        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String permission() { return permission; }
        @Override public int price() { return price; }
        @Override public List<ItemStack> armor() { return armor; }
        @Override public List<ItemStack> inventory() { return inventory; }
        @Override public List<String> effects() { return effects; }
        @Override public double healthScale() { return healthScale; }
        @Override public double walkSpeed() { return walkSpeed; }
    }

    static class RankImpl implements Rank {
        final int tier, requiredElo, maxElo;
        final String name, prefix;
        RankImpl(int t, String n, String p, int mn, int mx) { tier = t; name = n; prefix = p; requiredElo = mn; maxElo = mx; }
        @Override public int tier() { return tier; }
        @Override public String name() { return name; }
        @Override public String prefix() { return prefix; }
        @Override public int requiredElo() { return requiredElo; }
        @Override public int maxElo() { return maxElo; }
    }

    static class MatchEventImpl implements MatchEvent {
        final Type type; final Match match; final Player player; final Object data;
        MatchEventImpl(Type t, Match m, Player p, Object d) { type = t; match = m; player = p; data = d; }
        @Override public Type type() { return type; }
        @Override public Match match() { return match; }
        @Override public Player player() { return player; }
        @Override public Object data() { return data; }
    }
}