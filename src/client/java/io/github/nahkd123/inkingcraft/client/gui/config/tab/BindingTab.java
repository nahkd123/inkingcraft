package io.github.nahkd123.inkingcraft.client.gui.config.tab;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.client.config.binding.BindingConfiguration;
import io.github.nahkd123.inkingcraft.client.config.binding.BindingSlot;
import io.github.nahkd123.inkingcraft.client.config.binding.trigger.InkingTriggers;
import io.github.nahkd123.inkingcraft.client.gui.Distributor;
import io.github.nahkd123.inkingcraft.client.gui.widget.BindingSlotsListWidget;
import io.github.nahkd123.inkingcraft.client.gui.widget.BindingTriggerFactoryListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BindingTab implements Tab {
	private Supplier<BindingConfiguration> binding;

	public TextWidget mouseLabel;
	public ButtonWidget enableMouseEmuButton, alwaysRelativeButton, emulateGuiClicks;
	public SliderWidget maximumHoverDistanceSlider;
	private DoubleConsumer maxHoverDistSliderValue;

	public TextWidget slotsLabel, triggersLabel;
	public BindingSlotsListWidget slots;
	public BindingTriggerFactoryListWidget triggers;

	@SuppressWarnings("resource")
	public BindingTab(Supplier<BindingConfiguration> binding, Supplier<TabletSpec> spec, Runnable onChanges) {
		this.binding = binding;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		mouseLabel = new TextWidget(Text.literal("Mouse emulation"), textRenderer);
		enableMouseEmuButton = ButtonWidget.builder(Text.empty(), button -> {
			BindingConfiguration config = binding.get();
			if (config == null) return;
			config.getMouse().setEnabled(!config.getMouse().isEnabled());
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Control the in-game mouse and camera with graphics tablet.")))
			.width(100)
			.build();
		alwaysRelativeButton = ButtonWidget.builder(Text.empty(), button -> {
			BindingConfiguration config = binding.get();
			if (config == null) return;
			config.getMouse().setAlwaysRelative(!config.getMouse().isAlwaysRelative());
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Always control the mouse in relative mode, regradless the cursor "
				+ "locking state.")))
			.width(100)
			.build();
		emulateGuiClicks = ButtonWidget.builder(Text.empty(), button -> {
			BindingConfiguration config = binding.get();
			if (config == null) return;
			config.getMouse().setEmulateClicks(!config.getMouse().isEmulateClicks());
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Emulate left click on pen tap, right click on pen/aux button #1 and "
				+ "middle click on pen/aux button #2")))
			.width(100)
			.build();
		maximumHoverDistanceSlider = new SliderWidget(0, 0, 0, 0, Text.empty(), 0d) {
			{
				BindingTab.this.maxHoverDistSliderValue = v -> value = v;
			}

			@Override
			protected void updateMessage() {
				BindingConfiguration config = binding.get();
				if (config == null) return;
				setMessage(Text.literal("Max. Hovering Distance: " + config.getMouse().getMaximumHoverDistance()));
			}

			@Override
			protected void applyValue() {
				BindingConfiguration config = binding.get();
				if (config == null) return;
				config.getMouse().setMaximumHoverDistance((int) (value * 1024));
				onChanges.run();
			}
		};
		maximumHoverDistanceSlider.setTooltip(Tooltip.of(Text.literal("The maximum hovering distance for mouse "
			+ "emulation. Not all tablets support pen hovering distance.")));

		// @formatter:off
		slotsLabel = new TextWidget(Text.literal("Slots"), textRenderer);
		triggersLabel = new TextWidget(Text.literal("Triggers"), textRenderer);
		slots = new BindingSlotsListWidget(100, 100, 0, binding, spec, slot -> updateWidgets());
		triggers = new BindingTriggerFactoryListWidget(100, 100, 0,
			() -> slots.getSelectedOrNull() != null
				? slots.getSelectedOrNull().getSlot().get().getFactory()
				: InkingTriggers.EMPTY,
			factory -> {
				var slotEntry = slots.getSelectedOrNull();
				if (slotEntry == null) return;
				BindingSlot slot = slotEntry.getSlot();
				if (slot.get().getFactory() == factory) return;
				slot.set(factory.createDefault());
				onChanges.run();
				updateWidgets();
			});
		// @formatter:on
	}

	@Override
	public Text getTitle() { return Text.literal("Binding"); }

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		consumer.accept(mouseLabel);
		consumer.accept(enableMouseEmuButton);
		consumer.accept(alwaysRelativeButton);
		consumer.accept(emulateGuiClicks);
		consumer.accept(maximumHoverDistanceSlider);

		consumer.accept(slotsLabel);
		consumer.accept(slots);
		consumer.accept(triggersLabel);
		consumer.accept(triggers);
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		boolean thinMode = tabArea.width() < 600 || tabArea.height() < 400;
		int sidebarWidth = Math.min(tabArea.width() / 3, thinMode ? 100 : 120);
		int pad = thinMode ? 4 : 10;
		int hpad = pad / 2;

		// @formatter:off
		mouseLabel.setPosition(tabArea.getLeft() + (tabArea.width() - mouseLabel.getWidth()) / 2, tabArea.getTop() + pad);
		ScreenRect mouseEmuButtonsArea = new ScreenRect(sidebarWidth / 2, mouseLabel.getBottom() + hpad, tabArea.width() - sidebarWidth, 20);
		Distributor.distributeHorizontally(
			mouseEmuButtonsArea,
			hpad,
			enableMouseEmuButton,
			alwaysRelativeButton,
			emulateGuiClicks,
			maximumHoverDistanceSlider);
		
		slotsLabel.setPosition(tabArea.getLeft() + pad, mouseEmuButtonsArea.getBottom() + hpad);
		int listHeight = tabArea.height()
			- (mouseEmuButtonsArea.getBottom() - mouseEmuButtonsArea.getTop())
			- slotsLabel.getHeight() - hpad - pad * 4;
		slots.setPosition(slotsLabel.getX(), slotsLabel.getBottom() + hpad);
		slots.setDimensions(Math.min(200, tabArea.width() / 3), listHeight);
		
		triggersLabel.setPosition(slots.getRight() + hpad, slotsLabel.getY());
		triggers.setPosition(triggersLabel.getX(), triggersLabel.getBottom() + hpad);
		triggers.setDimensions(Math.min(200, tabArea.width() / 3), listHeight);
		// @formatter:on

		updateWidgets();
	}

	public void updateWidgets() {
		BindingConfiguration config = binding.get();

		if (config != null) {
			enableMouseEmuButton.active = true;
			enableMouseEmuButton.setMessage(Text.literal("Mouse emulation: ")
				.append(Text.literal(config.getMouse().isEnabled() ? "On" : "Off")
					.styled(s -> s.withColor(config.getMouse().isEnabled() ? Formatting.GREEN : Formatting.RED))));

			alwaysRelativeButton.active = true;
			alwaysRelativeButton.setMessage(Text.literal("Always relative: ")
				.append(Text.literal(config.getMouse().isAlwaysRelative() ? "On" : "Off")
					.styled(s -> s.withColor(config.getMouse().isAlwaysRelative()
						? Formatting.GREEN
						: Formatting.RED))));

			emulateGuiClicks.active = true;
			emulateGuiClicks.setMessage(Text.literal("Emulate GUI clicks: ")
				.append(Text.literal(config.getMouse().isEmulateClicks() ? "On" : "Off")
					.styled(s -> s.withColor(config.getMouse().isEmulateClicks()
						? Formatting.GREEN
						: Formatting.RED))));

			maximumHoverDistanceSlider.active = true;
			maximumHoverDistanceSlider.setMessage(Text.literal("Max. Hovering Distance: "
				+ config.getMouse().getMaximumHoverDistance()));
			maxHoverDistSliderValue.accept(config.getMouse().getMaximumHoverDistance() / 1024d);

			if (slots != null) slots.active = triggers.active = true;
		} else {
			enableMouseEmuButton.active = false;
			enableMouseEmuButton.setMessage(Text.literal("Mouse emulation: n/a"));

			alwaysRelativeButton.active = false;
			alwaysRelativeButton.setMessage(Text.literal("Always relative: n/a"));

			emulateGuiClicks.active = false;
			emulateGuiClicks.setMessage(Text.literal("Emulate GUI clicks: n/a"));

			maximumHoverDistanceSlider.active = false;
			maximumHoverDistanceSlider.setMessage(Text.literal("Max. Hovering Distance: n/a"));

			if (slots != null) slots.active = triggers.active = false;
		}

		if (slots != null) slots.updateList();
		if (triggers != null) triggers.updateList();
	}
}
