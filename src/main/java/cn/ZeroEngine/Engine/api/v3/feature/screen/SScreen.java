package cn.ZeroEngine.Engine.api.v3.feature.screen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;

import java.util.Collections;
import java.util.List;

/**
 * 自定义屏幕基类 —— 基于 Paper 1.21.8 Dialog API。
 *
 * 玩家进服时处于 configuration phase（尚未进入世界），此时弹出 Dialog，
 * 用 CompletableFuture 阻塞直到玩家点击按钮或超时。accept() 放行，deny() 踢出。
 *
 * 用法：
 *   public class RulesScreen extends SScreen {
 *       @Override public String id() { return "server_rules"; }
 *       @Override public Component title() { return Component.text("服务器规则").color(NamedTextColor.GOLD); }
 *       @Override public List<DialogBody> body() {
 *           return List.of(DialogBody.plainMessage(Component.text("1. 禁止作弊\n2. 友好交流")));
 *       }
 *       @Override public DialogType type() {
 *           return DialogType.confirmation(
 *               button(Component.text("同意"), "agree"),
 *               button(Component.text("拒绝"), "deny")
 *           );
 *       }
 *       @Override public void onClick(ClickContext ctx) {
 *           if (ctx.action().equals("agree")) ctx.accept();
 *           else ctx.deny(Component.text("你拒绝了规则"));
 *       }
 *   }
 *
 *   sf.screens().register(new RulesScreen());
 */
public abstract class SScreen {

    private String namespace;
    protected Plugin plugin;

    public abstract String id();

    public abstract Component title();

    public List<DialogBody> body() { return Collections.emptyList(); }

    public List<DialogInput> inputs() { return Collections.emptyList(); }

    public boolean canCloseWithEscape() { return false; }

    public abstract DialogType type();

    public boolean shouldShow(PlayerConfigurationConnection conn) { return true; }

    public int timeoutSeconds() { return 60; }

    public int priority() { return 0; }

    public abstract void onClick(ClickContext ctx);

    public Dialog buildDialog() {
        DialogBase.Builder baseBuilder = DialogBase.builder(title())
                .canCloseWithEscape(canCloseWithEscape());
        if (!body().isEmpty()) baseBuilder.body(body());
        if (!inputs().isEmpty()) baseBuilder.inputs(inputs());
        return Dialog.create(b -> b.empty()
                .base(baseBuilder.build())
                .type(type()));
    }

    protected ActionButton button(net.kyori.adventure.text.Component label, net.kyori.adventure.text.Component tooltip, String action) {
        ActionButton.Builder builder = ActionButton.builder(label);
        if (tooltip != null) builder.tooltip(tooltip);
        return builder.action(DialogAction.customClick(keyFor(action), null)).build();
    }

    protected ActionButton button(net.kyori.adventure.text.Component label, String action) {
        return button(label, null, action);
    }

    public final Key keyFor(String action) {
        String ns = (namespace == null || namespace.isEmpty()) ? "sf" : namespace;
        return Key.key(ns, id() + "/" + action);
    }

    public final String actionOf(NamespacedKey key) {
        if (key == null) return null;
        String expected = id() + "/";
        String value = key.value();
        int idx = value.indexOf(expected);
        if (idx < 0) return null;
        return value.substring(idx + expected.length());
    }

    public final String actionOf(net.kyori.adventure.key.Key key) {
        if (key == null) return null;
        String expected = id() + "/";
        String value = key.value();
        int idx = value.indexOf(expected);
        if (idx < 0) return null;
        return value.substring(idx + expected.length());
    }

    void bind(String namespace, Plugin plugin) {
        this.namespace = namespace;
        this.plugin = plugin;
    }

    public String namespace() { return namespace; }
}
