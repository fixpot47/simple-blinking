package dev.fixpot.simpleblinking.screen;

import dev.fixpot.simpleblinking.SimpleBlinkingClient;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
	private static final int CELL = 16;
	private static final int GRID_SIZE = FACE_PIXELS * CELL;

	private final Screen parent;

	public SimpleBlinkingScreen(Screen parent) {
		super(Component.literal("Simple Blinking"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = width / 2;

		// Use normal Screen widgets for every face cell. This is more reliable
		// than manually hit-testing the full grid and also works correctly with
		// Minecraft's GUI scaling on macOS/Retina displays.
		for (int localY = 0; localY < FACE_PIXELS; localY++) {
			for (int localX = 0; localX < FACE_PIXELS; localX++) {
				addRenderableWidget(new EyePixelCell(
					gridX() + localX * CELL,
					gridY() + localY * CELL,
					localX,
					localY
				));
			}
		}

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

		graphics.centeredText(font, title, centerX, 12, 0xFFFFFFFF);
		graphics.centeredText(
			font,
			Component.literal("Click the eye pixels on the enlarged face"),
			centerX,
			28,
			0xFFBFC7D5
		);

		drawPlayerPreview(graphics);
		drawFacePicker(graphics, gridX, gridY);

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
		graphics.fill(gridX + 72, gridY + GRID_SIZE + 21, gridX + 88, gridY + GRID_SIZE + 33, color);
		graphics.outline(gridX + 72, gridY + GRID_SIZE + 21, 16, 12, 0xFFFFFFFF);

		int percent = Math.round(SimpleBlinkingClient.blinkController().blinkProgress() * 100.0F);
		graphics.text(
			font,
			"Blink: " + percent + "% closed",
			gridX,
			gridY + GRID_SIZE + 39,
			0xFFAEB8C7,
			false
		);

		graphics.text(
			font,
			SimpleBlinkingClient.skinManager().status() + "  |  F7: open/close",
			gridX,
			gridY + GRID_SIZE + 54,
			0xFF8F9AAA,
			false
		);
	}

	private void drawFacePicker(GuiGraphicsExtractor graphics, int x, int y) {
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

		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			texture,
			x,
			y,
			GRID_SIZE,
			GRID_SIZE,
			8,
			8,
			8,
			8,
			64,
			64
		);

		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			texture,
			x,
			y,
			GRID_SIZE,
			GRID_SIZE,
			40,
			8,
			8,
			8,
			64,
			64
		);

		for (int i = 0; i <= FACE_PIXELS; i++) {
			int px = x + i * CELL;
			int py = y + i * CELL;
			graphics.fill(px, y, px + 1, y + GRID_SIZE, 0x553A4250);
			graphics.fill(x, py, x + GRID_SIZE, py + 1, 0x553A4250);
		}

		for (BlinkConfig.EyePixel pixel : SimpleBlinkingClient.config().eyePixels) {
			if (pixel.x() < 8 || pixel.x() > 15 || pixel.y() < 8 || pixel.y() > 15) {
				continue;
			}
			int localX = pixel.x() - 8;
			int localY = pixel.y() - 8;
			int px = x + localX * CELL;
			int py = y + localY * CELL;
			graphics.fill(px + 1, py + 1, px + CELL, py + CELL, 0x5577CCFF);
			graphics.outline(px, py, CELL, CELL, 0xFFFFFFFF);
		}

		graphics.outline(x, y, GRID_SIZE, GRID_SIZE, 0xFFFFFFFF);
	}

	private void drawPlayerPreview(GuiGraphicsExtractor graphics) {
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

			int boxW = 130;
			int boxH = Math.min(180, Math.max(120, height - 120));
			int x0 = width / 2 - 210;
			int y0 = 42;
			int x1 = x0 + boxW;
			int y1 = y0 + boxH;

			graphics.fill(x0, y0, x1, y1, 0x33000000);
			graphics.outline(x0, y0, boxW, boxH, 0xFF596273);

			Quaternionf pose = new Quaternionf().rotateZ((float)Math.PI);
			Quaternionf camera = new Quaternionf();
			Vector3f translation = new Vector3f(0.0F, 0.9F, 0.0F);
			graphics.entity(state, 42.0F, translation, pose, camera, x0, y0, x1, y1);
			graphics.centeredText(font, Component.literal("Your skin"), (x0 + x1) / 2, y1 + 6, 0xFFBFC7D5);
		} catch (Throwable ignored) {
			// The face picker stays usable even when another rendering mod
			// interferes with the inventory-style entity preview.
		}
	}

	private void toggleEyePixel(int x, int y) {
		BlinkConfig.EyePixel target = new BlinkConfig.EyePixel(x, y);
		if (SimpleBlinkingClient.config().eyePixels.contains(target)) {
			SimpleBlinkingClient.config().eyePixels.remove(target);
		} else {
			SimpleBlinkingClient.config().eyePixels.add(target);
		}
		SimpleBlinkingClient.saveConfig();
	}

	private int gridX() {
		return width / 2 + 25;
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

	private final class EyePixelCell extends AbstractWidget {
		private final int localX;
		private final int localY;

		private EyePixelCell(int x, int y, int localX, int localY) {
			super(x, y, CELL, CELL, Component.empty());
			this.localX = localX;
			this.localY = localY;
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (!active || !visible || event.button() != 0 || !isMouseOver(event.x(), event.y())) {
				return false;
			}
			toggleEyePixel(8 + localX, 8 + localY);
			return true;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			// Invisible hitbox: the picker texture/grid is drawn by the screen.
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narration) {
			// No narration text needed for the invisible pixel hitbox.
		}
	}
}
