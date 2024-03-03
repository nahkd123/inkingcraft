package io.github.nahkd123.inkingcraft.client.gui.widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.client.config.binding.BindingConfiguration;
import io.github.nahkd123.inkingcraft.client.config.binding.BindingSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

public class BindingSlotsListWidget extends ElementListWidget<io.github.nahkd123.inkingcraft.client.gui.widget.BindingSlotsListWidget.Entry> {
	private Supplier<BindingConfiguration> binding;
	private Supplier<TabletSpec> spec;
	private int lastIndex = 0;
	private Consumer<BindingSlot> onSelect;

	public BindingSlotsListWidget(int width, int height, int y, Supplier<BindingConfiguration> binding, Supplier<TabletSpec> spec, Consumer<BindingSlot> onSelect) {
		super(MinecraftClient.getInstance(), width, height, y, 26);
		this.binding = binding;
		this.spec = spec;
		updateList();
		this.onSelect = onSelect;
	}

	public void updateList() {
		clearEntries();
		BindingConfiguration config = binding.get();
		if (config == null) return;
		config.getSlots(spec.get()).forEach(s -> addEntry(new Entry(this, s)));
		if (children().size() > 0) setSelected(children().get(Math.min(lastIndex, children().size() - 1)));
	}

	@Override
	public int getRowWidth() { return getWidth() - 6; }

	@Override
	public int getRowLeft() { return getX(); }

	@Override
	protected int getScrollbarPositionX() { return getRight() - 6; }

	@Override
	public void setSelected(Entry entry) {
		super.setSelected(entry);
		int nextIndex = children().indexOf(entry);

		if (nextIndex != lastIndex) {
			lastIndex = nextIndex;
			if (onSelect != null) onSelect.accept(entry != null ? entry.slot : null);
		}
	}

	public static class Entry extends ElementListWidget.Entry<Entry> {
		private BindingSlotsListWidget parent;
		private BindingSlot slot;

		public Entry(BindingSlotsListWidget parent, BindingSlot slot) {
			this.parent = parent;
			this.slot = slot;
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
			context.drawText(tr, slot.target().getName(), x + 4, y + 2, 0xFFFFFF, true);
			context.drawText(tr, slot.get().getFactory().getName(), x + 4, y + 3 + tr.fontHeight, 0x7F7F7F, true);
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

		public BindingSlot getSlot() { return slot; }
	}
}
