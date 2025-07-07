package name.giacomofurlan.waypoint;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import name.giacomofurlan.waypoint.models.Waypoint;

public class WaypointManager {
    private static final WaypointManager INSTANCE = new WaypointManager();
    private final Map<String, Waypoint> waypoints = new HashMap<>();
    private final File saveFile;

    private WaypointManager() {
        File configDir = new File(MinecraftClient.getInstance().runDirectory, "config/waypointmod");
        if (!configDir.exists()) configDir.mkdirs();
        saveFile = new File(configDir, "waypoints.json");

        load();
    }

    public static WaypointManager getInstance() {
        return INSTANCE;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.put(waypoint.getName().toLowerCase(), waypoint);

        save();
    }

    public void removeWaypoint(String name) {
        waypoints.remove(name.toLowerCase());

        save();
    }

    public Waypoint getWaypoint(String name) {
        return waypoints.get(name.toLowerCase());
    }

    public Collection<Waypoint> getAllWaypoints() {
        return waypoints.values();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            JsonArray array = new JsonArray();
            for (Waypoint wp : waypoints.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", wp.getName());
                obj.addProperty("x", wp.getPosition().x);
                obj.addProperty("y", wp.getPosition().y);
                obj.addProperty("z", wp.getPosition().z);
                obj.addProperty("dimension", wp.getDimension().toString());
                array.add(obj);
            }
            new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
        } catch (Exception e) {
            name.giacomofurlan.waypoint.WaypointModServer.LOGGER.error("Error while saving waypoints.", e);
        }
    }

    public void load() {
        if (!saveFile.exists()) return;

        try (FileReader reader = new FileReader(saveFile)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            waypoints.clear();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                double x = obj.get("x").getAsDouble();
                double y = obj.get("y").getAsDouble();
                double z = obj.get("z").getAsDouble();
                Identifier dim = Identifier.of(obj.get("dimension").getAsString());
                waypoints.put(name.toLowerCase(), new Waypoint(name, new Vec3d(x, y, z), dim));
            }
        } catch (Exception e) {
            name.giacomofurlan.waypoint.WaypointModServer.LOGGER.error("Error while loading waypoints.", e);
        }
    }
}
