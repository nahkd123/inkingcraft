package io.github.nahkd123.inkingcraft.client.gui.config;

import static io.github.nahkd123.inkingcraft.client.InkingCraftClient.getConfigStore;
import static io.github.nahkd123.inkingcraft.client.InkingCraftClient.getSpecificationsStore;
import static net.minecraft.text.Text.literal;

import io.github.nahkd123.inkingcraft.client.InkingCraftClient;
import io.github.nahkd123.inkingcraft.client.config.InkingConfiguration;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class InkingSettingsScreen extends Screen {
	private Screen parent;
	private ButtonWidget showPointers;
	private ButtonWidget showIndicators;

	public InkingSettingsScreen(Screen parent) {
		super(literal("InkingCraft Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();

		GridWidget.Adder adder = gridWidget.createAdder(2);
		adder.add(ButtonWidget.builder(literal("Configure Tablets"), button -> {
			Screen screen = new TabletsConfigurationScreen(this, getConfigStore(), getSpecificationsStore());
			client.setScreen(screen);
		}).build());

		showPointers = adder.add(ButtonWidget.builder(Text.empty(), button -> {
			InkingCraftClient.getGlobalConfig().setShowPointers(!InkingCraftClient.getGlobalConfig().isShowPointers());
			InkingCraftClient.saveGlobalConfig();
			updateWidgets();
		})
			.tooltip(Tooltip.of(literal("Show tablet pointers on the screen.")))
			.build());

		showIndicators = adder.add(ButtonWidget.builder(Text.empty(), button -> {
			InkingCraftClient.getGlobalConfig()
				.setShowIndicators(!InkingCraftClient.getGlobalConfig().isShowIndicators());
			InkingCraftClient.saveGlobalConfig();
			updateWidgets();
		})
			.tooltip(Tooltip.of(literal("Show pointers' indicators on the screen. Indicator includes pen "
				+ "pressure and pen tilting angles.")))
			.build());

		adder.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
			this.client.setScreen(this.parent);
		}).width(200).build(), 2, adder.copyPositioner().marginTop(6));

		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, 30, width, height, 0.5F, 0.0F);
		gridWidget.forEachChild(this::addDrawableChild);
		updateWidgets();
	}

	private void updateWidgets() {
		InkingConfiguration config = InkingCraftClient.getGlobalConfig();
		showPointers.setMessage(literal("Show Pointers: " + (config.isShowPointers() ? "On" : "Off")));
		showIndicators.setMessage(literal("Show Indicators: " + (config.isShowIndicators() ? "On" : "Off")));
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
	}
}
