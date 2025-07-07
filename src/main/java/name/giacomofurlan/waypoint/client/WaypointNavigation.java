package name.giacomofurlan.waypoint.client;

import name.giacomofurlan.waypoint.WaypointConfig;
import name.giacomofurlan.waypoint.WaypointManager;
import name.giacomofurlan.waypoint.models.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class WaypointNavigation {
    private static Waypoint activeWaypoint = null;

    public static Waypoint getActiveWaypoint() {
        return activeWaypoint;
    }

    public static void setActiveWaypoint(Waypoint waypoint) {
        activeWaypoint = waypoint;
    }

    public static void clear() {
        activeWaypoint = null;
    }

    public static boolean isActive() {
        return activeWaypoint != null;
    }

    public static void tick() {
        if (!isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        if (!client.world.getRegistryKey().getValue().equals(activeWaypoint.getDimension())) {
            return;
        }

        Vec3d playerPos = client.player.getPos();
        Vec3d targetPos = activeWaypoint.getPosition();

        double distance = playerPos.distanceTo(targetPos);

        if (distance <= WaypointConfig.getRange()) {
            if (WaypointConfig.isRemoveAfterReach()) {
                WaypointManager.getInstance().removeWaypoint(activeWaypoint.getName());
            }

            if (WaypointConfig.isToggleAfterReach()) {
                clear();
            }
        }
    }
}
