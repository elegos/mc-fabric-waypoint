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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

public class WaypointNetworkClientHandler {
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
            WaypointSimpleActionPayload.ID,
            (payload, context) -> {
                ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();

                Action action = payload.getAction();
                String waypointName = payload.getWaypointName();
                Boolean boolValue = payload.getBoolValue();
                Double doubleValue = payload.getDoubleValue();

                Waypoint waypoint = WaypointManager.getInstance().getWaypoint(waypointName);

                switch(action) {
                    case ACTIVATE:
                        if (waypoint != null) {
                            WaypointNavigation.setActiveWaypoint(waypoint);
                        }
                            
                        break;
                    case DELETE:
                        if (waypointName != null && waypointName.length() > 0) {
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
                            chatHud.addMessage(Text.literal("Waypoints list is empty."));
                        } else {
                            chatHud.addMessage(Text.literal("Saved waypoints:"));
                            waypoints.forEach(wp -> {
                                String info = "- " + wp.getName() + " @ [" + 
                                    (int)wp.getPosition().x + " " +
                                    (int)wp.getPosition().y + " " +
                                    (int)wp.getPosition().z + "] in " +
                                    wp.getDimension();
                                chatHud.addMessage(Text.literal(info));
                            });
                        }
                        break;
                    case SET_TOGGLE_AFTER_REACH:
                        WaypointConfig.setToggleAfterReach(boolValue);
                        chatHud.addMessage(Text.literal("Waypoint toggle after reach: " + (boolValue ? "enabled" : "disabled")));
                        break;
                    case SET_REMOVE_AFTER_REACH:
                        WaypointConfig.setRemoveAfterReach(boolValue);
                        chatHud.addMessage(Text.literal("Waypoint auto-removal: " + (boolValue ? "enabled" : "disabled")));
                        break;
                    case SET_RANGE:
                        WaypointConfig.setRange(doubleValue);
                        chatHud.addMessage(Text.literal("Arrival distance set to " + doubleValue + " blocks."));
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
