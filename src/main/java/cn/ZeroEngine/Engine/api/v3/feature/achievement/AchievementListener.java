package cn.ZeroEngine.Engine.api.v3.feature.achievement;

import cn.ZeroEngine.Engine.api.v3.SF;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AchievementListener implements Listener {

    private final AchievementManager manager;

    public AchievementListener(AchievementManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        Advancement adv = e.getAdvancement();
        if (adv == null) return;
        NamespacedKey key = adv.getKey();
        String ns = key.getNamespace();
        String id = key.getKey();
        String fullId = ns + ":" + id;

        for (SAchievement a : manager.all()) {
            if (a.fullId().equals(fullId)) {
                Player p = e.getPlayer();
                try {
                    a.onGrant(p);
                } catch (Throwable t) {
                    SF.sf().error("[Achievement] onGrant failed for " + fullId, t);
                }
                return;
            }
        }
    }
}
