package io.github.nahkd123.inkingcraft;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class InkingCraft implements ModInitializer {
	private static final String MODID = "inkingcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {}

	public static Path getConfigFolder() { return FabricLoader.getInstance().getConfigDir().resolve(MODID); }

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}