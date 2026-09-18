package cn.ZeroEngine.Engine.api.v3.feature.engine;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface ResourcePackManager {

    interface ResourcePack {
        String name();

        String url();

        byte[] hash();

        boolean forced();

        String promptMessage();
    }

    ResourcePack create(String name, String url, byte[] hash, boolean forced, String promptMessage);

    void register(ResourcePack pack);

    void unregister(String name);

    ResourcePack get(String name);

    List<ResourcePack> all();

    void send(Player player, String name);

    void send(Player player, ResourcePack pack);

    void sendAll(Player player);

    void sendAll(Player player, Runnable onComplete);

    void setCustomModelData(int itemId, int modelData, String texturePath);

    int getCustomModelData(int itemId);

    void registerMusic(String id, String soundName, int durationTicks);

    void playMusic(Player player, String id);

    void stopMusic(Player player);

    void playMusicAll(String id);

    void stopMusicAll();

    void setDefaultPack(ResourcePack pack);

    ResourcePack getDefaultPack();
}
