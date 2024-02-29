package io.github.nahkd123.inkingcraft.client.gui.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.config.ConfigurationsStore;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletSpecsStore;
import io.github.nahkd123.inkingcraft.client.utils.Rectangle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TabletsConfigurationScreen extends Screen {
	private Screen parent;
	private ConfigurationsStore store;
	private TabletSpecsStore specs;

	// Current
	private List<String> tabletIds = new ArrayList<>();
	private TabletSpec currentSpec;
	private TabletConfiguration currentConfig;
	private Text currentName;

	// Widgets
	private List<ClickablesCollection> tabs = new ArrayList<>();
	private List<ButtonWidget> tabButtons = new ArrayList<>();

	private Map<Integer, ColorChipWidget> colorChips;
	private ButtonWidget enabled;
	private ButtonWidget interactUI;
	private ButtonWidget letterboxing;
	private ButtonWidget resetArea;

	private PressureMappingWidget pressureMapping;
	private ButtonWidget removeNode;

	public TabletsConfigurationScreen(Screen parent, ConfigurationsStore store, TabletSpecsStore specs) {
		super(Text.literal("Configure Tablets"));
		this.parent = parent;
		this.store = store;
		this.specs = specs;
		tabletIds.addAll(store.getConfigurations().keySet());
	}

	public List<String> getTabletIds() { return tabletIds; }

	public void openConfigFor(TabletConfiguration config) {
		currentConfig = config;

		if (config != null) {
			currentSpec = specs.getAllSpecifications().get(config.getTabletId());
			currentName = Text.literal(currentSpec.getTabletName());
		} else {
			currentSpec = null;
			currentName = Text.literal("(no tablets selected)").styled(s -> s.withItalic(true));
		}

		updateWidgets();
	}

	public void updateWidgets() {
		enabled.setMessage(Text.literal("Enabled: "
			+ ((currentConfig != null && currentConfig.isEnabled()) ? "On" : "Off")));
		interactUI.setMessage(Text.literal("Interact UI: "
			+ ((currentConfig != null && currentConfig.isInteractUI()) ? "On" : "Off")));
		letterboxing.setMessage(Text.literal("Letterboxing: "
			+ ((currentConfig != null && currentConfig.getAreaMapping().isLetterboxing()) ? "On" : "Off")));

		enabled.active = interactUI.active = letterboxing.active = resetArea.active = currentConfig != null;
		removeNode.active = currentConfig != null && pressureMapping.getSelectedPoint() != null;
		colorChips.values().forEach(chip -> chip.active = currentConfig != null);

		// TODO temporary disable interactUI here because it hasn't been implemented yet
		interactUI.active = false;
	}

	public void onNewTablet(Tablet tablet) {
		tabletIds.add(tablet.getTabletId());
		if (currentConfig == null) openConfigFor(store.get(tablet));
	}

	public ClickablesCollection addTab(Text label, Consumer<Consumer<ClickableWidget>> builder) {
		int tabId = tabs.size();

		tabButtons.add(addDrawableChild(ButtonWidget.builder(label, button -> {
			ClickablesCollection tab = tabs.get(tabId);
			tabs.forEach(t -> t.makeAllInvisible());
			tab.makeAllVisible();

			for (ButtonWidget btn : tabButtons) btn.active = true;
			button.active = false;
		}).build()));

		ClickablesCollection coll = new ClickablesCollection();
		tabs.add(coll);
		builder.accept(coll.getWidgets()::add);

		// Update all tabs
		int tabsWidth = width - 100;
		int tabsMargin = 5;
		int tabWidth = (tabsWidth + tabsMargin * 2) / tabButtons.size();

		for (int i = 0; i < tabButtons.size(); i++) {
			ButtonWidget btn = tabButtons.get(i);
			btn.setPosition(50 + tabWidth * i, 45);
			btn.setDimensions(tabWidth - tabsMargin * 2, 20);
		}

		return coll;
	}

	@Override
	protected void init() {
		tabs.clear();
		tabButtons.clear();
		colorChips = new HashMap<>();

		addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
			int idx = currentConfig != null ? tabletIds.indexOf(currentConfig.getTabletId()) : -1;

			if (idx == -1) {
				if (tabletIds.size() > 0) openConfigFor(store.getConfigurations().get(tabletIds.get(0)));
				return;
			}

			idx--;
			if (idx <= 0) idx = tabletIds.size() - 1;
			openConfigFor(store.getConfigurations().get(tabletIds.get(idx)));
		}).dimensions((width - 150 - 20) / 2, 15, 20, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
			int idx = currentConfig != null ? tabletIds.indexOf(currentConfig.getTabletId()) : -1;

			if (idx == -1) {
				if (tabletIds.size() > 0) openConfigFor(store.getConfigurations().get(tabletIds.get(0)));
				return;
			}

			idx++;
			if (idx >= tabletIds.size()) idx = 0;
			openConfigFor(store.getConfigurations().get(tabletIds.get(idx)));
		}).dimensions((width + 150 - 20) / 2, 15, 20, 20).build());

		for (Entry<String, Integer> pair : Map.of(
			"White", 0xFFFFFF,
			"Red", 0xFF7F7F,
			"Green", 0x7FFF7F,
			"Blue", 0x7F7FFF,
			"Yellow", 0xFFFF7F,
			"Aqua", 0x7FFFFF,
			"Pink", 0xFF7FFF).entrySet()) {
			int chipW = width / 4 > 25 * 7 ? 20 : 7;
			int chipPad = width / 4 > 25 * 7 ? 5 : 0;
			int chipX = width - 50 + chipPad - (colorChips.size() + 1) * (chipW + chipPad);

			ColorChipWidget chip = addDrawableChild(
				new ColorChipWidget(chipX, 15, chipW, 20, Text.literal(pair.getKey()), pair.getValue(), button -> {
					if (currentConfig == null) return;
					if (currentConfig.getPointerColor() == pair.getValue()) return;
					currentConfig.setPointerColor(pair.getValue());
					store.save(currentConfig);
				}));
			chip.setTooltip(Tooltip.of(Text.literal("Set pointer color to ")
				.append(Text.literal(pair.getKey()).styled(s -> s.withColor(pair.getValue())))));
			colorChips.put(pair.getValue(), chip);
		}

		addTab(Text.literal("Area"), add -> {
			// @formatter:off
			add.accept(addDrawableChild(new AreaMappingWidget(
				50, 45 + 25, width - 205, height - 45 - 35 - 25,
				() -> currentConfig != null ? currentConfig.getAreaMapping() : null,
				() -> currentSpec,
				() -> currentConfig.getAreaMapping().isLetterboxing()
					? new ConstantVector2(width, height)
					: currentConfig.getAreaMapping().getTabletArea().getSize(),
				() -> store.save(currentConfig))));
			// @formatter:on

			add.accept(
				letterboxing = addDrawableChild(ButtonWidget.builder(Text.literal("Letterboxing: Off"), button -> {
					if (currentConfig == null) return;
					currentConfig.getAreaMapping().setLetterboxing(!currentConfig.getAreaMapping().isLetterboxing());
					updateWidgets();
					store.save(currentConfig);
				})
					.dimensions(width - 150, 45 + 25 * 4, 100, 20)
					.tooltip(Tooltip.of(Text.literal("Maintain the screen's aspect ratio inside mapped area.")))
					.build()));

			add.accept(resetArea = addDrawableChild(ButtonWidget.builder(Text.literal("Reset Area"), button -> {
				Vector2 area = currentSpec.getInputSize();
				currentConfig.getAreaMapping().setTabletArea(new Rectangle(ConstantVector2.ZERO, area));
				store.save(currentConfig);
			})
				.dimensions(width - 150, 45 + 25 * 5, 100, 20)
				.tooltip(Tooltip.of(Text.literal("Reset to full area")))
				.build()));

			// TODO text fields
		}).makeAllVisible();

		addTab(Text.literal("Pressure"), add -> {
			// @formatter:off
			add.accept(pressureMapping = addDrawableChild(new PressureMappingWidget(
				50, 45 + 25, width - 205, height - 45 - 35 - 25,
				() -> currentConfig != null? currentConfig.getPressureMapping() : null,
				() -> currentSpec,
				() -> {
					store.save(currentConfig);
					updateWidgets();
				})));
			// @formatter:on

			add.accept(
				removeNode = addDrawableChild(ButtonWidget.builder(Text.literal("Remove Node"), button -> {
					if (currentConfig == null) return;
					if (pressureMapping.getSelectedPoint() == null) return;
					currentConfig.getPressureMapping().remove(pressureMapping.getSelectedPoint());
					pressureMapping.setSelectedPoint(null);
					updateWidgets();
					store.save(currentConfig);
				})
					.dimensions(width - 150, 45 + 25 * 4, 100, 20)
					.tooltip(Tooltip.of(Text.literal("Remove selected node.")))
					.build()));
		}).makeAllInvisible();

		addTab(Text.literal("Binding"), add -> {}).makeAllInvisible();

		addTab(Text.literal("Misc"), add -> {}).makeAllInvisible();

		// Sidebar
		initMainSidebar();

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
			client.setScreen(parent);
		}).dimensions(width / 2 - 100, height - 30, 200, 20).build());

		// Post init
		openConfigFor(tabletIds.size() > 0 ? store.getConfigurations().get(tabletIds.get(0)) : null);
		tabButtons.get(0).active = false;
	}

	private void initMainSidebar() {
		enabled = addDrawableChild(ButtonWidget.builder(Text.literal("Enabled: Off"), button -> {
			if (currentConfig == null) return;
			currentConfig.setEnable(!currentConfig.isEnabled());
			updateWidgets();
			store.save(currentConfig);
		})
			.dimensions(width - 150, 45 + 25, 100, 20)
			.tooltip(Tooltip.of(Text.literal("Enable/disable this tablet. If disabled, InkingCraft "
				+ "will ignore inputs from this tablet. Other mods can still read inputs as usual.")))
			.build());

		interactUI = addDrawableChild(ButtonWidget.builder(Text.literal("Interact UI: Off"), button -> {
			if (currentConfig == null) return;
			currentConfig.setInteractUI(!currentConfig.isInteractUI());
			updateWidgets();
			store.save(currentConfig);
		})
			.dimensions(width - 150, 45 + 25 * 2, 100, 20)
			.tooltip(Tooltip.of(Text.literal("Allows this tablet to interact with game UI like when you use "
				+ "mouse. Implementation temporary unavailable at this moment.")))
			.build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Not detected?"), button -> {
			ConfirmLinkScreen.open(this, "https://opentabletdriver.net/Wiki/FAQ/General#supported-tablets");
		})
			.dimensions(width - 150, 45 + 25 * 3, 100, 20)
			.tooltip(Tooltip.of(Text.literal("Inking handles tablet inputs using OpenTabletDriver. "
				+ "Click for more information.")))
			.build());
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
		context.drawCenteredTextWithShadow(
			textRenderer,
			currentName,
			width / 2, 15 + textRenderer.fontHeight + 2,
			0xFFFFFF);

		context.drawText(textRenderer, "InkingCraft", 50, 15, 0x7F7F7F, false);
		context.drawText(textRenderer, "by nahkd123", 50, 15 + textRenderer.fontHeight + 2, 0x5F5F5F, false);
	}
}
