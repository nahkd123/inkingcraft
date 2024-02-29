package io.github.nahkd123.inkingcraft.client.event;

import io.github.nahkd123.inking.api.tablet.Packet;
import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public final class TabletPacketsCallback {
	/**
	 * <p>
	 * Fabric-friendly version of {@link Tablet#getPacketsEmitter()}. This will
	 * emits packets and {@link Tablet}, which is where the packet is originated
	 * from. <b>This will be emitted in driver thread (or tablet input thread)!</b>
	 * Use {@link MinecraftClient#execute(Runnable)} to execute your code in render
	 * thread.
	 * </p>
	 * <p>
	 * <b>Packet data</b>: All packets emitted from this event are
	 * <i>unfiltered</i>, which means transformations are not applied on packets. In
	 * other words, packets that you'll receive from this event are coming directly
	 * from the tablet. Disabling the tablet from {@link TabletConfiguration} will
	 * not stop this event from emitting.
	 * </p>
	 */
	public static final Event<UnfilteredCallback> UNFILTERED = EventFactory.createArrayBacked(UnfilteredCallback.class,
		listeners -> (tablet, packet) -> {
			for (UnfilteredCallback listener : listeners) listener.onPacket(tablet, packet);
		});

	@FunctionalInterface
	public static interface UnfilteredCallback {
		void onPacket(Tablet tablet, Packet packet);
	}

	/**
	 * <p>
	 * Filtered packets event. This will not emit if the tablet is disabled from
	 * {@link TabletConfiguration}. The pen position on the screen and pen pressure
	 * are mapped from {@link TabletConfiguration}.<b>This will be emitted in driver
	 * thread (or tablet input thread)!</b> Use
	 * {@link MinecraftClient#execute(Runnable)} to execute your code in render
	 * thread.
	 * </p>
	 */
	public static final Event<FilteredCallback> FILTERED = EventFactory.createArrayBacked(FilteredCallback.class,
		listeners -> (tablet, packet, screenX, screenY, pressure) -> {
			for (FilteredCallback listener : listeners)
				listener.onFilteredPacket(tablet, packet, screenX, screenY, pressure);
		});

	@FunctionalInterface
	public static interface FilteredCallback {
		void onFilteredPacket(Tablet tablet, Packet packet, double screenX, double screenY, double pressure);
	}
}
