package io.github.nahkd123.inkingcraft.client.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.InkingCraft;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigurationsStore {
	private static final Pattern SPECIAL_SYMBOLS = Pattern.compile("[^A-Za-z0-9-_./]");
	private Map<String, TabletConfiguration> configurations = new ConcurrentHashMap<>();
	private Path storeDir;

	public ConfigurationsStore(Path storeDir) {
		this.storeDir = storeDir;
		load(storeDir);
	}

	public Path getStoreDir() { return storeDir; }

	public void putIfAbsent(TabletConfiguration configuration) {
		if (configurations.containsKey(configuration.getTabletId())) return;
		configurations.put(configuration.getTabletId(), configuration);
		save(configuration);
	}

	public void save(TabletConfiguration configuration) {
		try {
			String tabletId = configuration.getTabletId();
			Path path = getPathForId(tabletId);
			if (!Files.exists(path.resolve(".."))) Files.createDirectories(path.resolve(".."));

			DataResult<JsonElement> result = TabletConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, configuration);
			Optional<JsonElement> json = result
				.resultOrPartial(msg -> InkingCraft.LOGGER.warn("{}: {}", tabletId, msg));
			if (json.isEmpty()) return;

			String jsonText = new GsonBuilder()
				.disableHtmlEscaping()
				.setPrettyPrinting()
				.create().toJson(json.get());
			Files.writeString(path, jsonText, StandardCharsets.UTF_8);

			// Initially I wanted to log every time a configuration is saved
			// However, subsequence edits in configuration screen spams the log file real
			// fast.
			if (FabricLoader.getInstance().isDevelopmentEnvironment())
				InkingCraft.LOGGER.info("Saved configurations for {}!", tabletId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load(Path path) {
		try {
			if (!Files.exists(path)) return;

			if (Files.isDirectory(path)) {
				Files.list(path).forEach(child -> load(child));
				return;
			}

			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				JsonElement json = JsonParser.parseReader(reader);
				DataResult<TabletConfiguration> result = TabletConfiguration.CODEC.parse(JsonOps.INSTANCE, json);
				Optional<TabletConfiguration> config = result
					.resultOrPartial(msg -> InkingCraft.LOGGER.warn("{}: {}", path, msg));
				if (config.isPresent()) configurations.put(config.get().getTabletId(), config.get());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String tabletIdToFilename(String id) {
		id = id.replace(':', '/');
		return SPECIAL_SYMBOLS.matcher(id).replaceAll("__") + ".json";
	}

	private Path getPathForId(String tabletId) {
		String filename = tabletIdToFilename(tabletId);
		return storeDir.resolve(filename);
	}

	public TabletConfiguration get(Tablet tablet) {
		TabletConfiguration configuration = configurations.get(tablet.getTabletId());

		if (configuration == null) {
			configuration = TabletConfiguration.createDefault(tablet);
			putIfAbsent(configuration);
		}

		return configuration;
	}

	public Map<String, TabletConfiguration> getConfigurations() { return Collections.unmodifiableMap(configurations); }
}
