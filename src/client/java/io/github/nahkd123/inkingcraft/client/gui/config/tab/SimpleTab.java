package io.github.nahkd123.inkingcraft.client.gui.config.tab;

import java.util.function.Consumer;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class SimpleTab implements Tab {
	private Text title;
	private ClickableWidget[] children;

	public SimpleTab(Text title, ClickableWidget... children) {
		this.title = title;
		this.children = children;
	}

	@Override
	public Text getTitle() { return title; }

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		for (ClickableWidget child : children) consumer.accept(child);
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {}
}
