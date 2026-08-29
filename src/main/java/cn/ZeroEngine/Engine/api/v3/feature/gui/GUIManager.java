package cn.ZeroEngine.Engine.api.v3.feature.gui;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface GUIManager {

    ChestGUI create();

    ChestGUI create(String title, int rows);

    ChestGUI create(String title, int rows, boolean readonly);

    void register(SChestGUI gui);

    SChestGUI byCommand(String command);

    void closeAll();
}
