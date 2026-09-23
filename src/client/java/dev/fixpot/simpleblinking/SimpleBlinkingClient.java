package dev.fixpot.simpleblinking;

import dev.fixpot.simpleblinking.blink.BlinkController;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import dev.fixpot.simpleblinking.skin.SkinBlinkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SimpleBlinkingClient implements ClientModInitializer {
	public static final String MOD_ID = "simple_blinking";

	private static BlinkConfig config;
	private static BlinkController blinkController;
	private static SkinBlinkManager skinManager;

	@Override
	public void onInitializeClient() {
		config = BlinkConfig.load();
		blinkController = new BlinkController(config);
		skinManager = new SkinBlinkManager();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			skinManager.tick(client);
			blinkController.tick();
		});
	}

	public static BlinkConfig config() {
		return config;
	}

	public static BlinkController blinkController() {
		return blinkController;
	}

	public static SkinBlinkManager skinManager() {
		return skinManager;
	}

	public static void saveConfig() {
		config.save();
		blinkController.applyConfig(config);
		if (skinManager != null) {
			skinManager.onConfigChanged();
		}
	}
}
