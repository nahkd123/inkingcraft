package io.github.nahkd123.inkingcraft.client;

import static io.github.nahkd123.inkingcraft.InkingCraft.LOGGER;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.github.nahkd123.inking.api.TabletDriversCollection;
import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inking.otd.OpenTabletDriver;
import io.github.nahkd123.inking.otd.netnative.OtdNative;
import io.github.nahkd123.inkingcraft.InkingCraft;
import io.github.nahkd123.inkingcraft.client.config.ConfigurationsStore;
import io.github.nahkd123.inkingcraft.client.config.InkingConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletConfiguration;
import io.github.nahkd123.inkingcraft.client.config.TabletSpecsStore;
import io.github.nahkd123.inkingcraft.client.event.TabletPacketsCallback;
import io.github.nahkd123.inkingcraft.client.event.TabletsCallback;
import io.github.nahkd123.inkingcraft.client.gui.widget.TabletElement;
import io.github.nahkd123.inkingcraft.client.input.FilteredPacketData;
import io.github.nahkd123.inkingcraft.client.input.InkingInputManager;
import io.github.nahkd123.inkingcraft.client.utils.XYConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * <p>
 * Main client-side mod initializer for InkingCraft. The only method that might
 * be useful to you is {@link #getDrivers()}, which returns a drivers
 * collection. See the "See also" section for more stuffs you can use.
 * </p>
 * 
 * @see TabletsCallback#DISCOVERED
 * @see TabletsCallback#CONNECTED
 * @see TabletsCallback#DISCONNECTED
 * @see TabletPacketsCallback#UNFILTERED
 */
public class InkingCraftClient implements ClientModInitializer {
	private static InkingConfiguration globalConfig;
	private static TabletDriversCollection drivers;
	private static TabletSpecsStore specStore;
	private static ConfigurationsStore configStore;
	private static InkingInputManager inputManager;

	@Override
	public void onInitializeClient() {
		// Init
		initializeDriverEvents();
		initializeTabletInput();
		loadGlobalConfig();
		specStore = new TabletSpecsStore(drivers, InkingCraft.getConfigFolder().resolve("specifications"));
		configStore = new ConfigurationsStore(InkingCraft.getConfigFolder().resolve("configurations"));
		inputManager = new InkingInputManager();

		// We only load drivers when the client is started
		// This allows other mods to register events listener
		ClientLifecycleEvents.CLIENT_STARTED.register($ -> {
			Path modDataDir = InkingCraft.getConfigFolder();
			Linker linker = Linker.nativeLinker();
			Arena arena = Arena.ofAuto();

			LOGGER.info("InkingCraft data folder is {}", modDataDir);
			LOGGER.info("Loaded {} existing tablet specifications!", specStore.getAllSpecifications().size());
			LOGGER.info("Loaded {} existing tablet configurations!", configStore.getConfigurations().size());
			LOGGER.info("Loading tablet drivers...");
			drivers.addDriver(new OpenTabletDriver(OtdNative.findNative(modDataDir, linker, arena)));
			LOGGER.info("Loaded {} tablet drivers!", drivers.getAllDrivers().size());
		});

		// Logging only
		initializeVerboseLogging();
	}

	private void initializeVerboseLogging() {
		TabletsCallback.DISCOVERED.register(tablet -> {
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

			TabletSpec spec = tablet.getSpec();
			LOGGER.info("A new tablet has been discovered!");
			LOGGER.info("  Tablet name: {}", spec.getTabletName());
			LOGGER.info("  Unique ID:   {}", tablet.getTabletId());
			LOGGER.info("  Digitizer:   {}mm x {}mm, input window {} x {}, max pressure {}",
				spec.getPhysicalSize().x(), spec.getPhysicalSize().y(),
				spec.getInputSize().x(), spec.getInputSize().y(),
				spec.getMaxPressure());
			LOGGER.info("  Buttons:     {} pen, {} tablet",
				spec.getButtonsCount(ButtonType.PEN),
				spec.getButtonsCount(ButtonType.AUXILIARY));
		});

		TabletsCallback.CONNECTED.register(tablet -> {
			LOGGER.info("Tablet {} has been connected!", tablet.getSpec().getTabletName());
		});

		TabletsCallback.DISCONNECTED.register(tablet -> {
			LOGGER.info("Tablet {} has been disconnected!", tablet.getSpec().getTabletName());
		});
	}

	private void initializeDriverEvents() {
		drivers = new TabletDriversCollection();

		drivers.getTabletDiscoverEmitter().listen(tablet -> {
			specStore.registerIfAbsent(tablet.getTabletId(), tablet.getSpec());
			configStore.get(tablet);
			TabletsCallback.DISCOVERED.invoker().callback(tablet);
		});

		drivers.getTabletConnectEmitter().listen(tablet -> TabletsCallback.CONNECTED.invoker().callback(tablet));
		drivers.getTabletDisconnectEmitter().listen(tablet -> TabletsCallback.DISCONNECTED.invoker().callback(tablet));
		drivers.getTabletDiscoverEmitter().listen(tablet -> {
			tablet
				.getPacketsEmitter()
				.listen(packet -> TabletPacketsCallback.UNFILTERED.invoker().onPacket(tablet, packet));
		});
	}

	private void initializeTabletInput() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			TabletPacketsCallback.UNFILTERED.register((tablet, packet) -> {
				TabletConfiguration config = configStore.get(tablet);
				if (!config.isEnabled()) return;

				Vector2 screenSize = new ConstantVector2(client.getWindow().getWidth(), client.getWindow().getHeight());
				double[] screenXY = new double[2];
				config.getAreaMapping().map(packet.getPenPosition(), screenSize, XYConsumer.forArray(screenXY, 0));
				double maxPressure = tablet.getSpec().getMaxPressure();
				double pressure = config.getPressureMapping().map(packet.getRawPressure()) / maxPressure;

				inputManager.get(tablet).setRawPacket(packet);
				TabletPacketsCallback.FILTERED.invoker().onFilteredPacket(
					tablet, packet,
					screenXY[0], screenXY[1], pressure);
			});

			TabletPacketsCallback.FILTERED.register((tablet, packet, screenX, screenY, pressure) -> {
				FilteredPacketData filtered = new FilteredPacketData(packet.getTimestamp(), screenX, screenY, pressure);
				inputManager.get(tablet).setFilteredPacket(filtered);

				if (client.currentScreen != null) {
					TabletElement elem = (TabletElement) client.currentScreen;
					elem.tabletInputtedAsync(tablet, packet,
						screenX / client.getWindow().getScaleFactor(),
						screenY / client.getWindow().getScaleFactor(),
						pressure);
				}
			});
		});
	}

	public static InkingConfiguration getGlobalConfig() { return globalConfig; }

	private static void loadGlobalConfig() {
		Path path = InkingCraft.getConfigFolder().resolve("global.json");

		if (!Files.exists(path)) {
			globalConfig = new InkingConfiguration();
			saveGlobalConfig();
		}

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement json = JsonParser.parseReader(reader);
			var result = InkingConfiguration.CODEC.parse(JsonOps.INSTANCE, json);
			Optional<InkingConfiguration> opt = result.resultOrPartial(msg -> LOGGER.warn("{}: {}", path, msg));
			globalConfig = opt.isPresent() ? opt.get() : new InkingConfiguration();
		} catch (IOException e) {
			e.printStackTrace();
			globalConfig = new InkingConfiguration();
		}
	}

	public static void saveGlobalConfig() {
		try {
			Path path = InkingCraft.getConfigFolder().resolve("global.json");
			if (!Files.exists(path.resolve(".."))) Files.createDirectories(path.resolve(".."));
			DataResult<JsonElement> result = InkingConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, globalConfig);
			Optional<JsonElement> opt = result.resultOrPartial(msg -> LOGGER.warn("{}: {}", path, msg));
			if (opt.isEmpty()) return;

			String jsonText = new GsonBuilder()
				.disableHtmlEscaping()
				.setPrettyPrinting()
				.create().toJson(opt.get());

			Files.writeString(path, jsonText, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Get tablet drivers collection. This contains all tablet drivers.
	 * </p>
	 * 
	 * @return Tablet drivers collection.
	 */
	public static TabletDriversCollection getDrivers() { return drivers; }

	public static TabletSpecsStore getSpecificationsStore() { return specStore; }

	public static ConfigurationsStore getConfigStore() { return configStore; }

	public static InkingInputManager getInputManager() { return inputManager; }
}