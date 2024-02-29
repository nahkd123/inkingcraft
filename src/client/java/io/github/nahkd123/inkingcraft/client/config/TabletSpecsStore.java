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

import io.github.nahkd123.inking.api.TabletDriver;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inkingcraft.InkingCraft;

public class TabletSpecsStore {
	private static final Pattern SPECIAL_SYMBOLS = Pattern.compile("[^A-Za-z0-9-_./]"); // Replace with 2 underscores
	private TabletDriver driver;
	private Path storeDir;
	private Map<String, OfflineTabletSpec> allSpecs = new ConcurrentHashMap<>();

	public TabletSpecsStore(TabletDriver driver, Path storeDir) {
		this.driver = driver;
		this.storeDir = storeDir;
		load(storeDir);
	}

	public TabletDriver getDriver() { return driver; }

	public Path getStoreDir() { return storeDir; }

	private static String tabletIdToFilename(String id) {
		id = id.replace(':', '/');
		return SPECIAL_SYMBOLS.matcher(id).replaceAll("__") + ".json";
	}

	private Path getPathForId(String tabletId) {
		String filename = tabletIdToFilename(tabletId);
		return storeDir.resolve(filename);
	}

	private void load(Path path) {
		try {
			if (!Files.exists(path)) return;

			if (Files.isDirectory(path)) {
				Files.list(path).forEach(child -> load(child));
				return;
			}

			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				JsonElement json = JsonParser.parseReader(reader);
				DataResult<OfflineTabletSpec> result = OfflineTabletSpec.CODEC.parse(JsonOps.INSTANCE, json);
				Optional<OfflineTabletSpec> spec = result
					.resultOrPartial(msg -> InkingCraft.LOGGER.warn("{}: {}", path, msg));
				if (spec.isPresent()) allSpecs.put(spec.get().tabletId(), spec.get());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerIfAbsent(String tabletId, TabletSpec spec) {
		if (allSpecs.containsKey(tabletId)) return;
		OfflineTabletSpec snapshot = new OfflineTabletSpec(tabletId, spec);
		allSpecs.put(tabletId, snapshot);

		try {
			Path path = getPathForId(tabletId);

			if (Files.exists(path)) {
				load(path);
				return;
			}

			if (!Files.exists(path.resolve(".."))) Files.createDirectories(path.resolve(".."));

			DataResult<JsonElement> result = OfflineTabletSpec.CODEC.encodeStart(JsonOps.INSTANCE, snapshot);
			Optional<JsonElement> json = result
				.resultOrPartial(msg -> InkingCraft.LOGGER.warn("{}: {}", tabletId, msg));
			if (json.isEmpty()) return;

			String jsonText = new GsonBuilder()
				.disableHtmlEscaping()
				.setPrettyPrinting()
				.create().toJson(json.get());
			Files.writeString(path, jsonText, StandardCharsets.UTF_8);
			InkingCraft.LOGGER.info("Registered specifications for {} ({})!", spec.getTabletName(), tabletId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, OfflineTabletSpec> getAllSpecifications() { return Collections.unmodifiableMap(allSpecs); }
}
