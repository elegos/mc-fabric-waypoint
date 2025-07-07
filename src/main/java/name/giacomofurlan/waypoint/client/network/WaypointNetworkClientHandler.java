package name.giacomofurlan.waypoint.client.network;

import name.giacomofurlan.waypoint.WaypointConfig;
import name.giacomofurlan.waypoint.WaypointManager;
import name.giacomofurlan.waypoint.client.WaypointNavigation;
import name.giacomofurlan.waypoint.client.WaypointScreen;
import name.giacomofurlan.waypoint.models.Waypoint;
import name.giacomofurlan.waypoint.network.AddWaypointPayload;
import name.giacomofurlan.waypoint.network.WaypointSimpleActionPayload;
import name.giacomofurlan.waypoint.network.WaypointSimpleActionPayload.Action;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

public class WaypointNetworkClientHandler {
     public static void registerClientPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(
            WaypointSimpleActionPayload.ID,
            PacketCodec.of(WaypointSimpleActionPayload::write, WaypointSimpleActionPayload::read)
        );

        PayloadTypeRegistry.playS2C().register(
            AddWaypointPayload.ID,
            PacketCodec.of(AddWaypointPayload::write, AddWaypointPayload::read)
        );
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
            WaypointSimpleActionPayload.ID,
            (payload, context) -> {
                InGameHud hud = MinecraftClient.getInstance().inGameHud;

                Action action = payload.getAction();
                String waypointName = payload.getWaypointName();
                Boolean boolValue = payload.getBoolValue();
                Double doubleValue = payload.getDoubleValue();

                Waypoint waypoint = WaypointManager.getInstance().getWaypoint(waypointName);

                if (waypoint == null) {
                    return;
                }

                switch(action) {
                    case ACTIVATE:
                        WaypointNavigation.setActiveWaypoint(waypoint);
                        break;
                    case DELETE:
                        if (waypointName != null) {
                            WaypointManager.getInstance().removeWaypoint(waypointName);
                        } else {
                            Waypoint currentWaypoint = WaypointNavigation.getActiveWaypoint();
                            if (currentWaypoint == null) {
                                break;
                            }
                            WaypointManager.getInstance().removeWaypoint(currentWaypoint.getName());
                        }
                        WaypointNavigation.clear();
                        break;
                    case OPEN_SETTINGS_SCREEN:
                        MinecraftClient.getInstance().setScreen(new WaypointScreen());
                        break;
                    case LIST:
                        var waypoints = WaypointManager.getInstance().getAllWaypoints();
                        if (waypoints.isEmpty()) {
                            hud.setOverlayMessage(Text.literal("Waypoints list is empty."), false);
                        } else {
                            hud.setOverlayMessage(Text.literal("Saved waypoints:"), false);
                            waypoints.forEach(wp -> {
                                String info = "- " + wp.getName() + " @ [" + 
                                    (int)wp.getPosition().x + " " +
                                    (int)wp.getPosition().y + " " +
                                    (int)wp.getPosition().z + "] in " +
                                    wp.getDimension();
                                hud.setOverlayMessage(Text.literal(info), false);
                            });
                        }
                        break;
                    case SET_TOGGLE_AFTER_REACH:
                        WaypointConfig.setToggleAfterReach(boolValue);
                        hud.setOverlayMessage(Text.literal("Waypoint toggle after reach: " + (boolValue ? "enabled" : "disabled")), false);
                        break;
                    case SET_REMOVE_AFTER_REACH:
                        WaypointConfig.setRemoveAfterReach(boolValue);
                        hud.setOverlayMessage(Text.literal("Waypoint auto-removal: " + (boolValue ? "enabled" : "disabled")), false);
                        break;
                    case SET_RANGE:
                        WaypointConfig.setRange(doubleValue);
                        hud.setOverlayMessage(Text.literal("Arrival distance set to " + doubleValue + " blocks."), false);
                        break;
                }
            }
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AddWaypointPayload.ID,
            (payload, context) -> {
                Waypoint wp = payload.toWaypoint();
                WaypointManager.getInstance().addWaypoint(wp);

                MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.literal("Waypoint \"" + wp.getName() + "\" saved for dimension " + wp.getDimension()), false);
                
                if (wp.getDimension().equals(context.player().getWorld().getRegistryKey().getValue())) {
                    WaypointNavigation.setActiveWaypoint(wp);
                }
            }
        );
    }
}
