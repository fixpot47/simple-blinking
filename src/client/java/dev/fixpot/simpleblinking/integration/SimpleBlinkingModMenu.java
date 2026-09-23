package dev.fixpot.simpleblinking.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.fixpot.simpleblinking.screen.SimpleBlinkingScreen;

public final class SimpleBlinkingModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return SimpleBlinkingScreen::new;
	}
}
