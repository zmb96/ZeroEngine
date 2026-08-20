package cn.ZeroEngine.Engine.api.v2.feature.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import cn.ZeroEngine.Engine.api.v2.SF;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SFText {

    private SFText() {}

    public static Component text(String content) {
        return Component.text(content);
    }

    public static Component text(String content, NamedTextColor color) {
        return Component.text(content, color);
    }

    public static Component text(String content, String hexColor) {
        return Component.text(content, TextColor.fromHexString(hexColor));
    }

    public static Component text(String content, NamedTextColor color, TextDecoration... decorations) {
        Component c = Component.text(content, color);
        for (TextDecoration d : decorations) c = c.decorate(d);
        return c;
    }

    public static Component item(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return Component.text("[空手]", NamedTextColor.GRAY);
        }
        Component itemName = Component.translatable(item.getType().translationKey());
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().displayName();
            if (itemName == null) itemName = Component.translatable(item.getType().translationKey());
        }
        return Component.empty()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(itemName)
                .append(Component.text("]", NamedTextColor.GRAY))
                .hoverEvent(item.asHoverEvent());
    }

    public static Component item(ItemStack item, String displayOverride) {
        if (item == null || item.getType() == Material.AIR) {
            return Component.text("[空手]", NamedTextColor.GRAY);
        }
        Component display = displayOverride != null
                ? Component.text(displayOverride)
                : Component.translatable(item.getType().translationKey());
        return Component.empty()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(display)
                .append(Component.text("]", NamedTextColor.GRAY))
                .hoverEvent(item.asHoverEvent());
    }

    public static Component skull(OfflinePlayer player) {
        if (player == null) return Component.text("[未知玩家]", NamedTextColor.GRAY);
        return skull(player.getUniqueId(), player.getName());
    }

    public static Component skull(UUID playerId, String name) {
        SF sf = SF.sf();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(sf.bukkit().getOfflinePlayer(playerId));
        if (name != null) meta.setDisplayName("§e" + name + " §7的头颅");
        skull.setItemMeta(meta);
        return Component.empty()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text(name != null ? name : "玩家头颅", NamedTextColor.YELLOW))
                .append(Component.text("]", NamedTextColor.GRAY))
                .hoverEvent(skull.asHoverEvent());
    }

    public static Component skullByTexture(String base64Texture, String displayName) {
        SF sf = SF.sf();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (base64Texture != null && !base64Texture.isEmpty()) {
            meta.setPlayerProfile(sf.bukkit().createProfile(base64Texture));
        }
        if (displayName != null) meta.setDisplayName("§e" + displayName);
        skull.setItemMeta(meta);
        return Component.empty()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text(displayName != null ? displayName : "自定义头颅", NamedTextColor.YELLOW))
                .append(Component.text("]", NamedTextColor.GRAY))
                .hoverEvent(skull.asHoverEvent());
    }

    public static Component url(String text, String url) {
        return Component.text(text)
                .clickEvent(ClickEvent.openUrl(url))
                .decorate(TextDecoration.UNDERLINED)
                .color(NamedTextColor.AQUA);
    }

    public static Component command(String text, String command) {
        return Component.text(text, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("点击执行: " + command, NamedTextColor.GRAY)));
    }

    public static Component suggest(String text, String command) {
        return Component.text(text, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("点击填入: " + command, NamedTextColor.GRAY)));
    }

    public static Component tooltip(String text, String hoverText) {
        return Component.text(text)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.GRAY)));
    }

    public static Component tooltip(String text, Component hoverComponent) {
        return Component.text(text).hoverEvent(HoverEvent.showText(hoverComponent));
    }

    public static Component copy(String text, String copyContent) {
        return Component.text(text, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.copyToClipboard(copyContent))
                .hoverEvent(HoverEvent.showText(Component.text("点击复制", NamedTextColor.GRAY)));
    }

    public static Component newline() {
        return Component.newline();
    }

    public static Component separator() {
        return Component.text("───────────────", NamedTextColor.DARK_GRAY);
    }

    public static Component empty() {
        return Component.empty();
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Component> parts = new ArrayList<>();

        public Builder append(Component component) {
            parts.add(component);
            return this;
        }

        public Builder append(String text) {
            parts.add(Component.text(text));
            return this;
        }

        public Builder append(String text, NamedTextColor color) {
            parts.add(Component.text(text, color));
            return this;
        }

        public Builder appendItem(ItemStack item) {
            parts.add(SFText.item(item));
            return this;
        }

        public Builder appendItem(ItemStack item, String displayOverride) {
            parts.add(SFText.item(item, displayOverride));
            return this;
        }

        public Builder appendSkull(OfflinePlayer player) {
            parts.add(SFText.skull(player));
            return this;
        }

        public Builder appendSkull(UUID playerId, String name) {
            parts.add(SFText.skull(playerId, name));
            return this;
        }

        public Builder appendUrl(String text, String url) {
            parts.add(SFText.url(text, url));
            return this;
        }

        public Builder appendCommand(String text, String command) {
            parts.add(SFText.command(text, command));
            return this;
        }

        public Builder appendSuggest(String text, String command) {
            parts.add(SFText.suggest(text, command));
            return this;
        }

        public Builder appendTooltip(String text, String hover) {
            parts.add(SFText.tooltip(text, hover));
            return this;
        }

        public Builder appendCopy(String text, String copyContent) {
            parts.add(SFText.copy(text, copyContent));
            return this;
        }

        public Builder newLine() {
            parts.add(Component.newline());
            return this;
        }

        public Builder separator() {
            parts.add(SFText.separator());
            return this;
        }

        public Builder space() {
            parts.add(Component.space());
            return this;
        }

        public Component build() {
            if (parts.isEmpty()) return Component.empty();
            Component result = parts.get(0);
            for (int i = 1; i < parts.size(); i++) {
                result = result.append(parts.get(i));
            }
            return result;
        }

        public List<Component> buildLines() {
            return new ArrayList<>(parts);
        }
    }

}
