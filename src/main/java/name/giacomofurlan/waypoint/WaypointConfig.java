package name.giacomofurlan.waypoint;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;

public class WaypointConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(WaypointMod.MOD_ID + "/config.json");

    private static double range = 2.0;
    private static boolean toggleAfterReach = true;
    private static boolean removeAfterReach = true;

    public static double getRange() {
        return range;
    }

    public static boolean isToggleAfterReach() {
        return toggleAfterReach;
    }

    public static boolean isRemoveAfterReach() {
        return removeAfterReach;
    }

    public static void setRange(double range) {
        WaypointConfig.range = range;
        save();
    }

    public static void setToggleAfterReach(boolean toggleAfterReach) {
        WaypointConfig.toggleAfterReach = toggleAfterReach;
    }

    public static void setRemoveAfterReach(boolean removeAfterReach) {
        WaypointConfig.removeAfterReach = removeAfterReach;
        save();
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("range")) {
                range = json.get("range").getAsDouble();
            }
            if (json.has("toggleAfterReach")) {
                toggleAfterReach = json.get("toggleAfterReach").getAsBoolean();
            }
            if (json.has("removeAfterReach")) {
                removeAfterReach = json.get("removeAfterReach").getAsBoolean();
            }
        } catch (Exception e) {
            WaypointMod.LOGGER.error("Error while loading Waypoint config.", e);
        }
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.add("range", new JsonPrimitive(range));
        json.add("toggleAfterReach", new JsonPrimitive(toggleAfterReach));
        json.add("removeAfterReach", new JsonPrimitive(removeAfterReach));

        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            writer.write(json.toString());
        } catch (IOException e) {
            WaypointMod.LOGGER.error("Error while saving Waypoint config.", e);
        }
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
