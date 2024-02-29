package io.github.nahkd123.inkingcraft.client.gui.widget;

import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.Tablet;
import net.minecraft.client.MinecraftClient;

/**
 * <p>
 * An interface for all widgets that accepts inputs coming from graphics tablet.
 * </p>
 */
public interface TabletElement {
	/**
	 * <p>
	 * Called <b>inside input thread</b> when this widget received tablet input.
	 * </p>
	 * 
	 * @param tablet   The tablet.
	 * @param raw      The raw packet data.
	 * @param penX     X position in widget.
	 * @param penY     Y position in widget.
	 * @param pressure Pen pressure.
	 */
	default void tabletInputtedAsync(Tablet tablet, Packet raw, double penX, double penY, double pressure) {
		MinecraftClient.getInstance().execute(() -> tabletInputted(tablet, raw, penX, penY, pressure));
	}

	/**
	 * <p>
	 * Called when this widget received tablet input. This will always be called in
	 * render thread.
	 * </p>
	 * 
	 * @param tablet   The tablet.
	 * @param raw      The raw packet data.
	 * @param penX     X position in widget.
	 * @param penY     Y position in widget.
	 * @param pressure Pen pressure.
	 */
	default void tabletInputted(Tablet tablet, Packet raw, double penX, double penY, double pressure) {}
}
