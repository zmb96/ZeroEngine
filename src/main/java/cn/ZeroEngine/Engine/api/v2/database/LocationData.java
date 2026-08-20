package cn.ZeroEngine.Engine.api.v2.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LocationData {

    public final String world;
    public final double x, y, z;
    public final float yaw, pitch;

    public LocationData(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static LocationData of(Location loc) {
        return new LocationData(
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch()
        );
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    public String sqlValues() {
        return "'" + world + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch;
    }
}
