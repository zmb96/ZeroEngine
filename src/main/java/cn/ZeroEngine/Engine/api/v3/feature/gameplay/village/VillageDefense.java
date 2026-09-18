package cn.ZeroEngine.Engine.api.v3.feature.gameplay.village;

import org.bukkit.Location;
import org.bukkit.Material;
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

public interface VillageDefense {

    enum GameState { WAITING, COUNTDOWN, BUILD_PHASE, WAVE_ACTIVE, WAVE_INTERVAL, ENDING }
    enum BuildingType { CORE, TOWER_ARROW, TOWER_MAGIC, TOWER_CANNON, WALL, GATE,
        GOLD_MINE, LUMBER_CAMP, BARRACKS, BLACKSMITH, WELL, FARM, VILLAGER_HOUSE }
    enum UnitType { VILLAGER, GUARD, ARCHER, KNIGHT, MAGE, HEALER, WORKER }
    enum EnemyType { RAIDER, ARCHER, GRUNT, BRUTE, SHAMAN, BOSS_WARCHIEF, BOSS_BEHEMOTH }

    interface Building {
        String id();
        BuildingType type();
        Location location();
        int level();
        int maxLevel();
        double health();
        double maxHealth();
        boolean isAlive();
        int attackRange();
        int attackDamage();
        double attackSpeedTicks();
        int resourcePerTick();
        void upgrade();
        void repair(double amount);
        void setEnabled(boolean enabled);
        boolean isEnabled();
    }

    interface Unit {
        UUID uuid();
        UnitType type();
        String name();
        Location location();
        Location home();
        Building assignedBuilding();
        double health();
        double maxHealth();
        int attackDamage();
        int armor();
        double speed();
        boolean isAlive();
        void attack(LivingEntity target);
        void moveTo(Location loc);
        void heal(double amount);
    }

    interface Wave {
        int number();
        int totalEnemies();
        int spawned();
        int killed();
        int remaining();
        List<EnemySpawn> spawns();
        long durationTicks();
        long elapsedTicks();
    }

    interface EnemySpawn {
        EnemyType type();
        int count();
        int intervalTicks();
        double healthMul();
        double damageMul();
        double speedMul();
    }

    interface Defender {
        UUID uuid();
        String name();
        int gold();
        int wood();
        int stone();
        int iron();
        int kills();
        int deaths();
        int score();
        List<Building> buildings();
        List<Unit> units();
        void addResource(String type, int amount);
        boolean spendGold(int amount);
        boolean spendResources(Map<String, Integer> costs);
    }

    interface Arena {
        String id();
        String name();
        String world();
        int minPlayers();
        int maxPlayers();
        Location lobbyLocation();
        Location spectatorLocation();
        Location coreSpawn();
        List<Location> playerSpawns();
        List<Location> enemySpawnPoints();
        int mapRadius();
        int maxWaves();
        int buildPhaseSeconds();
        int waveIntervalSeconds();
        double coreMaxHealth();
        boolean allowBuild();
        boolean isInUse();
    }

    interface Game {
        String id();
        Arena arena();
        GameState state();
        Wave currentWave();
        List<Wave> allWaves();
        Building coreBuilding();
        double coreHealth();
        double coreMaxHealth();
        List<Building> allBuildings();
        List<Unit> allUnits();
        List<LivingEntity> aliveEnemies();
        List<Defender> defenders();
        Defender defender(UUID uuid);
        boolean isInGame(Player p);
        int timerSeconds();
        int score();
        void onEnd(Consumer<GameResult> callback);
    }

    interface GameResult {
        boolean victory();
        int waveReached();
        int totalScore();
        List<Defender> participants();
        Map<UUID, Integer> playerScores();
    }

    void registerArena(String id, String name, String world, int minPlayers, int maxPlayers,
                       Location lobbyLocation, Location spectatorLocation, Location coreSpawn,
                       List<Location> playerSpawns, List<Location> enemySpawnPoints,
                       int mapRadius, int maxWaves, int buildPhaseSeconds,
                       int waveIntervalSeconds, double coreMaxHealth, boolean allowBuild);

