package cn.ZeroEngine.Engine.api.v2.feature.perf;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformanceCommand implements CommandExecutor, TabCompleter {

    private final PerformanceManager manager;

    public PerformanceCommand(PerformanceManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(manager.getStatusReport());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status", "info" -> sender.sendMessage(manager.getStatusReport());
            case "gc" -> {
                manager.manualGc();
                sender.sendMessage("§a[Perf] GC triggered.");
            }
            case "chunks" -> {
                sender.sendMessage("§e[Perf] Cleaning chunks...");
                manager.manualChunkCleanup();
            }
            case "entities" -> {
                sender.sendMessage("§e[Perf] Cleaning entities...");
                manager.manualEntityCleanup();
            }
            case "toggle" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /sfperf toggle <memory|chunks|entities|throttle>");
                    return true;
                }
                boolean result = switch (args[1].toLowerCase()) {
                    case "memory" -> manager.toggleMemoryMonitor();
                    case "chunks" -> manager.toggleChunkManager();
                    case "entities" -> manager.toggleEntityCleaner();
                    case "throttle" -> manager.toggleAutoThrottle();
                    default -> {
                        sender.sendMessage("§cUnknown feature: " + args[1]);
                        yield false;
                    }
                };
                sender.sendMessage("§a[Perf] " + args[1] + ": " + (result ? "ON" : "OFF"));
            }
            case "tps" -> {
                sender.sendMessage(String.format("§eTPS: §a%.2f§7/20 (1m) §a%.2f§7/20 (5m) §7| §eMSPT: §a%.2f§7ms",
                        manager.getLastTps1m(), manager.getLastTps5m(), manager.getLastMspt()));
            }
            case "mem" -> {
                long used = manager.getLastUsedMemory();
                long max = manager.getLastMaxMemory();
                sender.sendMessage(String.format("§eMemory: §a%d§7/§a%d §7MB §a(%.1f%%)",
                        used / 1048576, max / 1048576, max > 0 ? (double) used / max * 100 : 0));
            }
            case "help" -> sendHelp(sender);
            default -> sender.sendMessage("§cUnknown subcommand. Use /sfperf help");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6===== SF Performance Help =====");
        sender.sendMessage("§e/sfperf §7- Show full status report");
        sender.sendMessage("§e/sfperf status §7- Same as above");
        sender.sendMessage("§e/sfperf tps §7- Show TPS and MSPT");
        sender.sendMessage("§e/sfperf mem §7- Show memory usage");
        sender.sendMessage("§e/sfperf gc §7- Trigger garbage collection");
        sender.sendMessage("§e/sfperf chunks §7- Clean idle chunks now");
        sender.sendMessage("§e/sfperf entities §7- Clean excess entities now");
        sender.sendMessage("§e/sfperf toggle <feature> §7- Toggle feature on/off");
        sender.sendMessage("§7  Features: §fmemory, chunks, entities, throttle");
        sender.sendMessage("§e/sfperf help §7- Show this help");
        sender.sendMessage("§6===============================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("status", "tps", "mem", "gc", "chunks", "entities", "toggle", "help"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return filter(Arrays.asList("memory", "chunks", "entities", "throttle"), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : options) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
