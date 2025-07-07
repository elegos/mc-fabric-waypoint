package name.giacomofurlan.waypoint.network;

import name.giacomofurlan.waypoint.client.WaypointNavigation;
import name.giacomofurlan.waypoint.models.Waypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;

public class WaypointNetworkHandler {
    public static void registerClientPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(
            ActivateWaypointPayload.ID,
            PacketCodec.of(ActivateWaypointPayload::write, ActivateWaypointPayload::read)
        );
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
            ActivateWaypointPayload.ID,
            (payload, context) -> {
                WaypointNavigation.setActiveWaypoint(payload.toWaypoint());
            }
        );
    }

    public static void sendActivateWaypointPacket(ServerPlayerEntity player, ActivateWaypointPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendActivateWaypointPacket(ServerPlayerEntity player, Waypoint waypoint) {
        sendActivateWaypointPacket(player, new ActivateWaypointPayload(waypoint));
    }
}
