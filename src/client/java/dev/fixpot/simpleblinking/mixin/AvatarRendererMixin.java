package dev.fixpot.simpleblinking.mixin;

import dev.fixpot.simpleblinking.SimpleBlinkingClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public final class AvatarRendererMixin {
	@Inject(
		method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;",
		at = @At("HEAD"),
		cancellable = true,
		require = 1
	)
	private void simpleBlinking$animatedSkin(
		AvatarRenderState renderState,
		CallbackInfoReturnable<Identifier> cir
	) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}

		if (!SimpleBlinkingClient.config().enabled || SimpleBlinkingClient.config().eyePixels.isEmpty()) {
			return;
		}

		Entity entity = client.level.getEntity(renderState.id);
		if (!(entity instanceof AbstractClientPlayer player)) {
			return;
		}

		if (!player.getUUID().equals(client.player.getUUID())) {
			return;
		}

		Identifier animated = SimpleBlinkingClient.skinManager().animatedTexture(
			SimpleBlinkingClient.blinkController().blinkProgress()
		);
		if (animated != null) {
			cir.setReturnValue(animated);
		}
	}
}
