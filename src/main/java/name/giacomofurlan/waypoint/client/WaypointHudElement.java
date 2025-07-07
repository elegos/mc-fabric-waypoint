package name.giacomofurlan.waypoint.client;

import name.giacomofurlan.waypoint.models.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class WaypointHudElement implements HudElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final Identifier BAR_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/locator_bar_background.png");
    private static final Identifier DOT_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/locator_bar_dot/default_0.png");
    private static final Identifier ARROW_UP_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/locator_bar_arrow_up.png");
    private static final Identifier ARROW_DOWN_TEXTURE = Identifier.of("minecraft", "textures/gui/sprites/hud/locator_bar_arrow_down.png");

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        Waypoint waypoint = WaypointNavigation.getActiveWaypoint();

        if (
            waypoint == null
            || client.player == null
            || !waypoint.getDimension().equals(client.player.getWorld().getRegistryKey().getValue())
        ) {
            return;
        }

        Vec3d playerPos = client.player.getPos();
        Vec3d targetPos = waypoint.getPosition();

        double distance = playerPos.distanceTo(targetPos);
        double dx = targetPos.x - playerPos.x;
        double dz = targetPos.z - playerPos.z;

        double angleToTarget = Math.atan2(dz, dx) - Math.PI / 2;
        double playerYaw = Math.toRadians(normalizeDegrees(client.player.getYaw(tickCounter.getDynamicDeltaTicks())));
        double angleDiff = playerYaw - angleToTarget;
        angleDiff = Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff)); // Normalize [-π, π]

        int screenWidth = context.getScaledWindowWidth();

        int barWidth = screenWidth / 3;
        int barHeight = 5;

        int barX = (screenWidth - barWidth) / 2;
        int barY = 20;

        int maxOffset = barWidth / 2 - 4; // Border margin
        int offset = (int)(angleDiff / Math.PI * maxOffset); // [-maxOffset, +maxOffset]
        int dotX = barX + barWidth / 2 - offset - 4;
        int dotY = barY - 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, BAR_TEXTURE, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, DOT_TEXTURE, dotX, dotY, 0, 0, 8, 8, 8, 8);

        double dy = waypoint.getPosition().y - playerPos.y;

        if (dy > 2) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_UP_TEXTURE, dotX, dotY - 7, 0, 0, 7, 4, 7, 8);
        } else if (dy < -2) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_DOWN_TEXTURE, dotX, dotY + 7, 0, 0, 7, 4, 7, 8);
        }

        // Name and distance
        String label = waypoint.getName() + " [" + (int) distance + "m]";
        context.drawText(
            client.textRenderer,
            Text.literal(label),
            barX, barY + 12, 0xFFFFFFFF,
            true
        );
    }

    public static double normalizeDegrees(double angle) {
        angle = angle % 360;

        if (angle >= 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }

        return angle;
    }
}
