package io.github.nahkd123.inkingcraft.client.gui.config.tab;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class MiscTab implements Tab {
	public SharedHeader header;

	public MiscTab(Screen parent, Supplier<TabletConfiguration> configuration, Runnable previousTablet, Runnable nextTablet) {
		header = new SharedHeader(parent, configuration, previousTablet, nextTablet);
	}

	@Override
	public Text getTitle() { return Text.literal("Miscellaneous"); }

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		header.forEachChild(consumer);
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		header.refreshGrid(tabArea);
	}
}
