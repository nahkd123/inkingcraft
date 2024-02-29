package io.github.nahkd123.inkingcraft.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.widget.ClickableWidget;

public class ClickablesCollection {
	private List<ClickableWidget> widgets = new ArrayList<>();

	public List<ClickableWidget> getWidgets() { return widgets; }

	public void makeAllVisible() {
		widgets.forEach(w -> w.visible = true);
	}

	public void makeAllInvisible() {
		widgets.forEach(w -> w.visible = false);
	}
}
