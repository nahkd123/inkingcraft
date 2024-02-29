package io.github.nahkd123.inkingcraft.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.nahkd123.inkingcraft.client.gui.config.InkingSettingsScreen;

public class InkingCraftModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() { return InkingSettingsScreen::new; }
}
