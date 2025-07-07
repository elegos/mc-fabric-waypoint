package name.giacomofurlan.waypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.giacomofurlan.waypoint.client.WaypointHudElement;
import name.giacomofurlan.waypoint.client.WaypointNavigation;
import name.giacomofurlan.waypoint.network.ActivateWaypointPayload;
import name.giacomofurlan.waypoint.network.WaypointNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;

public class WaypointModClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(WaypointModServer.MOD_ID);

    @Override
    public void onInitializeClient() {
		WaypointConfig.load();
        WaypointNetworkHandler.registerClientPayloadTypes();
        WaypointNetworkHandler.registerClientReceivers();
        WaypointCommand.registerClient();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
			WaypointNavigation.tick();
		});

        ClientPlayNetworking.registerGlobalReceiver(
            ActivateWaypointPayload.ID,
            (payload, context) -> {
                WaypointNavigation.setActiveWaypoint(payload.toWaypoint());
            }
        );

        HudElementRegistry.addLast(Identifier.of(WaypointModServer.MOD_ID, "waypoint_hud"), new WaypointHudElement());
    }
}
