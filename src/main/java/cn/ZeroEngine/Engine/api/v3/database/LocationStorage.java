package cn.ZeroEngine.Engine.api.v3.database;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LocationStorage {

    private LocationStorage() {
    }

    public static boolean setHome(UUID uuid, String name, LocationData loc) {
        Database db = DatabaseManager.db();
        db.executeUpdate("DELETE FROM homes WHERE uuid=? AND name=?", uuid.toString(), name);
        return db.executeUpdate(
                "INSERT INTO homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?)",
                uuid.toString(), name, loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch
        ) > 0;
    }

    public static boolean delHome(UUID uuid, String name) {
        return DatabaseManager.db().executeUpdate(
                "DELETE FROM homes WHERE uuid=? AND name=?", uuid.toString(), name
        ) > 0;
    }

    public static LocationData getHome(UUID uuid, String name) {
        return DatabaseManager.db().executeQuery(
                "SELECT * FROM homes WHERE uuid=? AND name=?",
                rs -> {
                    try {
                        return rs.next() ? readLoc(rs) : null;
                    } catch (Exception e) {
                        cn.ZeroEngine.Engine.api.v3.SF sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
                        sf.error("getHome read failed", e);
                        return null;
                    }
                },
                uuid.toString(), name
        );
    }

    public static Map<String, LocationData> getHomes(UUID uuid) {
        return DatabaseManager.db().executeQuery(
                "SELECT * FROM homes WHERE uuid=?",
                rs -> {
                    Map<String, LocationData> map = new HashMap<>();
                    try {
                        while (rs.next()) {
                            map.put(rs.getString("name"), readLoc(rs));
                        }
                    } catch (Exception e) {
                        cn.ZeroEngine.Engine.api.v3.SF sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
                        sf.error("getHomes read failed", e);
                    }
                    return map;
                },
                uuid.toString()
        );
    }

    public static boolean setWarp(String name, LocationData loc, UUID createdBy) {
        Database db = DatabaseManager.db();
        db.executeUpdate("DELETE FROM warps WHERE name=?", name);
        return db.executeUpdate(
                "INSERT INTO warps (name, world, x, y, z, yaw, pitch, created_by, created_at) VALUES (?,?,?,?,?,?,?,?,?)",
                name, loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch,
                createdBy == null ? null : createdBy.toString(),
                System.currentTimeMillis()
        ) > 0;
    }

    public static boolean delWarp(String name) {
        return DatabaseManager.db().executeUpdate("DELETE FROM warps WHERE name=?", name) > 0;
    }

    public static LocationData getWarp(String name) {
        return DatabaseManager.db().executeQuery(
                "SELECT * FROM warps WHERE name=?",
                rs -> {
                    try {
                        return rs.next() ? readLoc(rs) : null;
                    } catch (Exception e) {
                        cn.ZeroEngine.Engine.api.v3.SF sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
                        sf.error("getWarp read failed", e);
                        return null;
                    }
                },
                name
        );
    }

    public static Map<String, LocationData> getWarps() {
        return DatabaseManager.db().executeQuery(
                "SELECT * FROM warps",
                rs -> {
                    Map<String, LocationData> map = new HashMap<>();
                    try {
                        while (rs.next()) {
                            map.put(rs.getString("name"), readLoc(rs));
                        }
                    } catch (Exception e) {
                        cn.ZeroEngine.Engine.api.v3.SF sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
                        sf.error("getWarps read failed", e);
                    }
                    return map;
                }
        );
    }

    public static boolean setLastLocation(UUID uuid, LocationData loc) {
        Database db = DatabaseManager.db();
        db.executeUpdate("DELETE FROM last_locations WHERE uuid=?", uuid.toString());
        return db.executeUpdate(
                "INSERT INTO last_locations (uuid, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?)",
                uuid.toString(), loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch
        ) > 0;
    }

    public static LocationData getLastLocation(UUID uuid) {
        return DatabaseManager.db().executeQuery(
                "SELECT * FROM last_locations WHERE uuid=?",
                rs -> {
                    try {
                        return rs.next() ? readLoc(rs) : null;
                    } catch (Exception e) {
                        cn.ZeroEngine.Engine.api.v3.SF sf = cn.ZeroEngine.Engine.api.v3.SF.sf();
                        sf.error("getLastLocation read failed", e);
                        return null;
                    }
                },
                uuid.toString()
        );
    }

    private static LocationData readLoc(ResultSet rs) throws Exception {
        return new LocationData(
                rs.getString("world"),
                rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                rs.getFloat("yaw"), rs.getFloat("pitch")
        );
    }

    public static List<String> homeNames(UUID uuid) {
        return new ArrayList<>(getHomes(uuid).keySet());
    }

    public static List<String> warpNames() {
        return new ArrayList<>(getWarps().keySet());
    }
}
