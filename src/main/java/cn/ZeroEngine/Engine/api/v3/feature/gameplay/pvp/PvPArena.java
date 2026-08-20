package cn.ZeroEngine.Engine.api.v3.feature.gameplay.pvp;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface PvPArena {

    enum GameState { WAITING, COUNTDOWN, FIGHTING, ENDING }
    enum Mode { DUEL_1V1, TEAM_2V2, TEAM_3V3, TEAM_5V5, FFA, BATTLE_ROYALE, RANKED, PARTY }
    enum MatchResult { WIN_A, WIN_B, DRAW, CANCELLED }

    interface Match {
        String id();
        Mode mode();
        GameState state();
        Arena arena();
        List<UUID> teamA();
        List<UUID> teamB();
        List<UUID> allFighters();
        int timeLeftSeconds();
        int round();
        int scoreA();
        int scoreB();
        boolean isInMatch(UUID p);
        Player teamOf(UUID p);
        UUID winner();
        void onEnd(Consumer<MatchResult> cb);
    }

    interface Kit {
        String id();
        String name();
        String permission();
        int price();
        List<ItemStack> armor();
        List<ItemStack> inventory();
        List<String> effects();
        double healthScale();
        double walkSpeed();
    }

    interface Rank {
        int tier();
        String name();
        String prefix();
        int requiredElo();
        int maxElo();
    }

    interface Arena {
        String id();
        String name();
        int maxPlayers();
        List<Location> spawnsA();
        List<Location> spawnsB();
        Location waitingLobby();
        Location spectatorSpawn();
        List<String> allowedModes();
        boolean isInUse();
        boolean allowBuild();
        boolean allowInteract();
        int resetAfterSeconds();
    }

    void registerArena(String id, String name, List<Location> spawnsA, List<Location> spawnsB,
                       Location waitingLobby, Location spectatorSpawn,
                       int maxPlayers, List<String> allowedModes,
                       boolean allowBuild, boolean allowInteract, int resetAfterSeconds);
    void unregisterArena(String id);
    Arena getArena(String id);
    Collection<Arena> allArenas();

    void registerKit(Kit kit);
    Kit getKit(String id);
    Collection<Kit> allKits();
    boolean giveKit(Player p, String kitId);
    boolean applyKitToTeam(Match m, List<UUID> team, String kitId);

    Match createMatch(String arenaId, Mode mode);
    Match getMatch(String arenaId);
    Match matchOf(Player p);

    boolean joinTeamA(Player p, String arenaId);
    boolean joinTeamB(Player p, String arenaId);
    boolean joinFFA(Player p, String arenaId);
    boolean leave(Player p);

    boolean setKit(Player p, String kitId);

    void startCountdown(String arenaId, int seconds);
    void forceStart(String arenaId);
    void forceEnd(String arenaId, MatchResult result);

    void addKill(Match m, Player killer, Player victim);
    void addDeath(Match m, Player victim);
    boolean isPvPAllowedInMatch(Match m);

    void setModeRule(Mode m, String rule, Object value);
    Object getModeRule(Mode m, String rule);

    int getElo(OfflinePlayer p);
    int getElo(OfflinePlayer p, Mode mode);
    void setElo(OfflinePlayer p, Mode mode, int elo);
    void addElo(OfflinePlayer p, Mode mode, int delta);
    Rank getRank(int elo);
    List<Rank> allRanks();

    int getWins(OfflinePlayer p, Mode mode);
    int getLosses(OfflinePlayer p, Mode mode);
    int getWinStreak(OfflinePlayer p, Mode mode);
    int getBestWinStreak(OfflinePlayer p, Mode mode);
    int getKills(OfflinePlayer p, Mode mode);
    int getDeaths(OfflinePlayer p, Mode mode);
    void resetStats(OfflinePlayer p);

    void onEvent(Consumer<MatchEvent> listener);
    <T extends MatchEvent> void onEvent(Class<T> type, Consumer<T> listener);

    interface MatchEvent {
        enum Type { JOIN, LEAVE, COUNTDOWN_START, START, KILL, DEATH, ROUND_END, MATCH_END, KIT_CHANGE, ELO_CHANGE }
        Type type();
        Match match();
        Player player();
        Object data();
    }

    boolean queuePlayer(Player p, Mode mode);
    boolean dequeuePlayer(Player p);
    Map<Mode, List<UUID>> queueSnapshot();
    void startAutoMatchmaking();
    void stopAutoMatchmaking();
    void setMatchmakerInterval(int ticks);

    void registerSpectator(Player p, Match m);
    void removeSpectator(Player p, Match m);
    List<Player> spectators(Match m);

    void damageMatchPlayer(Match m, Player attacker, Player victim, double damage, EntityType cause);
}