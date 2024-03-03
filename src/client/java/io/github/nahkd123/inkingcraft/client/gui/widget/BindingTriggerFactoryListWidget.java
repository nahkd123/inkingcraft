package io.github.nahkd123.inkingcraft.client.gui.widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inkingcraft.client.config.binding.trigger.BindingTriggerFactory;
import io.github.nahkd123.inkingcraft.client.gui.widget.BindingTriggerFactoryListWidget.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

public class BindingTriggerFactoryListWidget extends ElementListWidget<Entry> {
	private Supplier<BindingTriggerFactory> current;
	private Consumer<BindingTriggerFactory> onSelect;

	public BindingTriggerFactoryListWidget(int width, int height, int y, Supplier<BindingTriggerFactory> current, Consumer<BindingTriggerFactory> onSelect) {
		super(MinecraftClient.getInstance(), width, height, y, 26);
		this.current = current;
		BindingTriggerFactory.REGISTRY.values().forEach(f -> addEntry(new Entry(this, f)));
		updateList();
		this.onSelect = onSelect;
	}

	public void updateList() {
		BindingTriggerFactory currentFactory = current.get();
		if (currentFactory == null) return;

		for (Entry e : children()) if (e.getFactory() == currentFactory) {
			setSelected(e);
			return;
		}

		setSelected(null);
	}

	@Override
	public void setSelected(Entry entry) {
		super.setSelected(entry);
		if (onSelect != null) onSelect.accept(entry != null ? entry.factory : null);
	}

	@Override
	public int getRowWidth() { return getWidth() - 6; }

	@Override
	public int getRowLeft() { return getX(); }

	@Override
	protected int getScrollbarPositionX() { return getRight() - 6; }

	public static class Entry extends ElementListWidget.Entry<Entry> {
		private BindingTriggerFactoryListWidget parent;
		private BindingTriggerFactory factory;

		public Entry(BindingTriggerFactoryListWidget parent, BindingTriggerFactory factory) {
			this.parent = parent;
			this.factory = factory;
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return Collections.emptyList();
		}

		@SuppressWarnings("resource")
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			TextRenderer tr = MinecraftClient.getInstance().textRenderer;
			context.drawText(tr, factory.getName(), x + 4, y + 2, 0xFFFFFF, true);
			context.drawText(tr, factory.getDescription(), x + 4, y + 3 + tr.fontHeight, 0x7F7F7F, true);
		}

		@Override
		public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			boolean selected = parent.getSelectedOrNull() == this;
			if (selected) context.drawBorder(x, y, entryWidth, entryHeight, 0xFFFFFFFF);
			else if (hovered) context.drawBorder(x, y, entryWidth, entryHeight, 0x7FFFFFFF);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			parent.setSelected(this);
			return true;
		}

		public BindingTriggerFactory getFactory() { return factory; }
	}
}
