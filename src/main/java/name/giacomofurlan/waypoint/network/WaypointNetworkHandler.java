package name.giacomofurlan.waypoint.network;

import name.giacomofurlan.waypoint.models.Waypoint;
import name.giacomofurlan.waypoint.network.WaypointSimpleActionPayload.Action;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class WaypointNetworkHandler {

    public static void sendWaypointSimpleActionPayload(ServerPlayerEntity player, WaypointSimpleActionPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendWaypointSimpleActionPayload(ServerPlayerEntity player, Action action, String waypointName) {
        sendWaypointSimpleActionPayload(player, new WaypointSimpleActionPayload(action, waypointName));
    }

    public static void sendAddWaypointPacket(ServerPlayerEntity player, AddWaypointPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendAddWaypointPacket(ServerPlayerEntity player, Waypoint waypoint) {
        sendAddWaypointPacket(player, new AddWaypointPayload(waypoint));
    }
}
