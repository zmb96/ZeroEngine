package cn.ZeroEngine.Engine.api.v3.feature.world;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import cn.ZeroEngine.Engine.api.v3.SF;

import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private final Map<String, WorldPreset> presets = new HashMap<>();

    public static class WorldPreset {
        public final String name;
        public final long time;
        public final boolean storm;
        public final boolean thunder;
        public final Difficulty difficulty;
        public final boolean pvp;
        public final boolean fireSpread;
        public final boolean mobSpawning;

        public WorldPreset(String name, long time, boolean storm, boolean thunder,
                           Difficulty difficulty, boolean pvp, boolean fireSpread, boolean mobSpawning) {
            this.name = name;
            this.time = time;
            this.storm = storm;
            this.thunder = thunder;
            this.difficulty = difficulty;
            this.pvp = pvp;
            this.fireSpread = fireSpread;
            this.mobSpawning = mobSpawning;
        }
    }

    public void setTime(World world, long time) {
        world.setTime(time);
    }

    public void setDay(World world) {
        world.setTime(1000);
    }

    public void setNight(World world) {
        world.setTime(13000);
    }

    public void setNoon(World world) {
        world.setTime(6000);
    }

    public void setMidnight(World world) {
        world.setTime(18000);
    }

    public void lockTime(World world, long time) {
        world.setTime(time);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
    }

    public void unlockTime(World world) {
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true);
    }

    public void setStorm(World world, boolean storm) {
        world.setStorm(storm);
        if (!storm) world.setThundering(false);
    }

    public void setThunder(World world, boolean thunder) {
        world.setThundering(thunder);
        if (thunder) world.setStorm(true);
    }

    public void setDifficulty(World world, Difficulty difficulty) {
        world.setDifficulty(difficulty);
    }

    public void setPvp(World world, boolean pvp) {
        world.setPVP(pvp);
    }

    public void setBorder(World world, double size) {
        WorldBorder border = world.getWorldBorder();
        border.setSize(size);
    }

    public void setBorder(World world, double size, long seconds) {
        WorldBorder border = world.getWorldBorder();
        border.setSize(size, seconds);
    }

    public void setBorderCenter(World world, double x, double z) {
        world.getWorldBorder().setCenter(x, z);
    }

    public void resetBorder(World world) {
        WorldBorder border = world.getWorldBorder();
        border.reset();
    }

    public void setMobSpawning(World world, boolean spawn) {
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, spawn);
    }

    public void setFireSpread(World world, boolean spread) {
        world.setGameRule(org.bukkit.GameRule.DO_FIRE_TICK, spread);
    }

    public void savePreset(String name, World world) {
        SF sf = SF.sf();
        presets.put(name.toLowerCase(), new WorldPreset(
                name,
                world.getTime(),
                world.hasStorm(),
                world.isThundering(),
                world.getDifficulty(),
                world.getPVP(),
                world.getGameRuleValue(org.bukkit.GameRule.DO_FIRE_TICK),
                world.getGameRuleValue(org.bukkit.GameRule.DO_MOB_SPAWNING)
        ));
        sf.info("[World] Preset saved: " + name);
    }

    public boolean applyPreset(String name, World world) {
        WorldPreset p = presets.get(name.toLowerCase());
        if (p == null) return false;
        world.setTime(p.time);
        world.setStorm(p.storm);
        world.setThundering(p.thunder);
        world.setDifficulty(p.difficulty);
        world.setPVP(p.pvp);
        world.setGameRule(org.bukkit.GameRule.DO_FIRE_TICK, p.fireSpread);
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, p.mobSpawning);
        return true;
    }

    public Map<String, WorldPreset> presets() {
        return presets;
    }
}
