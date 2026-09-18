package cn.ZeroEngine.Engine.api.v2.feature.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionManager {

    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerGroups = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerExtraPerms = new ConcurrentHashMap<>();

    public static class Group {
        public final String name;
        public String prefix;
        public String suffix;
        public int weight;
        public final Set<String> permissions = new HashSet<>();
        public final Set<String> inherits = new HashSet<>();

        public Group(String name, String prefix, String suffix, int weight) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.weight = weight;
        }
    }

    public void registerGroup(Group group) {
        groups.put(group.name.toLowerCase(), group);
    }

    public Group getGroup(String name) {
        return groups.get(name.toLowerCase());
    }

    public Collection<Group> allGroups() {
        return groups.values();
    }

    public void setGroup(Player p, String groupName) {
        Group g = getGroup(groupName);
        if (g == null) return;
        playerGroups.put(p.getUniqueId(), g.name.toLowerCase());
        applyPermissions(p);
    }

    public Group getGroup(Player p) {
        String name = playerGroups.get(p.getUniqueId());
        if (name == null) return groups.get("default");
        Group g = groups.get(name);
        return g != null ? g : groups.get("default");
    }

    public String getGroupExact(Player p) {
        return playerGroups.getOrDefault(p.getUniqueId(), "default");
    }

    public String getPrefix(Player p) {
        Group g = getGroup(p);
        return g != null ? g.prefix : "";
    }

    public String getSuffix(Player p) {
        Group g = getGroup(p);
        return g != null ? g.suffix : "";
    }

    public void addPermission(Player p, String permission) {
        playerExtraPerms.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>()).add(permission.toLowerCase());
        applyPermissions(p);
    }

    public void removePermission(Player p, String permission) {
        Set<String> perms = playerExtraPerms.get(p.getUniqueId());
        if (perms != null) {
            perms.remove(permission.toLowerCase());
            applyPermissions(p);
        }
    }

    public Set<String> getEffectivePermissions(Player p) {
        Set<String> all = new HashSet<>();
        Group g = getGroup(p);
        if (g != null) {
            collectGroupPermissions(g, all, new HashSet<>());
        }
        Set<String> extra = playerExtraPerms.get(p.getUniqueId());
        if (extra != null) all.addAll(extra);
        return all;
    }

    private void collectGroupPermissions(Group g, Set<String> out, Set<String> visited) {
        if (!visited.add(g.name.toLowerCase())) return;
        out.addAll(g.permissions);
        for (String parent : g.inherits) {
            Group pg = groups.get(parent.toLowerCase());
            if (pg != null) collectGroupPermissions(pg, out, visited);
        }
    }

    public boolean has(Player p, String permission) {
        return getEffectivePermissions(p).contains(permission.toLowerCase());
    }

    public void applyPermissions(Player p) {
        SF sf = SF.sf();
        PermissionAttachment old = attachments.remove(p.getUniqueId());
        if (old != null) {
            try { p.removeAttachment(old); } catch (Throwable ignored) {}
        }
        PermissionAttachment attach = p.addAttachment(sf.plugin());
        Set<String> perms = getEffectivePermissions(p);
        for (String perm : perms) {
            boolean value = true;
            String actual = perm;
            if (perm.startsWith("-")) {
                value = false;
                actual = perm.substring(1);
            }
            attach.setPermission(actual, value);
        }
        attachments.put(p.getUniqueId(), attach);
        p.recalculatePermissions();
    }

    public void cleanup(Player p) {
        PermissionAttachment attach = attachments.remove(p.getUniqueId());
        if (attach != null) {
            try { p.removeAttachment(attach); } catch (Throwable ignored) {}
        }
        playerExtraPerms.remove(p.getUniqueId());
    }

    public void initDefaults() {
        registerGroup(new Group("default", "§7", "", 0));
        registerGroup(new Group("vip", "§a[VIP] ", "", 10));
        registerGroup(new Group("mod", "§b[MOD] ", "", 50));
        registerGroup(new Group("admin", "§c[ADMIN] ", "", 100));
        registerGroup(new Group("owner", "§4[OWNER] ", "", 200));

        Group vip = getGroup("vip");
        vip.permissions.add("sf.teleport.delay.bypass");
        vip.permissions.add("sf.tpa.delay.bypass");

        Group mod = getGroup("mod");
        mod.inherits.add("vip");
        mod.permissions.add("sf.admin.chat");
        mod.permissions.add("sf.moderator");

        Group admin = getGroup("admin");
        admin.inherits.add("mod");
        admin.permissions.add("sf.admin.*");
        admin.permissions.add("sf.admin.enchant");
        admin.permissions.add("sf.admin.item");
        admin.permissions.add("sf.admin.world");

        Group owner = getGroup("owner");
        owner.inherits.add("admin");
        owner.permissions.add("*");
    }
}
