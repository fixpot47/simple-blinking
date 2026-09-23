package dev.fixpot.simpleblinking.screen;

import dev.fixpot.simpleblinking.SimpleBlinkingClient;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SimpleBlinkingScreen extends Screen {
	private static final int FACE_PIXELS = 8;
	private static final int CELL = 18;
	private static final int GRID_SIZE = FACE_PIXELS * CELL;

	private final Screen parent;
	private String lastAction = "Click the eye pixels";

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
			).bounds(centerX - 205, height - 54, 130, 20).build()
		);

		addRenderableWidget(
			Button.builder(
				Component.literal("Preview blink"),
				button -> SimpleBlinkingClient.blinkController().triggerPreviewBlink()
			).bounds(centerX - 65, height - 54, 130, 20).build()
		);

		addRenderableWidget(
			Button.builder(
				Component.literal("Clear eyes"),
				button -> {
					SimpleBlinkingClient.config().eyePixels.clear();
					SimpleBlinkingClient.saveConfig();
					lastAction = "Eye selection cleared";
				}
			).bounds(centerX + 75, height - 54, 130, 20).build()
		);

		addRenderableWidget(
			Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(centerX - 75, height - 30, 150, 20)
				.build()
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		int centerX = width / 2;
		int gridX = gridX();
		int gridY = gridY();

		graphics.centeredText(font, title, centerX, 10, 0xFFFFFFFF);
		graphics.centeredText(
			font,
			Component.literal("Select the eye pixels on your face"),
			centerX,
			25,
			0xFFBFC7D5
		);

		drawUpperBodyPreview(graphics);
		drawFacePicker(graphics, gridX, gridY, mouseX, mouseY);

		graphics.text(
			font,
			"Selected: " + SimpleBlinkingClient.config().eyePixels.size() + " pixel(s)",
			gridX,
			gridY + GRID_SIZE + 8,
			0xFFFFFFFF,
			false
		);

		int color = SimpleBlinkingClient.skinManager().eyelidArgb();
		graphics.text(font, "Eyelid color", gridX, gridY + GRID_SIZE + 23, 0xFFCFCFCF, false);
		graphics.fill(gridX + 72, gridY + GRID_SIZE + 21, gridX + 90, gridY + GRID_SIZE + 34, color);
		graphics.outline(gridX + 72, gridY + GRID_SIZE + 21, 18, 13, 0xFFFFFFFF);

		int percent = Math.round(SimpleBlinkingClient.blinkController().blinkProgress() * 100.0F);
		graphics.text(
			font,
			"Blink: " + percent + "% closed",
			gridX,
			gridY + GRID_SIZE + 40,
			0xFFAEB8C7,
			false
		);

		graphics.text(
			font,
			lastAction,
			gridX,
			gridY + GRID_SIZE + 55,
			0xFF9ED0A6,
			false
		);

		graphics.text(
			font,
			SimpleBlinkingClient.skinManager().status() + "  |  X: open/close",
			gridX,
			gridY + GRID_SIZE + 70,
			0xFF8F9AAA,
			false
		);
	}

	private void drawFacePicker(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || player.getSkin() == null || player.getSkin().body() == null) {
			graphics.fill(x, y, x + GRID_SIZE, y + GRID_SIZE, 0x66000000);
			graphics.outline(x, y, GRID_SIZE, GRID_SIZE, 0xFF6E7785);
			graphics.centeredText(font, Component.literal("Join a world"), x + GRID_SIZE / 2, y + GRID_SIZE / 2 - 4, 0xFFFFFFFF);
			return;
		}

		Identifier texture = SimpleBlinkingClient.skinManager().previewTexture();
		if (texture == null) {
			texture = player.getSkin().body().texturePath();
		}

		// Front of the head, base layer.
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			texture,
			x,
			y,
			8,
			8,
			GRID_SIZE,
			GRID_SIZE,
			8,
			8,
			64,
			64
		);

		// Head overlay / hat layer.
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			texture,
			x,
			y,
			40,
			8,
			GRID_SIZE,
			GRID_SIZE,
			8,
			8,
			64,
			64
		);

		for (int i = 0; i <= FACE_PIXELS; i++) {
			int px = x + i * CELL;
			int py = y + i * CELL;
			graphics.fill(px, y, px + 1, y + GRID_SIZE, 0x663A4250);
			graphics.fill(x, py, x + GRID_SIZE, py + 1, 0x663A4250);
		}

		for (BlinkConfig.EyePixel pixel : SimpleBlinkingClient.config().eyePixels) {
			if (!isFacePixel(pixel.x(), pixel.y())) {
				continue;
			}
			int localX = pixel.x() - 8;
			int localY = pixel.y() - 8;
			int px = x + localX * CELL;
			int py = y + localY * CELL;
			graphics.fill(px + 1, py + 1, px + CELL, py + CELL, 0x6677CCFF);
			graphics.outline(px, py, CELL, CELL, 0xFFFFFFFF);
		}

		// Make it obvious which pixel is clickable before the user clicks it.
		if (isInsideGrid(mouseX, mouseY)) {
			int localX = (mouseX - x) / CELL;
			int localY = (mouseY - y) / CELL;
			int px = x + localX * CELL;
			int py = y + localY * CELL;
			graphics.fill(px + 1, py + 1, px + CELL, py + CELL, 0x44FFFFFF);
			graphics.outline(px, py, CELL, CELL, 0xFFFFFF55);
		}

		graphics.outline(x, y, GRID_SIZE, GRID_SIZE, 0xFFFFFFFF);
	}

	private void drawUpperBodyPreview(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}

		try {
			EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
			EntityRenderState state = dispatcher.extractEntity(player, 1.0F);
			state.shadowPieces.clear();
			state.outlineColor = 0;
			state.nameTag = null;

			if (state instanceof LivingEntityRenderState living) {
				living.pose = Pose.STANDING;
				living.bodyRot = 180.0F;
				living.yRot = 0.0F;
				living.xRot = 0.0F;
				living.scale = 1.0F;
				living.walkAnimationPos = 0.0F;
				living.walkAnimationSpeed = 0.0F;
				living.isAutoSpinAttack = false;
			}

			if (state instanceof ArmedEntityRenderState armed) {
				armed.rightHandItemStack = ItemStack.EMPTY;
				armed.leftHandItemStack = ItemStack.EMPTY;
				armed.rightHandItemState.clear();
				armed.leftHandItemState.clear();
				armed.rightArmPose = HumanoidModel.ArmPose.EMPTY;
				armed.leftArmPose = HumanoidModel.ArmPose.EMPTY;
			}

			if (state instanceof HumanoidRenderState humanoid) {
				humanoid.isCrouching = false;
				humanoid.isFallFlying = false;
				humanoid.isVisuallySwimming = false;
				humanoid.isPassenger = false;
				humanoid.isUsingItem = false;
				humanoid.swimAmount = 0.0F;
			}

			if (state instanceof AvatarRenderState avatar) {
				avatar.showHat = true;
				avatar.showJacket = true;
				avatar.showLeftPants = true;
				avatar.showRightPants = true;
				avatar.showLeftSleeve = true;
				avatar.showRightSleeve = true;
				avatar.isSpectator = false;
			}

			int boxW = 180;
			int boxH = 165;
			int x0 = width / 2 - 235;
			int y0 = 42;
			int x1 = x0 + boxW;
			int y1 = y0 + boxH;

			graphics.fill(x0, y0, x1, y1, 0x44000000);
			graphics.outline(x0, y0, boxW, boxH, 0xFF697487);

			Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI);
			Quaternionf camera = new Quaternionf();

			// The old preview used scale 42. 126 is exactly 3x.
			// Moving the model pivot upward centers the portrait around the
			// head/chest so the legs are cropped out and only the upper body
			// and shoulders remain visible.
			Vector3f translation = new Vector3f(0.0F, 1.35F, 0.0F);
			graphics.entity(state, 126.0F, translation, pose, camera, x0, y0, x1, y1);

			graphics.centeredText(
				font,
				Component.literal("3x skin preview"),
				(x0 + x1) / 2,
				y1 + 6,
				0xFFBFC7D5
			);
		} catch (Throwable ignored) {
			// The eye picker remains available even if another rendering mod
			// interferes with the 3D portrait.
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			int mouseX = (int)Math.floor(event.x());
			int mouseY = (int)Math.floor(event.y());

			if (isInsideGrid(mouseX, mouseY)) {
				int localX = Math.min(FACE_PIXELS - 1, Math.max(0, (mouseX - gridX()) / CELL));
				int localY = Math.min(FACE_PIXELS - 1, Math.max(0, (mouseY - gridY()) / CELL));
				int skinX = 8 + localX;
				int skinY = 8 + localY;

				toggleEyePixel(skinX, skinY);
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void toggleEyePixel(int x, int y) {
		BlinkConfig.EyePixel target = new BlinkConfig.EyePixel(x, y);
		if (SimpleBlinkingClient.config().eyePixels.contains(target)) {
			SimpleBlinkingClient.config().eyePixels.remove(target);
			lastAction = "Removed eye pixel: " + x + ", " + y;
		} else {
			SimpleBlinkingClient.config().eyePixels.add(target);
			lastAction = "Selected eye pixel: " + x + ", " + y;
		}

		SimpleBlinkingClient.saveConfig();
	}

	private boolean isInsideGrid(int mouseX, int mouseY) {
		int x = gridX();
		int y = gridY();
		return mouseX >= x && mouseX < x + GRID_SIZE && mouseY >= y && mouseY < y + GRID_SIZE;
	}

	private static boolean isFacePixel(int x, int y) {
		return x >= 8 && x <= 15 && y >= 8 && y <= 15;
	}

	private int gridX() {
		return width / 2 + 35;
	}

	private int gridY() {
		return 44;
	}

	@Override
	public void onClose() {
		SimpleBlinkingClient.saveConfig();
		if (minecraft != null) {
			minecraft.gui.setScreen(parent);
		}
	}
}
