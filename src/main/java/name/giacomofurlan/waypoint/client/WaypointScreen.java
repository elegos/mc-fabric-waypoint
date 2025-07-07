package name.giacomofurlan.waypoint.client;

import name.giacomofurlan.waypoint.WaypointConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class WaypointScreen extends Screen {
    CheckboxWidget toggleAtArrival;
    CheckboxWidget removeAtArrival;
    TextFieldWidget range;
    TextWidget rangeLabel;

    public WaypointScreen() {
        super(Text.of("Waypoints"));
    }
    
    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int y = this.height / 4;

        toggleAtArrival = CheckboxWidget.builder(Text.literal("Toggle at arrival"), client.textRenderer)
            .tooltip(Tooltip.of(Text.literal("Stop the navigation when near the waypoint")))
            .checked(WaypointConfig.isToggleAfterReach())
            .callback((checkbox, checked) -> WaypointConfig.setToggleAfterReach(checked))
            .pos(Math.max(centerX - 65, 0), y)
            .build();
        
        y += 25;

        removeAtArrival = CheckboxWidget.builder(Text.literal("Remove at arrival"), client.textRenderer)
            .tooltip(Tooltip.of(Text.literal("Remove the waypoint when near the waypoint")))
            .checked(WaypointConfig.isRemoveAfterReach())
            .callback((checkbox, checked) -> WaypointConfig.setRemoveAfterReach(checked))
            .pos(Math.max(centerX - 65, 0), y)
            .build();
        
        y += 30;

        rangeLabel = new TextWidget(200, 20, Text.literal("Arrival distance range:"), client.textRenderer);
        rangeLabel.setPosition(Math.max(centerX - 65, 0), y);
        y += 20;
        
        range = new TextFieldWidget(client.textRenderer, Math.max(centerX - 65, 0), y, 200, 20, Text.of("Range"));
        range.setText(String.valueOf(WaypointConfig.getRange()));
        range.setChangedListener(value -> {
            try {
                int parsed = Integer.parseInt(value);
                WaypointConfig.setRange(parsed);
            } catch (NumberFormatException e) {
                // ignore
            }
        });

        y += 30;

        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Close"), button -> {
            this.close();
        }).dimensions(this.width - 80, this.height - 30, 70, 20).build();

        this.addDrawableChild(toggleAtArrival);
        this.addDrawableChild(removeAtArrival);
        this.addDrawableChild(rangeLabel);
        this.addDrawableChild(range);
        this.addDrawableChild(closeButton);
    }
}
