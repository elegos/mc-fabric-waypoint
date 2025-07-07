package name.giacomofurlan.waypoint.network;

import name.giacomofurlan.waypoint.WaypointModServer;
import name.giacomofurlan.waypoint.models.Waypoint;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ActivateWaypointPayload(String name, BlockPos pos, Identifier dimension) implements CustomPayload {
    public static final Id<ActivateWaypointPayload> ID = new Id<>(Identifier.of(WaypointModServer.MOD_ID, "activate_waypoint"));

    public ActivateWaypointPayload(Waypoint waypoint) {
        this(waypoint.getName(), BlockPos.ofFloored(waypoint.getPosition()), waypoint.getDimension());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static ActivateWaypointPayload read(PacketByteBuf buf) {
        String name = buf.readString();
        BlockPos pos = buf.readBlockPos();
        Identifier dimension = buf.readIdentifier();

        return new ActivateWaypointPayload(name, pos, dimension);
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(name);
        buf.writeBlockPos(pos);
        buf.writeIdentifier(dimension);
    }

    public Waypoint toWaypoint() {
        return new Waypoint(name, pos.toCenterPos(), dimension);
    }
}
