package cn.ZeroEngine.Engine.api.v3.feature.gameplay.bedwars.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.bedwars.Bedwars;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BedwarsImpl implements Bedwars, Listener {

    private final JavaPlugin plugin;
    private final Map<String, ArenaImpl> arenas = new ConcurrentHashMap<>();
    private final Map<String, GameImpl> games = new ConcurrentHashMap<>();
    private final List<Consumer<GameEvent>> listeners = new CopyOnWriteArrayList<>();
    private final List<ShopItem> shops = new CopyOnWriteArrayList<>();
    private BukkitTask mainTask;

    public BedwarsImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (mainTask != null) mainTask.cancel();
        arenas.clear();
        games.clear();
        listeners.clear();
        shops.clear();
    }

    @Override
    public void registerArena(String id, String name, String world, int minPlayers, int maxPlayersPerTeam,
                              Map<TeamColor, Location> spawns, Map<TeamColor, Location> beds, Location waitingLobby, Location spectatorSpawn,
                              List<Map<String, Object>> generators, List<Map<String, Object>> shops) {
        arenas.put(id, new ArenaImpl(id, name, world, minPlayers, maxPlayersPerTeam, spawns, beds, waitingLobby, spectatorSpawn));
    }

    @Override
    public void unregisterArena(String id) {
        arenas.remove(id);
        GameImpl g = games.remove(id);
        if (g != null) g.endInternal(null);
    }

    @Override
    public Arena getArena(String id) { return arenas.get(id); }

    @Override
    public Collection<Arena> allArenas() { return Collections.unmodifiableCollection(arenas.values()); }

    @Override
    public Game createGame(String arenaId, List<TeamColor> activeTeams) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return null;
        GameImpl g = new GameImpl(a, activeTeams);
        games.put(arenaId, g);
        fire(new GameEventImpl(GameEvent.Type.COUNTDOWN_START, g, null, null));
        return g;
    }

    @Override
    public Game getGame(String arenaId) { return games.get(arenaId); }

    @Override
    public boolean join(Player p, String arenaId, TeamColor color) {
        GameImpl g = games.computeIfAbsent(arenaId, id -> {
            ArenaImpl a = arenas.get(id);
            if (a == null) return null;
            return new GameImpl(a, new ArrayList<>(a.spawns.keySet()));
        });
        if (g == null) return false;
        if (g.state != GameState.WAITING && g.state != GameState.COUNTDOWN) return false;
        TeamImpl t = g.teams.computeIfAbsent(color, c -> new TeamImpl(c, a_of(g, c)));
        if (t.players.size() >= g.arena.maxPlayersPerTeam) return false;
        t.players.add(p.getUniqueId());
        g.playerTeam.put(p.getUniqueId(), color);
        fire(new GameEventImpl(GameEvent.Type.PLAYER_JOIN, g, p, color));
        return true;
    }

    private TeamData a_of(GameImpl g, TeamColor c) {
        ArenaImpl a = g.arena;
        return new TeamData(a.spawns.get(c), a.beds.get(c));
    }

    @Override
    public boolean leave(Player p) {
        for (GameImpl g : games.values()) {
            if (g.playerTeam.remove(p.getUniqueId()) != null) {
                for (TeamImpl t : g.teams.values()) t.players.remove(p.getUniqueId());
                fire(new GameEventImpl(GameEvent.Type.PLAYER_LEAVE, g, p, null));
                return true;
            }
        }
        return false;
    }

    @Override
    public void startCountdown(String arenaId, int seconds) {
        GameImpl g = games.get(arenaId);
        if (g != null) g.countdown = seconds;
    }

    @Override
    public void forceStart(String arenaId) {
        GameImpl g = games.get(arenaId);
        if (g != null) { g.countdown = 1; g.state = GameState.COUNTDOWN; }
    }

    @Override
    public void forceEnd(String arenaId, TeamColor winnerOrNull) {
        GameImpl g = games.get(arenaId);
        if (g != null) g.endInternal(winnerOrNull);
    }

    @Override
    public void breakBed(Player p, TeamColor color) {
        for (GameImpl g : games.values()) {
            TeamImpl t = g.teams.get(color);
            if (t != null && t.bedAlive && g.playerTeam.containsKey(p.getUniqueId())) {
                t.bedAlive = false;
                TeamImpl brk = g.teams.get(g.playerTeam.get(p.getUniqueId()));
                if (brk != null) brk.bedsBroken++;
                g.stats.merge(playerKey(p, "beds"), 1, Integer::sum);
                fire(new GameEventImpl(GameEvent.Type.BED_BROKEN, g, p, color));
            }
        }
    }

    private String playerKey(Player p, String key) { return p.getUniqueId() + "_" + key; }

    @Override
    public boolean placeBlock(Player p, int x, int y, int z, Material type) {
        GameImpl g = findGame(p);
        if (g == null) return false;
        if (g.state != GameState.PLAYING) return false;
        if (isProtected(g.arena.id(), x, y, z)) return false;
        g.placed.add(xyz(x, y, z));
        fire(new GameEventImpl(GameEvent.Type.BLOCK_PLACE, g, p, xyz(x, y, z)));
        return true;
    }

    @Override
    public boolean breakBlock(Player p, int x, int y, int z) {
        GameImpl g = findGame(p);
        if (g == null) return false;
        if (g.state != GameState.PLAYING) return false;
        long k = xyz(x, y, z);
        if (!g.placed.contains(k)) return false;
        g.placed.remove(k);
        fire(new GameEventImpl(GameEvent.Type.BLOCK_BREAK, g, p, k));
        return true;
    }

    @Override
    public boolean isProtected(String arenaId, int x, int y, int z) {
        ArenaImpl a = arenas.get(arenaId);
        if (a == null) return false;
        for (Location l : a.beds.values()) {
            if (l != null && Math.abs(l.getBlockX()-x) <= 2 && Math.abs(l.getBlockY()-y) <= 2 && Math.abs(l.getBlockZ()-z) <= 2) return true;
        }
        for (Location l : a.spawns.values()) {
            if (l != null && Math.abs(l.getBlockX()-x) <= 3 && Math.abs(l.getBlockY()-y) <= 3 && Math.abs(l.getBlockZ()-z) <= 3) return true;
        }
        return false;
    }

    @Override
    public void setGameRule(String arenaId, String rule, Object value) {
        ArenaImpl a = arenas.get(arenaId);
        if (a != null) a.rules.put(rule, value);
    }

    @Override
    public void onEvent(Consumer<GameEvent> listener) { listeners.add(listener); }

    @Override
    public void registerShopItem(Material icon, String name, int price, Material currency, List<ItemStack> rewards, Consumer<Player> onBuy) {
        shops.add(new ShopItem(icon, name, price, currency, rewards, onBuy));
    }

    public List<ShopItem> shopItems() { return shops; }

    @Override
    public void dropGenerator(String arenaId, TeamColor color, Material resource, int amountPerTick, int intervalTicks) {
        GameImpl g = games.get(arenaId);
        if (g == null) return;
        g.generators.add(new GeneratorTask(color, resource, amountPerTick, intervalTicks, 0));
    }

    @Override
    public void setResourceDrop(Location loc, Material mat, int amount) {
        World w = loc.getWorld();
        if (w != null) w.dropItemNaturally(loc, new ItemStack(mat, amount));
    }

    @Override
    public int getKills(OfflinePlayer p, String arenaId) { return statOf(p, arenaId, "kills"); }
    @Override
    public int getBedsBroken(OfflinePlayer p, String arenaId) { return statOf(p, arenaId, "beds"); }
    @Override
    public int getDeaths(OfflinePlayer p, String arenaId) { return statOf(p, arenaId, "deaths"); }
    @Override
    public int getWins(OfflinePlayer p, String arenaId) { return statOf(p, arenaId, "wins"); }

    @Override
    public void resetStats(String arenaId, OfflinePlayer p) {
        String k = p.getUniqueId().toString();
        GameImpl g = games.get(arenaId);
        if (g != null) { g.stats.remove(k+"_kills"); g.stats.remove(k+"_beds"); g.stats.remove(k+"_deaths"); g.stats.remove(k+"_wins"); }
    }

    private int statOf(OfflinePlayer p, String arenaId, String key) {
        GameImpl g = games.get(arenaId);
        if (g == null) return 0;
        return g.stats.getOrDefault(p.getUniqueId()+"_"+key, 0);
    }

    private GameImpl findGame(Player p) {
        for (GameImpl g : games.values()) if (g.playerTeam.containsKey(p.getUniqueId())) return g;
        return null;
    }

    private static long xyz(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(y & 0xFFF) << 26) | (z & 0x3FFFFFF);
    }

    private void fire(GameEvent ev) { for (Consumer<GameEvent> c : listeners) c.accept(ev); }

    private void tick() {
        for (GameImpl g : new ArrayList<>(games.values())) g.tick(plugin);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (placeBlock(p, b.getX(), b.getY(), b.getZ(), b.getType())) return;
        if (findGame(p) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (b.getType().name().contains("_BED")) {
            GameImpl g = findGame(p);
            if (g != null) {
                for (Map.Entry<TeamColor, TeamImpl> en : g.teams.entrySet()) {
                    Location bd = g.arena.beds.get(en.getKey());
                    if (bd != null && bd.getBlockX() == b.getX() && bd.getBlockZ() == b.getZ()) {
                        breakBed(p, en.getKey());
                        return;
                    }
                }
            }
        }
        if (breakBlock(p, b.getX(), b.getY(), b.getZ())) return;
        if (findGame(p) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        GameImpl g = findGame(p);
        if (g == null) return;
        g.stats.merge(p.getUniqueId()+"_deaths", 1, Integer::sum);
        Player killer = p.getKiller();
        if (killer != null && g.playerTeam.containsKey(killer.getUniqueId())) {
            TeamColor kc = g.playerTeam.get(killer.getUniqueId());
            TeamImpl kt = g.teams.get(kc);
            if (kt != null) kt.kills++;
            g.stats.merge(killer.getUniqueId()+"_kills", 1, Integer::sum);
        }
        fire(new GameEventImpl(GameEvent.Type.PLAYER_DEATH, g, p, killer));
        TeamColor pc = g.playerTeam.get(p.getUniqueId());
        if (pc != null) {
            TeamImpl t = g.teams.get(pc);
            if (!t.bedAlive) { t.players.remove(p.getUniqueId()); fire(new GameEventImpl(GameEvent.Type.PLAYER_RESPAWN, g, p, null)); }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        GameImpl g = findGame(e.getPlayer());
        if (g == null) return;
        TeamColor c = g.playerTeam.get(e.getPlayer().getUniqueId());
        if (c != null) {
            TeamImpl t = g.teams.get(c);
            if (t.spawn() != null) e.setRespawnLocation(t.spawn());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { leave(e.getPlayer()); }

    private static class ArenaImpl implements Arena {
        final String id, name, world;
        final int minPlayers, maxPlayersPerTeam;
        final Map<TeamColor, Location> spawns, beds;
        final Location waitingLobby, spectatorSpawn;
        final Map<String, Object> rules = new ConcurrentHashMap<>();
        final Map<TeamColor, Team> teamView = new HashMap<>();

        ArenaImpl(String id, String name, String world, int minPlayers, int maxPlayersPerTeam,
                  Map<TeamColor, Location> spawns, Map<TeamColor, Location> beds, Location waitingLobby, Location spectatorSpawn) {
            this.id = id; this.name = name; this.world = world;
            this.minPlayers = minPlayers; this.maxPlayersPerTeam = maxPlayersPerTeam;
            this.spawns = spawns; this.beds = beds;
            this.waitingLobby = waitingLobby; this.spectatorSpawn = spectatorSpawn;
        }

        @Override public String id() { return id; }
        @Override public String name() { return name; }
        @Override public String world() { return world; }
        @Override public int minPlayers() { return minPlayers; }
        @Override public int maxPlayers() { return maxPlayersPerTeam * spawns.size(); }
        @Override public int maxPlayersPerTeam() { return maxPlayersPerTeam; }
        @Override public Map<TeamColor, Team> teams() { return teamView; }
        @Override public GameState state() { return GameState.WAITING; }
        @Override public boolean isInGame(Player p) { return false; }
        @Override public Team teamOf(UUID uuid) { return null; }
    }

    private static class TeamData {
        final Location spawn, bed;
        TeamData(Location s, Location b) { spawn = s; bed = b; }
    }

    private static class TeamImpl implements Team {
        final TeamColor color;
        final List<UUID> players = new CopyOnWriteArrayList<>();
        final Location spawn, bedLoc;
        volatile boolean bedAlive = true;
        volatile int kills, bedsBroken;
        final Map<Material, Integer> resources = new ConcurrentHashMap<>();

        TeamImpl(TeamColor color, TeamData d) {
            this.color = color;
            this.spawn = d != null ? d.spawn : null;
            this.bedLoc = d != null ? d.bed : null;
        }

        @Override public TeamColor color() { return color; }
        @Override public List<UUID> players() { return players; }
        @Override public boolean hasPlayer(UUID uuid) { return players.contains(uuid); }
        @Override public Location spawn() { return spawn; }
        @Override public Location bedLocation() { return bedLoc; }
        @Override public boolean bedAlive() { return bedAlive; }
        @Override public int aliveCount() { int n = 0; for (UUID u : players) if (Bukkit.getPlayer(u) != null) n++; return n; }
        @Override public boolean isEliminated() { return !bedAlive && aliveCount() == 0; }
        @Override public int kills() { return kills; }
        @Override public int bedsBroken() { return bedsBroken; }
        @Override public void addResource(Material mat, int amount) { resources.merge(mat, amount, Integer::sum); }
        @Override public int getResource(Material mat) { return resources.getOrDefault(mat, 0); }
    }

    private static class GameImpl implements Game {
        final ArenaImpl arena;
        volatile GameState state = GameState.WAITING;
        volatile int countdown = 0;
        volatile int timer = 0;
        final Map<TeamColor, TeamImpl> teams;
        final Map<UUID, TeamColor> playerTeam = new ConcurrentHashMap<>();
        final Set<Long> placed = ConcurrentHashMap.newKeySet();
        final Map<String, Integer> stats = new ConcurrentHashMap<>();
        final List<GeneratorTask> generators = new CopyOnWriteArrayList<>();
        final List<Consumer<Team>> endCbs = new CopyOnWriteArrayList<>();
        volatile TeamColor winner;

        GameImpl(ArenaImpl a, List<TeamColor> active) {
            this.arena = a;
            this.teams = new ConcurrentHashMap<>();
            for (TeamColor c : active) {
                Location s = a.spawns.get(c), b = a.beds.get(c);
                if (s != null || b != null) teams.put(c, new TeamImpl(c, new TeamData(s, b)));
            }
        }

        @Override public Arena arena() { return arena; }
        @Override public GameState state() { return state; }
        @Override public List<Team> aliveTeams() { List<Team> r = new ArrayList<>(); for (TeamImpl t : teams.values()) if (!t.isEliminated()) r.add(t); return r; }
        @Override public int timer() { return timer; }
        @Override public int countdown() { return countdown; }
        @Override public Team winner() { return winner == null ? null : teams.get(winner); }
        @Override public void onEnd(Consumer<Team> cb) { endCbs.add(cb); }

        void tick(JavaPlugin plugin) {
            if (state == GameState.COUNTDOWN) {
                if (countdown <= 0) {
                    state = GameState.PLAYING;
                } else {
                    if (countdown % 20 == 0) {}
                    countdown--;
                }
            }
            if (state == GameState.PLAYING) {
                timer++;
                for (GeneratorTask gt : generators) {
                    gt.tick++;
                    if (gt.tick >= gt.interval) {
                        gt.tick = 0;
                        TeamImpl t = teams.get(gt.color);
                        if (t != null && t.spawn != null && t.spawn.getWorld() != null) {
                            t.spawn.getWorld().dropItemNaturally(t.spawn, new ItemStack(gt.resource, gt.amount));
                        }
                    }
                }
                int alive = 0;
                TeamColor last = null;
                for (Map.Entry<TeamColor, TeamImpl> en : teams.entrySet()) {
                    if (!en.getValue().isEliminated()) { alive++; last = en.getKey(); }
                }
                if (alive <= 1) {
                    winner = last;
                    if (last != null) {
                        for (UUID u : teams.get(last).players) stats.merge(u+"_wins", 1, Integer::sum);
                    }
                    endInternal(winner);
                }
            }
        }

        void endInternal(TeamColor w) {
            if (state == GameState.ENDING) return;
            state = GameState.ENDING;
            winner = w;
            TeamImpl t = w == null ? null : teams.get(w);
            for (Consumer<Team> c : endCbs) c.accept(t);
        }
    }

    private static class GeneratorTask {
        final TeamColor color;
        final Material resource;
        final int amount, interval;
        int tick;
        GeneratorTask(TeamColor c, Material r, int a, int i, int t) { color = c; resource = r; amount = a; interval = i; tick = t; }
    }

    public static class ShopItem {
        public final Material icon;
        public final String name;
        public final int price;
        public final Material currency;
        public final List<ItemStack> rewards;
        public final Consumer<Player> onBuy;
        ShopItem(Material i, String n, int p, Material c, List<ItemStack> r, Consumer<Player> b) {
            icon = i; name = n; price = p; currency = c; rewards = r; onBuy = b;
        }
    }

    private static class GameEventImpl implements GameEvent {
        final Type type;
        final Game game;
        final Player player;
        final Object data;
        GameEventImpl(Type t, Game g, Player p, Object d) { type = t; game = g; player = p; data = d; }
        @Override public Type type() { return type; }
        @Override public Game game() { return game; }
        @Override public Player player() { return player; }
        @Override public Object data() { return data; }
    }
}