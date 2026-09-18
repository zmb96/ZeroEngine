package cn.ZeroEngine.Engine.api.v2.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class DatabaseManager {

    private static Database db;

    private DatabaseManager() {
    }

    public static boolean init(JavaPlugin plugin) {
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("database");
        if (cfg == null) {
            db = new SQLiteDatabase(plugin, "data.db");
        } else if (cfg.getBoolean("mysql.enabled", false)) {
            db = new MySQLDatabase(
                    cfg.getString("mysql.host", "localhost"),
                    cfg.getInt("mysql.port", 3306),
                    cfg.getString("mysql.database", "minecraft"),
                    cfg.getString("mysql.user", "root"),
                    cfg.getString("mysql.password", ""),
                    cfg.getString("mysql.prefix", "sf_")
            );
        } else {
            db = new SQLiteDatabase(plugin, cfg.getString("sqlite.file", "data.db"));
        }

        boolean ok = db.connect();
        if (ok) {
            createTables();
        }
        return ok;
    }

    public static void shutdown() {
        if (db != null) {
            db.disconnect();
            db = null;
        }
    }

    public static Database db() {
        if (db == null) {
            throw new IllegalStateException("Database not initialized. Call DatabaseManager.init(plugin) in onEnable().");
        }
        return db;
    }

    public static boolean ready() {
        return db != null && db.isConnected();
    }

    private static void createTables() {
        db.executeUpdate("CREATE TABLE IF NOT EXISTS homes (" +
                "uuid VARCHAR(36) NOT NULL," +
                "name VARCHAR(64) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "PRIMARY KEY (uuid, name)" +
                ")");

        db.executeUpdate("CREATE TABLE IF NOT EXISTS warps (" +
                "name VARCHAR(64) NOT NULL PRIMARY KEY," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL," +
                "created_by VARCHAR(36)," +
                "created_at BIGINT" +
                ")");

        db.executeUpdate("CREATE TABLE IF NOT EXISTS last_locations (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "world VARCHAR(64) NOT NULL," +
                "x DOUBLE NOT NULL," +
                "y DOUBLE NOT NULL," +
                "z DOUBLE NOT NULL," +
                "yaw FLOAT NOT NULL," +
                "pitch FLOAT NOT NULL" +
                ")");
    }
}