    void unregisterArena(String id);

    Arena getArena(String id);

    Collection<Arena> allArenas();

    Game createGame(String arenaId);

    Game getGame(String arenaId);

    Game gameOf(Player p);

    boolean join(Player p, String arenaId);

    boolean leave(Player p);

    boolean startCountdown(String arenaId, int seconds);

    void forceStart(String arenaId);

    void forceEnd(String arenaId, boolean victory);

    Building build(Player p, BuildingType type, Location loc);

    boolean demolish(Player p, Building building);

    boolean upgradeBuilding(Player p, Building building);

    boolean repairBuilding(Player p, Building building, double amount);

    Unit spawnUnit(Game g, UnitType type, Location spawn, Building home);

    boolean removeUnit(Unit unit);

    void setBuildingCost(BuildingType type, Map<String, Integer> cost);

    Map<String, Integer> getBuildingCost(BuildingType type, int level);

    void setBuildingStats(BuildingType type, int level, Map<String, Object> stats);

    Map<String, Object> getBuildingStats(BuildingType type, int level);

    void setUnitCost(UnitType type, Map<String, Integer> cost);

    Map<String, Integer> getUnitCost(UnitType type);

    void setUnitStats(UnitType type, Map<String, Object> stats);

    Map<String, Object> getUnitStats(UnitType type);

    void setEnemyStats(EnemyType type, Map<String, Object> stats);

    Map<String, Object> getEnemyStats(EnemyType type);

    void addWaveSpawn(String arenaId, int waveNumber, EnemySpawn spawn);

    void removeWaveSpawns(String arenaId, int waveNumber);

    void grantResource(Player p, String type, int amount);

    boolean spendResource(Player p, String type, int amount);

    int getResource(Player p, String type);

    List<ItemStack> getBuildingDrops(BuildingType type, int level);

    void registerWaveReward(int wave, Map<String, Integer> resourcesPerPlayer, int scorePerPlayer);

    void registerKillReward(EnemyType type, int score, int gold, List<ItemStack> drops);

    void healAllBuildings(String arenaId, double percent);

    void damageCore(String arenaId, double damage, LivingEntity source);

    List<LivingEntity> findEnemiesNear(Location center, double radius);

    List<Building> findBuildingsNear(Location center, double radius, BuildingType type);

    void onEvent(Consumer<VillageEvent> listener);

    <T extends VillageEvent> void onEvent(Class<T> type, Consumer<T> listener);

    interface VillageEvent {
        enum Type { PLAYER_JOIN, PLAYER_LEAVE, COUNTDOWN_START, BUILD_START, WAVE_START, WAVE_END,
            BUILDING_BUILT, BUILDING_UPGRADED, BUILDING_DEMOLISHED, BUILDING_DAMAGED, BUILDING_DESTROYED,
            UNIT_SPAWNED, UNIT_KILLED, ENEMY_SPAWN, ENEMY_KILL, CORE_DAMAGED, CORE_DESTROYED,
            RESOURCE_GAIN, RESOURCE_SPEND, GAME_END, VICTORY, DEFEAT, REWARD_GRANT, SCORE_CHANGE }
        Type type();
        Game game();
        Player player();
        Object data();
    }

    int getWavesSurvived(OfflinePlayer p, String arenaId);

    int getBestWave(OfflinePlayer p, String arenaId);

    int getBestScore(OfflinePlayer p, String arenaId);

    int getBuildingsBuilt(OfflinePlayer p, String arenaId);

    int getUnitsSpawned(OfflinePlayer p, String arenaId);

    int getGamesPlayed(OfflinePlayer p, String arenaId);

    int getGamesWon(OfflinePlayer p, String arenaId);

    int getTotalKills(OfflinePlayer p, String arenaId);

    void resetStats(String arenaId, OfflinePlayer p);

    void setBuildLimitPerPlayer(String arenaId, BuildingType type, int max);

    int getBuildLimitPerPlayer(String arenaId, BuildingType type);
}