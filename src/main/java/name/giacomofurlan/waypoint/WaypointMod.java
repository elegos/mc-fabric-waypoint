package name.giacomofurlan.waypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.giacomofurlan.waypoint.network.AddWaypointPayload;
import name.giacomofurlan.waypoint.network.WaypointSimpleActionPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;

public class WaypointMod implements ModInitializer {
	public static final String MOD_ID = "name.giacomofurlan.waypoint";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WaypointCommand.register();
        registerPayloadTypes();
	}

	private static void registerPayloadTypes() {
		PayloadTypeRegistry.playS2C().register(
            WaypointSimpleActionPayload.ID,
            PacketCodec.of(WaypointSimpleActionPayload::write, WaypointSimpleActionPayload::read)
        );

        PayloadTypeRegistry.playS2C().register(
            AddWaypointPayload.ID,
            PacketCodec.of(AddWaypointPayload::write, AddWaypointPayload::read)
        );
	}
}