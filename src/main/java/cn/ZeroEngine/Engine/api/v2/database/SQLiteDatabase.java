package cn.ZeroEngine.Engine.api.v2.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.function.Function;

public final class SQLiteDatabase implements Database {

    private final JavaPlugin plugin;
    private final String fileName;
    private Connection connection;

    public SQLiteDatabase(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    @Override
    public boolean connect() {
        try {
            File f = new File(plugin.getDataFolder(), fileName);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA journal_mode=WAL");
                s.execute("PRAGMA synchronous=NORMAL");
                s.execute("PRAGMA foreign_keys=ON");
            }
            return true;
        } catch (Exception e) {
            cn.ZeroEngine.Engine.api.v2.SF sf = cn.ZeroEngine.Engine.api.v2.SF.sf();
            sf.error("SQLite connect failed", e);
            return false;
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            cn.ZeroEngine.Engine.api.v2.SF sf = cn.ZeroEngine.Engine.api.v2.SF.sf();
            sf.error("SQLite disconnect failed", e);
        } finally {
            connection = null;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement ps = stmt(connection, sql, params)) {
            return ps.executeUpdate();
        } catch (Exception e) {
            cn.ZeroEngine.Engine.api.v2.SF sf = cn.ZeroEngine.Engine.api.v2.SF.sf();
            sf.error("SQL update failed: " + sql, e);
            return -1;
        }
    }

    @Override
    public <T> T executeQuery(String sql, Function<ResultSet, T> mapper, Object... params) {
        try (PreparedStatement ps = stmt(connection, sql, params);
             ResultSet rs = ps.executeQuery()) {
            return mapper.apply(rs);
        } catch (Exception e) {
            cn.ZeroEngine.Engine.api.v2.SF sf = cn.ZeroEngine.Engine.api.v2.SF.sf();
            sf.error("SQL query failed: " + sql, e);
            return null;
        }
    }
}
