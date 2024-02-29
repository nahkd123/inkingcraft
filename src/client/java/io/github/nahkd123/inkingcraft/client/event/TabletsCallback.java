package io.github.nahkd123.inkingcraft.client.event;

import io.github.nahkd123.inking.api.TabletDriver;
import io.github.nahkd123.inking.api.tablet.Tablet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

@FunctionalInterface
public interface TabletsCallback {
	/**
	 * <p>
	 * Fabric-friendly version of {@link TabletDriver#getTabletDiscoverEmitter()}.
	 * Call the listeners when a new tablet is discovered. In other words, it will
	 * emit the tablet if the tablet's unique ID hasn't been seen before by the
	 * driver in its entire lifetime before.
	 * </p>
	 * <p>
	 * <b>Threading</b>: This will be called by driver thread. You can use
	 * {@link MinecraftClient#executeSync(Runnable)} to execute on render thread.
	 * </p>
	 */
	public static Event<TabletsCallback> DISCOVERED = EventFactory.createArrayBacked(TabletsCallback.class,
		listeners -> tablet -> {
			for (TabletsCallback listener : listeners) listener.callback(tablet);
		});

	/**
	 * <p>
	 * Fabric-friendly version of {@link TabletDriver#getTabletConnectEmitter()}.
	 * Call the listeners when a tablet is connected, regradless if the driver had
	 * seen it before.
	 * </p>
	 * <p>
	 * <b>Threading</b>: Depends on how the tablet is connected and how the driver
	 * handles input, it will be called by driver thread OR tablet input thread. You
	 * can use {@link MinecraftClient#executeSync(Runnable)} to execute on render
	 * thread.
	 * </p>
	 */
	public static Event<TabletsCallback> CONNECTED = EventFactory.createArrayBacked(TabletsCallback.class,
		listeners -> tablet -> {
			for (TabletsCallback listener : listeners) listener.callback(tablet);
		});

	/**
	 * <p>
	 * Fabric-friendly version of {@link TabletDriver#getTabletDisconnectEmitter()}.
	 * Call the listeners when a tablet is disconnected.
	 * </p>
	 * <p>
	 * <b>Threading</b>: Depends on how the driver handles input, it will be called
	 * by driver thread OR tablet input thread. You can use
	 * {@link MinecraftClient#executeSync(Runnable)} to execute on render thread.
	 * </p>
	 */
	public static Event<TabletsCallback> DISCONNECTED = EventFactory.createArrayBacked(TabletsCallback.class,
		listeners -> tablet -> {
			for (TabletsCallback listener : listeners) listener.callback(tablet);
		});

	public void callback(Tablet tablet);
}
