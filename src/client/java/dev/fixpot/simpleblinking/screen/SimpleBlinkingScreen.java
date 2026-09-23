package dev.fixpot.simpleblinking.screen;

import dev.fixpot.simpleblinking.SimpleBlinkingClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SimpleBlinkingScreen extends Screen {
	private final Screen parent;

	public SimpleBlinkingScreen(Screen parent) {
		super(Component.literal("Simple Blinking"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = width / 2;

		addRenderableWidget(
			Button.builder(
				Component.literal(SimpleBlinkingClient.config().enabled ? "Blinking: ON" : "Blinking: OFF"),
				button -> {
					SimpleBlinkingClient.config().enabled = !SimpleBlinkingClient.config().enabled;
					SimpleBlinkingClient.saveConfig();
					button.setMessage(Component.literal(
						SimpleBlinkingClient.config().enabled ? "Blinking: ON" : "Blinking: OFF"
					));
				}
			).bounds(centerX - 155, height - 54, 150, 20).build()
		);

		addRenderableWidget(
			Button.builder(
				Component.literal("Preview blink"),
				button -> SimpleBlinkingClient.blinkController().triggerPreviewBlink()
			).bounds(centerX + 5, height - 54, 150, 20).build()
		);

		addRenderableWidget(
			Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(centerX - 75, height - 30, 150, 20)
				.build()
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		int centerX = width / 2;
		graphics.drawCenteredString(font, title, centerX, 18, 0xFFFFFF);

		graphics.drawCenteredString(
			font,
			Component.literal("Skin preview + eye picker"),
			centerX,
			44,
			0xE6E6E6
		);

		graphics.drawCenteredString(
			font,
			Component.literal("v1.0.0: smooth eyelid animation"),
			centerX,
			60,
			0xA0A0A0
		);

		graphics.drawCenteredString(
			font,
			Component.literal("Selected eye pixels: " + SimpleBlinkingClient.config().eyePixels.size()),
			centerX,
			82,
			0xFFFFFF
		);

		int percent = Math.round(SimpleBlinkingClient.blinkController().blinkProgress() * 100.0F);
		graphics.drawCenteredString(
			font,
			Component.literal("Blink animation: " + percent + "% closed"),
			centerX,
			98,
			0xCFCFCF
		);
	}

	@Override
	public void onClose() {
		SimpleBlinkingClient.saveConfig();
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}
}
