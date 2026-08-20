package cn.ZeroEngine.Engine.api.v1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class helpcom implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        InputStream is = getClass().getResourceAsStream("/help.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sender.sendMessage(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }
}

