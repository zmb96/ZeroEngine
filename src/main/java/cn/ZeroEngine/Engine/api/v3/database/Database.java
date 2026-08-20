package cn.ZeroEngine.Engine.api.v3.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Function;

public interface Database {

    boolean connect();

    void disconnect();

    boolean isConnected();

    Connection connection();

    int executeUpdate(String sql, Object... params);

    <T> T executeQuery(String sql, Function<ResultSet, T> mapper, Object... params);

    default PreparedStatement stmt(Connection c, String sql, Object... params) throws Exception {
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}
