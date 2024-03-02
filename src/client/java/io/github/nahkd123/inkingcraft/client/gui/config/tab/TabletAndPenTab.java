package io.github.nahkd123.inkingcraft.client.gui.config.tab;

import java.text.DecimalFormat;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.config.PressureMappingPoint;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.gui.config.AreaMappingWidget;
import io.github.nahkd123.inkingcraft.client.gui.config.PressureMappingWidget;
import io.github.nahkd123.inkingcraft.client.utils.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TabletAndPenTab implements Tab {
	private static final DecimalFormat FORMATTER_FULL = new DecimalFormat("#,##0.##");
	private static final DecimalFormat FORMATTER_THIN = new DecimalFormat("#0");
	private Supplier<TabletConfiguration> configuration;
	private Supplier<TabletSpec> spec;

	// Widgets and UI
	private TextRenderer textRenderer;
	private boolean ignoreTextFields = false, inputtingMode = false, thinMode = false;
	public SharedHeader header;

	public AreaMappingWidget area;
	public ButtonWidget letterboxingButton, resetAreaButton;
	public TextWidget positionLabel, areaLabel;
	public TextFieldWidget areaX, areaY, areaWidth, areaHeight;

	public PressureMappingWidget pressure;
	public ButtonWidget deleteNodeButton;
	public TextWidget pressureLabel;
	public TextFieldWidget sourcePressure, targetPressure;

	// Late refresh
	private Vector2 screenSize = new ConstantVector2(1, 1);

	@SuppressWarnings("resource")
	public TabletAndPenTab(Screen parent, Supplier<TabletConfiguration> configuration, Supplier<TabletSpec> spec, Runnable onChanges, Runnable previousTablet, Runnable nextTablet) {
		this.textRenderer = MinecraftClient.getInstance().textRenderer;
		this.configuration = configuration;
		this.spec = spec;

		// @formatter:off
		header = new SharedHeader(parent, configuration, previousTablet, nextTablet);

		area = new AreaMappingWidget(0, 0, 0, 0,
			() -> configuration.get() != null ? configuration.get().getAreaMapping() : null,
			() -> spec.get().getPhysicalSize(),
			() -> spec.get().getInputSize(),
			() -> configuration.get().getAreaMapping().isLetterboxing()
				? screenSize
				: configuration.get().getAreaMapping().getTabletArea().getSize(),
			() -> {
				onChanges.run();
				updateWidgets();
			});
		letterboxingButton = ButtonWidget.builder(Text.empty(), button -> {
			TabletConfiguration config = configuration.get();
			if (config == null) return;
			config.getAreaMapping().setLetterboxing(!config.getAreaMapping().isLetterboxing());
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Maintain the screen's aspect ratio on tablet.")))
			.width(100)
			.build();
		resetAreaButton = ButtonWidget.builder(Text.literal("Reset Area"), button -> {
			TabletConfiguration config = configuration.get();
			if (config == null) return;
			config.getAreaMapping().setTabletArea(new Rectangle(ConstantVector2.ZERO, spec.get().getInputSize()));
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Reset mapping to full area for this tablet.")))
			.width(100)
			.build();
		positionLabel = new TextWidget(Text.literal("Position (X, Y)"), textRenderer);
		areaX = new TextFieldWidget(textRenderer, 50, 20, Text.literal("Area X"));
		areaY = new TextFieldWidget(textRenderer, 50, 20, Text.literal("Area Y"));
		areaLabel = new TextWidget(Text.literal("Area (Width, Height)"), textRenderer);
		areaWidth = new TextFieldWidget(textRenderer, 50, 20, Text.literal("Area Width"));
		areaHeight = new TextFieldWidget(textRenderer, 50, 20, Text.literal("Input Height"));
		setNumericalInputCallbackFor(areaX, configuration.get().getAreaMapping()::getTabletArea, Rectangle::withX, configuration.get().getAreaMapping()::setTabletArea);
		setNumericalInputCallbackFor(areaY, configuration.get().getAreaMapping()::getTabletArea, Rectangle::withY, configuration.get().getAreaMapping()::setTabletArea);
		setNumericalInputCallbackFor(areaWidth, configuration.get().getAreaMapping()::getTabletArea, Rectangle::withWidth, configuration.get().getAreaMapping()::setTabletArea);
		setNumericalInputCallbackFor(areaHeight, configuration.get().getAreaMapping()::getTabletArea, Rectangle::withHeight, configuration.get().getAreaMapping()::setTabletArea);

		pressure = new PressureMappingWidget(0, 0, 0, 0,
			() -> configuration.get() != null ? configuration.get().getPressureMapping() : null,
			() -> spec.get().getMaxPressure(),
			() -> {
				onChanges.run();
				updateWidgets();
			});
		deleteNodeButton = ButtonWidget.builder(Text.literal("Delete Node"), button -> {
			TabletConfiguration config = configuration.get();
			if (config == null) return;
			if (pressure.getSelectedPoint() == null) return;
			config.getPressureMapping().remove(pressure.getSelectedPoint());
			pressure.setSelectedPoint(null);
			onChanges.run();
			updateWidgets();
		})
			.tooltip(Tooltip.of(Text.literal("Delete selected point/node in pressure mapping.")))
			.width(100)
			.build();
		pressureLabel = new TextWidget(Text.literal("Pressure (In -> Out)"), textRenderer);
		sourcePressure = new TextFieldWidget(textRenderer, 100, 20, Text.literal("Input Pressure"));
		targetPressure = new TextFieldWidget(textRenderer, 100, 20, Text.literal("Output Pressure"));
		setNumericalInputCallbackFor(sourcePressure,
			pressure::getSelectedPoint,
			(p, d) -> new PressureMappingPoint(d.intValue(), p.targetPressure()),
			p -> {
				configuration.get().getPressureMapping().replace(pressure.getSelectedPoint(), p);
				pressure.setSelectedPoint(p);
			});
		setNumericalInputCallbackFor(targetPressure,
			pressure::getSelectedPoint,
			(p, d) -> new PressureMappingPoint(p.sourcePressure(), d.intValue()),
			p -> {
				configuration.get().getPressureMapping().replace(pressure.getSelectedPoint(), p);
				pressure.setSelectedPoint(p);
			});
		// @formatter:on
	}

	@Override
	public Text getTitle() { return Text.literal("Tablet & Pen"); }

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		header.forEachChild(consumer);

		consumer.accept(area);
		consumer.accept(letterboxingButton);
		consumer.accept(resetAreaButton);
		consumer.accept(positionLabel);
		consumer.accept(areaX);
		consumer.accept(areaY);
		consumer.accept(areaLabel);
		consumer.accept(areaWidth);
		consumer.accept(areaHeight);

		consumer.accept(pressure);
		consumer.accept(deleteNodeButton);
		consumer.accept(pressureLabel);
		consumer.accept(sourcePressure);
		consumer.accept(targetPressure);
	}

	public void updateWidgets() {
		ignoreTextFields = true;

		boolean isLetterboxing = configuration.get() != null && configuration.get().getAreaMapping().isLetterboxing();
		letterboxingButton.setMessage(Text.literal("Letterboxing: ")
			.append(Text.literal(isLetterboxing ? "On" : "Off")
				.styled(s -> s.withColor(isLetterboxing ? Formatting.GREEN : Formatting.RED))));

		letterboxingButton.active = configuration.get() != null;
		resetAreaButton.active = configuration.get() != null
			&& !configuration.get().getAreaMapping().getTabletArea().equals(new Rectangle(spec.get().getInputSize()));
		deleteNodeButton.active = configuration.get() != null && pressure.getSelectedPoint() != null;

		// Text fields
		if (configuration.get() != null) {
			areaX.active = areaY.active = areaWidth.active = areaHeight.active = true;
			sourcePressure.active = targetPressure.active = pressure.getSelectedPoint() != null;

			if (!inputtingMode) {
				DecimalFormat formatter = thinMode ? FORMATTER_THIN : FORMATTER_FULL;
				areaX.setText(formatter.format(configuration.get().getAreaMapping().getTabletArea().x()));
				areaY.setText(formatter.format(configuration.get().getAreaMapping().getTabletArea().y()));
				areaWidth.setText(formatter.format(configuration.get().getAreaMapping().getTabletArea().width()));
				areaHeight.setText(formatter.format(configuration.get().getAreaMapping().getTabletArea().height()));
				sourcePressure.setText(pressure.getSelectedPoint() != null
					? formatter.format(pressure.getSelectedPoint().sourcePressure())
					: "n/a");
				targetPressure.setText(pressure.getSelectedPoint() != null
					? formatter.format(pressure.getSelectedPoint().targetPressure())
					: "n/a");
			}
		} else {
			areaX.active = areaY.active = areaWidth.active = areaHeight.active = false;
			sourcePressure.active = targetPressure.active = false;
			areaX.setText("n/a");
			areaY.setText("n/a");
			areaWidth.setText("n/a");
			areaHeight.setText("n/a");
		}

		ignoreTextFields = false;
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		screenSize = new ConstantVector2(tabArea.width(), tabArea.height());
		thinMode = tabArea.width() < 600 || tabArea.height() < 400;
		int sidebarWidth = Math.min(tabArea.width() / 3, thinMode ? 100 : 120);
		int pad = thinMode ? 4 : 10;
		int hpad = pad / 2;

		header.refreshGrid(tabArea);

		// Area mapping
		area.setPosition(tabArea.getLeft() + pad, tabArea.getTop() + pad + 20 + hpad);
		area.setDimensions(tabArea.width() - pad * 2 - sidebarWidth, tabArea.height() / 5 * 3 - 20 - hpad);
		letterboxingButton.setPosition(area.getRight() + hpad, area.getY());
		letterboxingButton.setDimensions(sidebarWidth - hpad, 20);
		resetAreaButton.setPosition(area.getRight() + hpad, letterboxingButton.getBottom() + hpad);
		resetAreaButton.setDimensions(sidebarWidth - hpad, 20);
		positionLabel.setPosition(area.getRight() + hpad, resetAreaButton.getBottom() + hpad);
		areaX.setPosition(positionLabel.getX(), positionLabel.getBottom() + hpad);
		areaX.setDimensions(sidebarWidth / 2 - hpad, 20);
		areaY.setPosition(areaX.getRight() + hpad, areaX.getY());
		areaY.setDimensions(sidebarWidth / 2 - hpad, 20);
		areaLabel.setPosition(areaX.getX(), areaX.getBottom() + hpad);
		areaWidth.setPosition(areaLabel.getX(), areaLabel.getBottom() + hpad);
		areaWidth.setDimensions(sidebarWidth / 2 - hpad, 20);
		areaHeight.setPosition(areaWidth.getRight() + hpad, areaWidth.getY());
		areaHeight.setDimensions(sidebarWidth / 2 - hpad, 20);

		// Pressure mapping
		pressure.setPosition(area.getX(), area.getBottom() + hpad);
		pressure.setDimensions(area.getWidth(), tabArea.height() / 5 * 2 - pad * 2);
		deleteNodeButton.setPosition(pressure.getRight() + hpad, pressure.getY());
		deleteNodeButton.setDimensions(sidebarWidth, 20);
		pressureLabel.setPosition(deleteNodeButton.getX(), deleteNodeButton.getBottom() + hpad);
		sourcePressure.setPosition(pressureLabel.getX(), pressureLabel.getBottom() + hpad);
		sourcePressure.setDimensions(sidebarWidth / 2 - hpad, 20);
		targetPressure.setPosition(sourcePressure.getRight() + hpad, sourcePressure.getY());
		targetPressure.setDimensions(sidebarWidth / 2 - hpad, 20);

		updateWidgets();
	}

	private static boolean tryParseAndApply(String text, DoubleConsumer callback) {
		try {
			double d = Double.parseDouble(text);
			callback.accept(d);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	private <T> void setNumericalInputCallbackFor(TextFieldWidget field, Supplier<T> getter, BiFunction<T, Double, T> func, Consumer<T> setter) {
		field.setChangedListener(text -> {
			TabletConfiguration config = configuration.get();
			if (config == null) return;
			if (ignoreTextFields) return;
			inputtingMode = true;
			tryParseAndApply(text, d -> setter.accept(func.apply(getter.get(), d)));
			inputtingMode = false;
		});
	}
}
