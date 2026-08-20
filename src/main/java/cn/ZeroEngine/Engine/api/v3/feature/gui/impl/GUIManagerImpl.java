package cn.ZeroEngine.Engine.api.v3.feature.gui.impl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import cn.ZeroEngine.Engine.api.v3.feature.gui.ChestGUI;
import cn.ZeroEngine.Engine.api.v3.feature.gui.GUIManager;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GUIManagerImpl implements GUIManager {

    private final Plugin plugin;
    private final PluginManager pm;
    private final Set<ChestGUIImpl> activeGUIs = new CopyOnWriteArraySet<>();

    public GUIManagerImpl(Plugin plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }

    @Override
    public ChestGUI create() {
        return create("GUI", 3, true);
    }

    @Override
    public ChestGUI create(String title, int rows) {
        return create(title, rows, true);
    }

    @Override
    public ChestGUI create(String title, int rows, boolean readonly) {
        ChestGUIImpl gui = new ChestGUIImpl(plugin);
        gui.title(title);
        gui.rows(rows);
        gui.readonly(readonly);
        pm.registerEvents(gui, plugin);
        activeGUIs.add(gui);
        return gui;
    }

    @Override
    public void closeAll() {
        for (ChestGUIImpl gui : activeGUIs) {
            try { gui.unregister(); } catch (Exception ignored) {}
        }
        activeGUIs.clear();
    }
}
