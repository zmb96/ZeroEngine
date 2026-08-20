package cn.ZeroEngine.Engine.api.v3.feature.gui;

public interface GUIManager {

    ChestGUI create();

    ChestGUI create(String title, int rows);

    ChestGUI create(String title, int rows, boolean readonly);

    void closeAll();
}
