package name.giacomofurlan.waypoint.network;

import name.giacomofurlan.waypoint.WaypointModServer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaypointSimpleActionPayload(
    WaypointSimpleActionPayload.Action action, String waypointName, Boolean boolValue, Double doubleValue
) implements CustomPayload {
    public static final Id<WaypointSimpleActionPayload> ID = new Id<>(Identifier.of(WaypointModServer.MOD_ID, "waypoint_simple_action"));

    public WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action action) {
        this(action, null, null, null);
    }

    public WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action action, String waypointName) {
        this(action, waypointName, null, null);
    }

    public WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action action, Boolean boolValue) {
        this(action, null, boolValue, null);
    }

    public WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action action, Double doubleValue) {
        this(action, null, null, doubleValue);
    }

    public enum Action {
        LIST,
        ACTIVATE,
        DELETE,
        OPEN_SETTINGS_SCREEN,
        SET_TOGGLE_AFTER_REACH,
        SET_REMOVE_AFTER_REACH,
        SET_RANGE
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static WaypointSimpleActionPayload read(PacketByteBuf buf) {
        Action action = buf.readEnumConstant(Action.class);
        String name = buf.readString();
        return new WaypointSimpleActionPayload(action, name);
    }

    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(action);
        buf.writeString(waypointName);
    }

    public String getWaypointName() {
        return waypointName;
    }

    public Action getAction() {
        return action;
    }

    public Boolean getBoolValue() {
        return boolValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }
}
