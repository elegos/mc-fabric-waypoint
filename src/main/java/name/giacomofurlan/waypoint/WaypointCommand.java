package name.giacomofurlan.waypoint;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import name.giacomofurlan.waypoint.models.Waypoint;
import name.giacomofurlan.waypoint.network.AddWaypointPayload;
import name.giacomofurlan.waypoint.network.WaypointNetworkHandler;
import name.giacomofurlan.waypoint.network.WaypointSimpleActionPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WaypointCommand {
    private static final SuggestionProvider<ServerCommandSource> DIMENSION_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestIdentifiers(
            context.getSource().getServer().getWorldRegistryKeys().stream().map(RegistryKey::getValue),
            builder
        );
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("waypoint")
                .then(CommandManager.argument("name", StringArgumentType.string())
                    .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                        .executes(WaypointCommand::addWaypointCurrentDim)
                        .then(CommandManager.argument("world", IdentifierArgumentType.identifier())
                            .suggests(DIMENSION_SUGGESTIONS)
                            .executes(WaypointCommand::addWaypointWithDim)
                        )
                    )
                    .executes(WaypointCommand::activateWaypoint)
                )

                .then(CommandManager.literal("list")
                    .executes(WaypointCommand::waypointsList))

                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .executes(WaypointCommand::deleteWaypoint))
                    .executes(WaypointCommand::deleteWaypointNoName))

                .then(CommandManager.literal("range")
                    .then(CommandManager.argument("distance", DoubleArgumentType.doubleArg(0))
                        .executes(WaypointCommand::setRange)))

                .then(CommandManager.literal("removeAfterReach")
                    .then(CommandManager.argument("toggle", BoolArgumentType.bool())
                        .executes(WaypointCommand::setRemoveAfterReach)))
                
                .then(CommandManager.literal("toggleAfterReach")
                    .then(CommandManager.argument("toggle", BoolArgumentType.bool())
                        .executes(WaypointCommand::setToggleAfterReach)))
                
                .executes(WaypointCommand::openSettingsScreen)
            );
        });
    }

    private static int addWaypointCurrentDim(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal("This command can only be executed by a player."));
            return 1;
        }

        Identifier dimension = ctx.getSource().getWorld().getRegistryKey().getValue();
        return addWaypoint(ctx, dimension);
    }

    private static int addWaypointWithDim(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal("This command can only be executed by a player."));
            return 1;
        }

        Identifier dimension = IdentifierArgumentType.getIdentifier(ctx, "world");
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimension);

        ServerWorld world = ctx.getSource().getServer().getWorld(worldKey);
        if (world == null) {
            throw new SimpleCommandExceptionType(Text.literal("Invalid world: " + dimension)).create();
        }

        addWaypoint(ctx, dimension);

        return 1;
    }

    private static int addWaypoint(CommandContext<ServerCommandSource> ctx, Identifier dimension) {
        ServerCommandSource source = ctx.getSource();

        String name = StringArgumentType.getString(ctx, "name");
        Vec3d pos = Vec3ArgumentType.getVec3(ctx, "pos");
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        Waypoint wp = new Waypoint(name, new Vec3d(x, y, z), dimension);

        AddWaypointPayload payload = new AddWaypointPayload(wp);
        WaypointNetworkHandler.sendAddWaypointPacket(source.getPlayer(), payload);

        return 1;
    }

    private static int waypointsList(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.LIST);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int deleteWaypoint(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.DELETE, name);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int deleteWaypointNoName(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.DELETE);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int setRange(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        double dist = DoubleArgumentType.getDouble(ctx, "distance");

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.SET_RANGE, dist);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int setRemoveAfterReach(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        boolean val = BoolArgumentType.getBool(ctx, "toggle");

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.SET_REMOVE_AFTER_REACH, val);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int setToggleAfterReach(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        boolean val = BoolArgumentType.getBool(ctx, "toggle");

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.SET_TOGGLE_AFTER_REACH, val);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int activateWaypoint(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ServerCommandSource source = ctx.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.ACTIVATE, name);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }

    private static int openSettingsScreen(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointSimpleActionPayload payload = new WaypointSimpleActionPayload(WaypointSimpleActionPayload.Action.OPEN_SETTINGS_SCREEN);
            WaypointNetworkHandler.sendWaypointSimpleActionPayload(player, payload);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }

        return 1;
    }
}
