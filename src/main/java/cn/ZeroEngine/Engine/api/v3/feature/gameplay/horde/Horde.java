package cn.ZeroEngine.Engine.api.v3.feature.gameplay.horde;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface Horde {

    enum GameState { WAITING, COUNTDOWN, PREPARING, WAVE_ACTIVE, WAVE_INTERVAL, BOSS_WAVE, ENDING }
    enum Difficulty { EASY, NORMAL, HARD, NIGHTMARE, APOCALYPSE }
    enum SpawnType { NORMAL, ELITE, BOSS, SWARM, SPECIAL }

    interface Wave {
        int number();
        Difficulty difficulty();
        int totalMobs();
        int spawned();
        int remaining();
        int killed();
        int intervalSeconds();
        boolean isBossWave();
        List<MobRule> mobRules();
        long durationTicks();
        long elapsedTicks();
    }

    interface MobRule {
        EntityType type();
        SpawnType spawnType();
        int weight();
        int minCount();
        int maxCount();
        double healthMul();
        double damageMul();
        double speedMul();
        List<String> effects();
        Map<EntityType, Double> equipmentChance();
    }

    interface Survivor {
        UUID uuid();
        String name();
        int kills();
        int deaths();
        int damageDealt();
        int damageTaken();
        int revived();
        boolean isAlive();
        boolean isDowned();
        int downTimerSeconds();
        int score();
        int waveReached();
    }

    interface Arena {
        String id();
        String name();
        String world();
        int minPlayers();
        int maxPlayers();
        List<Location> spawnPoints();
        Location lobbyLocation();
        Location spectatorLocation();
        List<Location> mobSpawnPoints();
        int borderRadius();
        Location borderCenter();
        Difficulty defaultDifficulty();
        int maxWaves();
        int prepareSeconds();
        int waveIntervalSeconds();
        boolean isInUse();
        boolean allowBuild();
    }

    interface Game {
        String id();
        Arena arena();
        GameState state();
        Difficulty difficulty();
        Wave currentWave();
        List<Wave> allWaves();
        List<Survivor> survivors();
        List<LivingEntity> aliveMobs();
        int aliveCount();
        int timerSeconds();
        int score();
        Survivor survivor(UUID uuid);
        boolean isInGame(Player p);
        void onEnd(Consumer<List<Survivor>> callback);
    }

    void registerArena(String id, String name, String world, int minPlayers, int maxPlayers,
                       List<Location> spawnPoints, Location lobbyLocation, Location spectatorLocation,
                       List<Location> mobSpawnPoints, int borderRadius, Location borderCenter,
                       Difficulty defaultDifficulty, int maxWaves, int prepareSeconds,
                       int waveIntervalSeconds, boolean allowBuild);

    void unregisterArena(String id);

    Arena getArena(String id);

    Collection<Arena> allArenas();

    Game createGame(String arenaId, Difficulty difficulty);

    Game getGame(String arenaId);

    Game gameOf(Player p);

    boolean join(Player p, String arenaId);

    boolean leave(Player p);

    boolean startCountdown(String arenaId, int seconds);

    void forceStart(String arenaId);

    void forceEnd(String arenaId);

    void addWaveMobRule(String arenaId, int waveNumber, MobRule rule);

    void removeWaveMobRule(String arenaId, int waveNumber, EntityType type);

    void setDifficultyMultiplier(Difficulty d, String key, double value);

    double getDifficultyMultiplier(Difficulty d, String key);

    void registerWaveReward(int wave, List<ItemStack> items, double moneyPerPlayer, int scorePerPlayer);

    void registerKillReward(EntityType type, int score, double money, List<ItemStack> drops);

    void revivePlayer(Player dead, Player reviver);

    void downPlayer(Player p, int timerSeconds);

    boolean isPlayerDowned(Player p);

    int spawnWaveMobs(Game g, Wave w);

    void clearArenaMobs(String arenaId);

    void registerElite(EntityType base, String eliteId, String displayName,
                       double healthMul, double damageMul, double speedMul,
                       List<String> effects, Map<String, Object> skills);

    void registerBoss(EntityType base, String bossId, String displayName,
                      double healthMul, double damageMul, double speedMul,
                      List<String> effects, Map<String, Object> skills,
                      List<ItemStack> drops, int scoreReward);

    void onEvent(Consumer<HordeEvent> listener);

    <T extends HordeEvent> void onEvent(Class<T> type, Consumer<T> listener);

    interface HordeEvent {
        enum Type { PLAYER_JOIN, PLAYER_LEAVE, COUNTDOWN_START, PREPARE_START,
            WAVE_START, WAVE_END, BOSS_WAVE_START, MOB_SPAWN, MOB_KILL,
            PLAYER_DOWN, PLAYER_REVIVE, PLAYER_DEATH, SURVIVOR_WIN,
            GAME_END, REWARD_GRANT, SCORE_CHANGE, ELITE_SPAWN, BOSS_SPAWN }
        Type type();
        Game game();
        Player player();
        Object data();
    }

    int getWavesSurvived(OfflinePlayer p, String arenaId);

    int getTotalKills(OfflinePlayer p, String arenaId);

    int getTotalDeaths(OfflinePlayer p, String arenaId);

    int getBestWave(OfflinePlayer p, String arenaId);

    int getBestScore(OfflinePlayer p, String arenaId);

    int getGamesPlayed(OfflinePlayer p, String arenaId);

    int getGamesWon(OfflinePlayer p, String arenaId);

    void resetStats(String arenaId, OfflinePlayer p);

    void setWorldTimeSpeed(Game g, int multiplier);

    void setBloodMoonChance(String arenaId, double chance);

    boolean isBloodMoon(Game g);
}