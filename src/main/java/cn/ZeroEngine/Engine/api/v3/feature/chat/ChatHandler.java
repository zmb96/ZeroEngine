package cn.ZeroEngine.Engine.api.v3.feature.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface ChatHandler {

    int priority();

    void handle(ChatContext ctx);

    interface ChatContext {
        Player player();
        String rawMessage();
        Component formattedMessage();
        void formattedMessage(Component component);
        boolean consumed();
        void consume();
        boolean cancelled();
        void cancel();
        ChatManager.ChatChannel channel();
        void channel(ChatManager.ChatChannel channel);
    }
}
