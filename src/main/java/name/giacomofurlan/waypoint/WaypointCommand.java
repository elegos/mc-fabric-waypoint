package name.giacomofurlan.waypoint;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import name.giacomofurlan.waypoint.client.WaypointNavigation;
import name.giacomofurlan.waypoint.client.WaypointScreen;
import name.giacomofurlan.waypoint.models.Waypoint;
import name.giacomofurlan.waypoint.network.WaypointNetworkHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
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
                        .executes(WaypointCommand::addWaypointDefaultDim)
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
            );
        });
    }

    public static void registerClient() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environament) -> {
            dispatcher.register(CommandManager.literal("waypoint")
                .executes(context -> {
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().setScreen(new WaypointScreen());
                    });
                    return 1;
                })
            );
        });
    }

    private static int addWaypointDefaultDim(CommandContext<ServerCommandSource> ctx) {
        return addWaypoint(ctx, Identifier.of("minecraft", "overworld"));
    }

    private static int addWaypointWithDim(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier dimension = IdentifierArgumentType.getIdentifier(ctx, "world");
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimension);

        ServerWorld world = ctx.getSource().getServer().getWorld(worldKey);
        if (world == null) {
            throw new SimpleCommandExceptionType(Text.literal("Invalid world: " + dimension)).create();
        }

        addWaypoint(ctx, dimension);

        if (ctx.getSource().getWorld().getRegistryKey().equals(worldKey)) {
            activateWaypoint(ctx);
        }

        return 1;
    }

    private static int addWaypoint(CommandContext<ServerCommandSource> ctx, Identifier dimension) {
        String name = StringArgumentType.getString(ctx, "name");
        Vec3d pos = Vec3ArgumentType.getVec3(ctx, "pos");
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        Waypoint wp = new Waypoint(name, new Vec3d(x, y, z), dimension);
        WaypointManager.getInstance().addWaypoint(wp);
        ctx.getSource().sendFeedback(() -> Text.literal("Waypoint \"" + name + "\" saved for dimension " + dimension), false);

        activateWaypoint(ctx);
        
        return 1;
    }

    private static int waypointsList(CommandContext<ServerCommandSource> ctx) {
        var src = ctx.getSource();
        var waypoints = WaypointManager.getInstance().getAllWaypoints();
        if (waypoints.isEmpty()) {
            src.sendFeedback(() -> Text.literal("Waypoints list is empty."), false);
        } else {
            src.sendFeedback(() -> Text.literal("Saved waypoints:"), false);
            waypoints.forEach(wp -> {
                String info = "- " + wp.getName() + " @ [" + 
                    (int)wp.getPosition().x + " " +
                    (int)wp.getPosition().y + " " +
                    (int)wp.getPosition().z + "] in " +
                    wp.getDimension();
                src.sendFeedback(() -> Text.literal(info), false);
            });
        }

        return 1;
    }

    private static void deleteWaypointCore(CommandContext<ServerCommandSource> ctx, String name) {
        Waypoint currentWaypoint = WaypointNavigation.getActiveWaypoint();
        WaypointManager.getInstance().removeWaypoint(name);
        ctx.getSource().sendFeedback(() -> Text.literal("Waypoint \"" + name + "\" was removed."), false);

        if (currentWaypoint != null) {
            WaypointNavigation.clear();
        }
    }

    private static int deleteWaypoint(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        deleteWaypointCore(ctx, name);

        return 1;
    }

    private static int deleteWaypointNoName(CommandContext<ServerCommandSource> ctx) {
        Waypoint currentWaypoint = WaypointNavigation.getActiveWaypoint();
        if (currentWaypoint == null) {
            return 0;
        }
        deleteWaypointCore(ctx, currentWaypoint.getName());

        return 1;
    }

    private static int setRange(CommandContext<ServerCommandSource> ctx) {
        double dist = DoubleArgumentType.getDouble(ctx, "distance");
        WaypointConfig.setRange(dist);
        ctx.getSource().sendFeedback(() -> Text.literal("Arrival distance set to " + dist + " blocks."), false);

        return 1;
    }

    private static int setRemoveAfterReach(CommandContext<ServerCommandSource> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "toggle");
        WaypointConfig.setRemoveAfterReach(val);
        ctx.getSource().sendFeedback(() -> Text.literal("Waypoint auto-removal: " + (val ? "enabled" : "disabled")), false);

        return 1;
    }

    private static int setToggleAfterReach(CommandContext<ServerCommandSource> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "toggle");
        WaypointConfig.setToggleAfterReach(val);
        ctx.getSource().sendFeedback(() -> Text.literal("Waypoint toggle after reach: " + (val ? "enabled" : "disabled")), false);

        return 1;
    }

    private static int activateWaypoint(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        Waypoint wp = WaypointManager.getInstance().getWaypoint(name);
        ServerCommandSource source = ctx.getSource();

        if (wp == null) {
            ctx.getSource().sendError(Text.literal("Waypoint \"" + name + "\" not found."));
            return 0;
        }

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            WaypointNetworkHandler.sendActivateWaypointPacket(player, wp);

            source.sendFeedback(() -> Text.literal("Heading to waypoint \"" + name + "\"."), false);
        } else {
            source.sendError(Text.literal("This command can only be executed by a player."));
        }


        return 1;
    }
}
