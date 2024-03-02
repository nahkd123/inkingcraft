package io.github.nahkd123.inkingcraft.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.client.config.ConfigurationsStore;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletSpecsStore;
import io.github.nahkd123.inkingcraft.client.gui.config.tab.BindingTab;
import io.github.nahkd123.inkingcraft.client.gui.config.tab.MiscTab;
import io.github.nahkd123.inkingcraft.client.gui.config.tab.TabletAndPenTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.text.Text;

public class TabletsConfigurationScreen extends Screen {
	private Screen parent;
	private ConfigurationsStore store;
	private TabletSpecsStore specs;

	// Current
	private List<String> tabletIds = new ArrayList<>();
	private TabletSpec currentSpec;
	private TabletConfiguration currentConfig;

	// Tabs
	private TabManager tabManager = new TabManager(this::addDrawableChild, child -> remove(child));
	private TabletAndPenTab tabletAndPen;

	public TabletsConfigurationScreen(Screen parent, ConfigurationsStore store, TabletSpecsStore specs) {
		super(Text.literal("Configure Tablets"));
		this.parent = parent;
		this.store = store;
		this.specs = specs;
		tabletIds.addAll(store.getConfigurations().keySet());
		if (tabletIds.size() > 0) openConfigFor(store.getConfigurations().get(tabletIds.get(0)));
	}

	public List<String> getTabletIds() { return tabletIds; }

	public void openConfigFor(TabletConfiguration config) {
		currentConfig = config;
		currentSpec = config != null ? specs.getAllSpecifications().get(config.getTabletId()) : null;
	}

	public void updateWidgets() {
		if (tabletAndPen != null) tabletAndPen.updateWidgets();
	}

	public void onNewTablet(Tablet tablet) {
		tabletIds.add(tablet.getTabletId());
		if (currentConfig == null) openConfigFor(store.get(tablet));
	}

	@Override
	protected void init() {
		// @formatter:off
		TabNavigationWidget tabNav = addDrawableChild(TabNavigationWidget.builder(tabManager, width)
			.tabs(
				tabletAndPen = new TabletAndPenTab(this,
					() -> currentConfig,
					() -> currentSpec,
					() -> store.save(currentConfig),
					this::previousTablet,
					this::nextTablet),
				new BindingTab(this,
					() -> currentConfig,
					this::previousTablet,
					this::nextTablet),
				new MiscTab(this,
					() -> currentConfig,
					this::previousTablet,
					this::nextTablet))
			.build());
		// @formatter:on

		tabNav.selectTab(0, false);
		tabNav.init();
		tabManager.setTabArea(new ScreenRect(0, 24, width, height - 24));
		updateWidgets();
	}

	private void previousTablet() {
		int idx = currentConfig != null ? tabletIds.indexOf(currentConfig.getTabletId()) : -1;

		if (idx == -1) {
			if (tabletIds.size() > 0) openConfigFor(store.getConfigurations().get(tabletIds.get(0)));
			return;
		}

		idx--;
		if (idx < 0) idx = tabletIds.size() - 1;
		openConfigFor(store.getConfigurations().get(tabletIds.get(idx)));
	}

	private void nextTablet() {
		int idx = currentConfig != null ? tabletIds.indexOf(currentConfig.getTabletId()) : -1;

		if (idx == -1) {
			if (tabletIds.size() > 0) openConfigFor(store.getConfigurations().get(tabletIds.get(0)));
			return;
		}

		idx++;
		if (idx >= tabletIds.size()) idx = 0;
		openConfigFor(store.getConfigurations().get(tabletIds.get(idx)));
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
	}
}
