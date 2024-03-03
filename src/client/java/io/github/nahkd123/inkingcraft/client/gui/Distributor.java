package io.github.nahkd123.inkingcraft.client.gui;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;

public class Distributor {
	public static void distributeHorizontally(ScreenRect area, int marginX, ClickableWidget... widgets) {
		int widgetWidth = area.width() / widgets.length;

		for (int i = 0; i < widgets.length; i++) {
			int x = area.getLeft() + i * (widgetWidth + marginX);
			widgets[i].setPosition(x, area.getTop());
			widgets[i].setDimensions(widgetWidth, area.height());
		}
	}
}
