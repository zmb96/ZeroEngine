package cn.ZeroEngine.Engine.api.v3.feature.gameplay.bedwars;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface Bedwars {

    enum GameState { WAITING, COUNTDOWN, PLAYING, ENDING }

    enum TeamColor {
        RED("红", 0xFF5555, Material.RED_BED, Material.RED_WOOL),
        BLUE("蓝", 0x5555FF, Material.BLUE_BED, Material.BLUE_WOOL),
        GREEN("绿", 0x55FF55, Material.LIME_BED, Material.LIME_WOOL),
        YELLOW("黄", 0xFFFF55, Material.YELLOW_BED, Material.YELLOW_WOOL),
        AQUA("青", 0x55FFFF, Material.CYAN_BED, Material.CYAN_WOOL),
        WHITE("白", 0xFFFFFF, Material.WHITE_BED, Material.WHITE_WOOL),
        PINK("粉", 0xFF55FF, Material.PINK_BED, Material.PINK_WOOL),
        GRAY("灰", 0xAAAAAA, Material.GRAY_BED, Material.GRAY_WOOL);

        public final String name;
        public final int color;
        public final Material bed;
        public final Material wool;

        TeamColor(String name, int color, Material bed, Material wool) {
            this.name = name;
            this.color = color;
            this.bed = bed;
            this.wool = wool;
        }
    }

    interface Team {
        TeamColor color();
        List<UUID> players();
        boolean hasPlayer(UUID uuid);
        Location spawn();
        Location bedLocation();
        boolean bedAlive();
        int aliveCount();
        boolean isEliminated();
        int kills();
        int bedsBroken();
        void addResource(Material mat, int amount);
        int getResource(Material mat);
    }

    interface Arena {
        String id();
        String name();
        String world();
        int minPlayers();
        int maxPlayers();
        int maxPlayersPerTeam();
        Map<TeamColor, Team> teams();
        GameState state();
        boolean isInGame(Player p);
        Team teamOf(UUID uuid);
    }

    interface Game {
        Arena arena();
        GameState state();
        List<Team> aliveTeams();
        int timer();
        int countdown();
        Team winner();
        void onEnd(Consumer<Team> callback);
    }

    void registerArena(String id, String name, String world, int minPlayers, int maxPlayersPerTeam,
                       Map<TeamColor, Location> spawns, Map<TeamColor, Location> beds, Location waitingLobby, Location spectatorSpawn,
                       List<Map<String, Object>> generators, List<Map<String, Object>> shops);

    void unregisterArena(String id);

    Arena getArena(String id);

    Collection<Arena> allArenas();

    Game createGame(String arenaId, List<TeamColor> activeTeams);

    Game getGame(String arenaId);

    boolean join(Player p, String arenaId, TeamColor color);

    boolean leave(Player p);

    void startCountdown(String arenaId, int seconds);

    void forceStart(String arenaId);

    void forceEnd(String arenaId, TeamColor winnerOrNull);

    void breakBed(Player p, TeamColor color);

    boolean placeBlock(Player p, int x, int y, int z, Material type);

    boolean breakBlock(Player p, int x, int y, int z);

    boolean isProtected(String arenaId, int x, int y, int z);

    void setGameRule(String arenaId, String rule, Object value);

    void onEvent(Consumer<GameEvent> listener);

    void registerShopItem(Material icon, String name, int price, Material currency, List<ItemStack> rewards, Consumer<Player> onBuy);

    void dropGenerator(String arenaId, TeamColor color, Material resource, int amountPerTick, int intervalTicks);

    void setResourceDrop(Location loc, Material mat, int amount);

    interface GameEvent {
        enum Type { PLAYER_JOIN, PLAYER_LEAVE, COUNTDOWN_START, GAME_START, BED_BROKEN, PLAYER_DEATH, PLAYER_RESPAWN, TEAM_ELIMINATED, GAME_END, GENERATOR_TICK, BLOCK_PLACE, BLOCK_BREAK, SHOP_BUY }
        Type type();
        Game game();
        Player player();
        Object data();
    }

    int getKills(OfflinePlayer p, String arenaId);
    int getBedsBroken(OfflinePlayer p, String arenaId);
    int getDeaths(OfflinePlayer p, String arenaId);
    int getWins(OfflinePlayer p, String arenaId);
    void resetStats(String arenaId, OfflinePlayer p);
}