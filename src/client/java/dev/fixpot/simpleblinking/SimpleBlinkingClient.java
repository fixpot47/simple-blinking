package dev.fixpot.simpleblinking;

import dev.fixpot.simpleblinking.blink.BlinkController;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import dev.fixpot.simpleblinking.screen.SimpleBlinkingScreen;
import dev.fixpot.simpleblinking.skin.SkinBlinkManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class SimpleBlinkingClient implements ClientModInitializer {
	public static final String MOD_ID = "simple_blinking";

	private static BlinkConfig config;
	private static BlinkController blinkController;
	private static SkinBlinkManager skinManager;
	private static KeyMapping openMenuKey;

	@Override
	public void onInitializeClient() {
		config = BlinkConfig.load();
		blinkController = new BlinkController(config);
		skinManager = new SkinBlinkManager();

		KeyMapping.Category category = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "main")
		);

		openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.simple_blinking.open_menu",
			GLFW.GLFW_KEY_F7,
			category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			skinManager.tick(client);
			blinkController.tick();

			while (openMenuKey.consumeClick()) {
				var current = client.gui.screen();
				if (current instanceof SimpleBlinkingScreen blinkingScreen) {
					blinkingScreen.onClose();
				} else {
					client.gui.setScreen(new SimpleBlinkingScreen(current));
				}
			}
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
