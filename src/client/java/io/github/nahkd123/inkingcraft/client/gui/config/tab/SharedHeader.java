package io.github.nahkd123.inkingcraft.client.gui.config.tab;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.gui.widget.TabletStatusWidget;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

/**
 * <p>
 * A tab header that is shared across all tabs.
 * </p>
 */
public class SharedHeader {
	public TabletStatusWidget status;
	public ButtonWidget previousButton, nextButton, notDetectedButton, closeButton;

	public SharedHeader(Screen parent, Supplier<TabletConfiguration> configuration, Runnable previousTablet, Runnable nextTablet) {
		// @formatter:off
		status = new TabletStatusWidget(0, 0, 0, 0,
			Text.literal("Tablet Status"),
			() -> configuration.get() != null ? configuration.get().getTabletId() : null);
		previousButton = ButtonWidget.builder(Text.literal("<"), button -> previousTablet.run())
			.tooltip(Tooltip.of(Text.literal("Open configuration for previous tablet.")))
			.width(20)
			.build();
		nextButton = ButtonWidget.builder(Text.literal(">"), button -> nextTablet.run())
			.tooltip(Tooltip.of(Text.literal("Open configuration for next tablet.")))
			.width(20)
			.build();
		notDetectedButton = ButtonWidget.builder(Text.literal("Not Detected?"), button -> {
			ConfirmLinkScreen.open(parent, "https://opentabletdriver.net/Wiki/FAQ/General#supported-tablets");
		})
			.tooltip(Tooltip.of(Text.literal("InkingCraft uses OpenTabletDriver under the hood. "
				+ "Visit OpenTabletDriver website to find out why your tablet is not supported.")))
			.width(100)
			.build();
		closeButton = ButtonWidget.builder(Text.literal("Done"), button -> parent.close()).width(50).build();
		// @formatter:on
	}

	public void forEachChild(Consumer<ClickableWidget> iterator) {
		iterator.accept(status);
		iterator.accept(previousButton);
		iterator.accept(nextButton);
		iterator.accept(notDetectedButton);
		iterator.accept(closeButton);
	}

	public void refreshGrid(ScreenRect tabArea) {
		boolean thinMode = tabArea.width() < 600;
		int pad = thinMode ? 4 : 10;
		int hpad = pad / 2;

		previousButton.setPosition(tabArea.getLeft() + pad, tabArea.getTop() + pad);
		nextButton.setPosition(previousButton.getRight() + hpad, previousButton.getY());
		status.setPosition(nextButton.getRight() + 5, nextButton.getY());
		closeButton.setPosition(tabArea.getRight() - pad - closeButton.getWidth(), nextButton.getY());
		notDetectedButton.setPosition(closeButton.getX() - notDetectedButton.getWidth() - hpad, closeButton.getY());
	}
}
