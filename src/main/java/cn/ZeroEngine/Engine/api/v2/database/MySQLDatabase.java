package cn.ZeroEngine.Engine.api.v2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Function;

public final class MySQLDatabase implements Database {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final String tablePrefix;
    private Connection connection;

    public MySQLDatabase(String host, int port, String database, String user, String password, String tablePrefix) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    @Override
    public boolean connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&useUnicode=true&characterEncoding=UTF-8", host, port, database);
            connection = DriverManager.getConnection(url, user, password);
            return true;
        } catch (Exception e) {
            cn.ZeroEngine.Engine.api.v2.SF sf = cn.ZeroEngine.Engine.api.v2.SF.sf();
            sf.error("MySQL connect failed", e);
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
            sf.error("MySQL disconnect failed", e);
        } finally {
            connection = null;
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Connection connection() {
        return connection;
    }

    public String tablePrefix() {
        return tablePrefix;
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
