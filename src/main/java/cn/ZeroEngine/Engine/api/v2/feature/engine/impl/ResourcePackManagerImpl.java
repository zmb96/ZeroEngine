package cn.ZeroEngine.Engine.api.v2.feature.engine.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v2.feature.engine.ResourcePackManager;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackManagerImpl implements ResourcePackManager {

    private final JavaPlugin plugin;
    private final Map<String, ResourcePack> packs = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> customModelData = new ConcurrentHashMap<>();
    private final Map<String, MusicEntry> music = new ConcurrentHashMap<>();
    private ResourcePack defaultPack;

    private record MusicEntry(String soundName, int durationTicks) {}

    private record ResourcePackImpl(String name, String url, byte[] hash, boolean forced, String promptMessage) implements ResourcePack {
        @Override public String name() { return name; }
        @Override public String url() { return url; }
        @Override public byte[] hash() { return hash; }
        @Override public boolean forced() { return forced; }
        @Override public String promptMessage() { return promptMessage; }
    }

    public ResourcePackManagerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ResourcePack create(String name, String url, byte[] hash, boolean forced, String promptMessage) {
        return new ResourcePackImpl(name, url, hash, forced, promptMessage);
    }

    @Override
    public void register(ResourcePack pack) { packs.put(pack.name(), pack); }

    @Override
    public void unregister(String name) { packs.remove(name); }

    @Override
    public ResourcePack get(String name) { return packs.get(name); }

    @Override
    public List<ResourcePack> all() { return new ArrayList<>(packs.values()); }

    @Override
    public void send(Player player, String name) {
        ResourcePack pack = packs.get(name);
        if (pack != null) send(player, pack);
    }

    @Override
    public void send(Player player, ResourcePack pack) {
        if (pack.promptMessage() != null && !pack.promptMessage().isEmpty()) {
            player.sendMessage(net.kyori.adventure.text.Component.text(pack.promptMessage()));
        }
        if (pack.hash() != null && pack.hash().length > 0) {
            player.setResourcePack(pack.url(), pack.hash());
        } else {
            player.setResourcePack(pack.url());
        }
    }

    @Override
    public void sendAll(Player player) {
        sendAll(player, null);
    }

    @Override
    public void sendAll(Player player, Runnable onComplete) {
        List<ResourcePack> list = new ArrayList<>(packs.values());
        if (list.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        sendNext(player, list, 0, onComplete);
    }

    private void sendNext(Player player, List<ResourcePack> list, int index, Runnable onComplete) {
        if (index >= list.size()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        send(player, list.get(index));
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendNext(player, list, index + 1, onComplete), 20L);
    }

    @Override
    public void setCustomModelData(int itemId, int modelData, String texturePath) {
        customModelData.put(itemId, modelData);
    }

    @Override
    public int getCustomModelData(int itemId) {
        return customModelData.getOrDefault(itemId, -1);
    }

    @Override
    public void registerMusic(String id, String soundName, int durationTicks) {
        music.put(id, new MusicEntry(soundName, durationTicks));
    }

    @Override
    public void playMusic(Player player, String id) {
        MusicEntry entry = music.get(id);
        if (entry != null) {
            player.playSound(player.getLocation(), entry.soundName, org.bukkit.SoundCategory.MUSIC, 1f, 1f);
        }
    }

    @Override
    public void stopMusic(Player player) {
        for (MusicEntry entry : music.values()) {
            player.stopSound(entry.soundName, org.bukkit.SoundCategory.MUSIC);
        }
    }

    @Override
    public void playMusicAll(String id) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            playMusic(p, id);
        }
    }

    @Override
    public void stopMusicAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            stopMusic(p);
        }
    }

    @Override
    public void setDefaultPack(ResourcePack pack) { defaultPack = pack; }

    @Override
    public ResourcePack getDefaultPack() { return defaultPack; }
}
