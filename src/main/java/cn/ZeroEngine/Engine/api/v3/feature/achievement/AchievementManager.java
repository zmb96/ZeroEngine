package cn.ZeroEngine.Engine.api.v3.feature.achievement;

import cn.ZeroEngine.Engine.api.v3.SF;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AchievementManager {

    private final Map<String, SAchievement> registry = new HashMap<>();

    public AchievementManager register(SAchievement a) {
        String fullId = a.fullId();
        if (registry.containsKey(fullId)) {
            throw new IllegalStateException("Achievement already registered: " + fullId);
        }
        registry.put(fullId, a);
        SF.sf().info("[Achievement] Registered: " + fullId + " (" + a.title() + ")");
        return this;
    }

    public boolean registerIfAbsent(SAchievement a) {
        if (registry.containsKey(a.fullId())) return false;
        try {
            register(a);
            return true;
        } catch (IllegalStateException ignore) {
            return false;
        }
    }

    public void unregisterAll() {
        registry.clear();
    }

    public SAchievement get(String fullId) {
        return registry.get(fullId);
    }

    public Collection<SAchievement> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public boolean grant(Player p, String fullId) {
        SAchievement a = registry.get(fullId);
        if (a == null) {
            SF.sf().warn("[Achievement] Not found: " + fullId);
            return false;
        }
        NamespacedKey key = a.namespacedKey();
        Advancement adv = Bukkit.getAdvancement(key);
        if (adv == null) {
            SF.sf().warn("[Achievement] Advancement not loaded (datapack may need restart): " + key);
            return false;
        }
        AdvancementProgress progress = p.getAdvancementProgress(adv);
        if (progress.isDone()) {
            return false;
        }
        if (!progress.getAwardedCriteria().contains(a.criteriaName())) {
            progress.awardCriteria(a.criteriaName());
        }
        return true;
    }

    public boolean revoke(Player p, String fullId) {
        SAchievement a = registry.get(fullId);
        if (a == null) return false;
        Advancement adv = Bukkit.getAdvancement(a.namespacedKey());
        if (adv == null) return false;
        AdvancementProgress progress = p.getAdvancementProgress(adv);
        for (String crit : progress.getAwardedCriteria()) {
            progress.revokeCriteria(crit);
        }
        return true;
    }

    public boolean isGranted(Player p, String fullId) {
        SAchievement a = registry.get(fullId);
        if (a == null) return false;
        Advancement adv = Bukkit.getAdvancement(a.namespacedKey());
        if (adv == null) return false;
        return p.getAdvancementProgress(adv).isDone();
    }

    public void generateDataPack() {
        AchievementGenerator.generate(registry.values());
    }

    public void shutdown() {
        registry.clear();
    }
}
