package cn.ZeroEngine.Engine.api.v3.feature.achievement;

import cn.ZeroEngine.Engine.api.v3.SF;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class AchievementGenerator {

    private AchievementGenerator() {
    }

    public static void generate(Collection<SAchievement> achievements) {
        if (achievements.isEmpty()) {
            SF.sf().info("[Achievement] No achievements registered, skipping datapack generation");
            return;
        }

        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (world == null) {
            SF.sf().warn("[Achievement] No world found, cannot generate datapack");
            return;
        }

        File worldDir = world.getWorldFolder();
        File datapackDir = new File(worldDir, "datapacks" + File.separator + "sf_advancements");

        if (!datapackDir.exists()) {
            datapackDir.mkdirs();
        }

        File packMcmeta = new File(datapackDir, "pack.mcmeta");
        writeJson(packMcmeta, "{\"pack\":{\"pack_format\":48,\"description\":\"ZeroEngine Custom Achievements\"}}");

        int count = 0;
        for (SAchievement a : achievements) {
            File dir = new File(datapackDir, "data" + File.separator + a.namespace() + File.separator + "advancement");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File jsonFile = new File(dir, a.id() + ".json");
            String json = buildJson(a);
            writeJson(jsonFile, json);
            count++;
        }

        SF.sf().info("[Achievement] Generated " + count + " advancement files in " + datapackDir.getAbsolutePath());
    }

    private static String buildJson(SAchievement a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"display\":{");
        sb.append("\"icon\":{\"id\":\"").append(a.icon().name().toLowerCase()).append("\"},");
        sb.append("\"title\":\"").append(escape(a.title())).append("\",");
        sb.append("\"description\":\"").append(escape(a.description())).append("\",");
        sb.append("\"frame\":\"").append(frameName(a.frame())).append("\",");
        sb.append("\"show_toast\":").append(a.showToast()).append(",");
        sb.append("\"announce_to_chat\":").append(a.announceToChat()).append(",");
        sb.append("\"hidden\":").append(a.hidden());
        sb.append("}");

        if (a.parent() != null) {
            String parentNs;
            String parentId;
            int colon = a.parent().indexOf(':');
            if (colon > 0) {
                parentNs = a.parent().substring(0, colon);
                parentId = a.parent().substring(colon + 1);
            } else {
                parentNs = a.namespace();
                parentId = a.parent();
            }
            sb.append(",\"parent\":\"").append(parentNs).append(":").append(parentId).append("\"");
        }

        sb.append(",\"criteria\":{");
        sb.append("\"").append(a.criteriaName()).append("\":{");
        sb.append("\"trigger\":\"minecraft:impossible\"");
        sb.append("}}");

        sb.append("}");
        return sb.toString();
    }

    private static String frameName(SAchievement.Frame frame) {
        switch (frame) {
            case GOAL:
                return "goal";
            case CHALLENGE:
                return "challenge";
            default:
                return "task";
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void writeJson(File file, String content) {
        try (Writer w = new FileWriter(file, StandardCharsets.UTF_8, false)) {
            w.write(content);
        } catch (IOException e) {
            SF.sf().error("[Achievement] Failed to write " + file.getName(), e);
        }
    }
}
