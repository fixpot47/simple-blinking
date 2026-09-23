package dev.fixpot.simpleblinking;

import dev.fixpot.simpleblinking.blink.BlinkController;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SimpleBlinkingClient implements ClientModInitializer {
	public static final String MOD_ID = "simple_blinking";

	private static BlinkConfig config;
	private static BlinkController blinkController;

	@Override
	public void onInitializeClient() {
		config = BlinkConfig.load();
		blinkController = new BlinkController(config);

		ClientTickEvents.END_CLIENT_TICK.register(client -> blinkController.tick());
	}

	public static BlinkConfig config() {
		return config;
	}

	public static BlinkController blinkController() {
		return blinkController;
	}

	public static void saveConfig() {
		config.save();
		blinkController.applyConfig(config);
	}
}
