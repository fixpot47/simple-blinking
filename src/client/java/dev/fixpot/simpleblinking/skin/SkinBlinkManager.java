package dev.fixpot.simpleblinking.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.fixpot.simpleblinking.SimpleBlinkingClient;
import dev.fixpot.simpleblinking.config.BlinkConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SkinBlinkManager {
	private static final Identifier ANIMATED_SKIN =
		Identifier.fromNamespaceAndPath(SimpleBlinkingClient.MOD_ID, "animated_skin");

	private DynamicTexture dynamicTexture;
	private int[] originalPixels;
	private String loadedKey;
	private String requestedKey;
	private boolean loading;
	private boolean configDirty = true;
	private int lastFrame = -1;
	private int eyelidArgb = 0xFFFFFFFF;
	private String status = "Waiting for player skin";

	public void tick(Minecraft client) {
		if (client.player == null) {
			status = "Join a world to load your skin";
			return;
		}

		PlayerSkin skin = client.player.getSkin();
		if (skin == null || skin.body() == null) {
			status = "Player skin is not available yet";
			return;
		}

		ClientAsset.Texture body = skin.body();
		String key = key(body);
		if (key.equals(loadedKey) || key.equals(requestedKey)) {
			return;
		}

		requestedKey = key;
		load(client, body, key);
	}

	public void onConfigChanged() {
		configDirty = true;
		lastFrame = -1;
	}

	public boolean isReady() {
		return dynamicTexture != null && originalPixels != null;
	}

	public String status() {
		return status;
	}

	public int eyelidArgb() {
		prepareConfig();
		return eyelidArgb;
	}

	public @Nullable Identifier previewTexture() {
		return isReady() ? ANIMATED_SKIN : null;
	}

	public @Nullable Identifier animatedTexture(float progress) {
		if (!isReady()) {
			return null;
		}

		prepareConfig();

		BlinkConfig config = SimpleBlinkingClient.config();
		if (!config.enabled || config.eyePixels.isEmpty()) {
			restoreOriginalIfNeeded();
			return null;
		}

		progress = clamp01(progress);
		int frame = Math.round(progress * 32.0F);
		if (frame == lastFrame) {
			return ANIMATED_SKIN;
		}

		NativeImage pixels = dynamicTexture.getPixels();
		if (pixels == null || pixels.isClosed()) {
			return null;
		}

		for (BlinkConfig.EyePixel eye : config.eyePixels) {
			restorePixel(pixels, eye.x(), eye.y());
			restoreHatPixel(pixels, eye.x(), eye.y());
		}

		if (progress > 0.0F) {
			int minY = 63;
			int maxY = 0;
			for (BlinkConfig.EyePixel eye : config.eyePixels) {
				minY = Math.min(minY, eye.y());
				maxY = Math.max(maxY, eye.y());
			}

			for (BlinkConfig.EyePixel eye : config.eyePixels) {
				float localProgress = eyelidProgressForRow(progress, eye.y(), minY, maxY);
				applyPixel(pixels, eye.x(), eye.y(), localProgress);
				applyHatPixel(pixels, eye.x(), eye.y(), localProgress);
			}
		}

		dynamicTexture.upload();
		lastFrame = frame;
		return ANIMATED_SKIN;
	}

	private void load(Minecraft client, ClientAsset.Texture body, String key) {
		if (loading) {
			return;
		}
		loading = true;
		status = "Loading your skin...";

		if (body instanceof ClientAsset.DownloadedTexture downloaded && downloaded.url() != null) {
			String url = downloaded.url();
			CompletableFuture
				.supplyAsync(() -> download(url))
				.whenComplete((image, error) -> client.execute(() -> finishLoad(client, key, image, error)));
			return;
		}

		try {
			Optional<net.minecraft.server.packs.resources.Resource> resource =
				client.getResourceManager().getResource(body.texturePath());
			if (resource.isEmpty()) {
				throw new IllegalStateException("Skin texture is not readable from resources");
			}
			try (InputStream input = resource.get().open()) {
				NativeImage image = NativeImage.read(input);
				finishLoad(client, key, image, null);
			}
		} catch (Throwable error) {
			finishLoad(client, key, null, error);
		}
	}

	private static NativeImage download(String url) {
		try {
			URLConnection connection = URI.create(url).toURL().openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(7000);
			connection.setRequestProperty("User-Agent", "Simple-Blinking/1.0.1");
			try (InputStream input = connection.getInputStream()) {
				return NativeImage.read(input);
			}
		} catch (Exception exception) {
			throw new RuntimeException("Could not download player skin", exception);
		}
	}

	private void finishLoad(Minecraft client, String key, @Nullable NativeImage image, @Nullable Throwable error) {
		loading = false;

		if (!key.equals(requestedKey)) {
			if (image != null && !image.isClosed()) {
				image.close();
			}
			return;
		}

		if (error != null || image == null) {
			status = "Could not read this skin";
			requestedKey = null;
			return;
		}

		if (image.getWidth() != 64 || image.getHeight() != 64) {
			status = "Simple Blinking needs a 64x64 skin";
			image.close();
			requestedKey = null;
			return;
		}

		try {
			if (dynamicTexture != null) {
				client.getTextureManager().release(ANIMATED_SKIN);
				dynamicTexture = null;
			}

			originalPixels = image.getPixels();
			dynamicTexture = new DynamicTexture(() -> "Simple Blinking animated skin", image);
			client.getTextureManager().register(ANIMATED_SKIN, dynamicTexture);

			loadedKey = key;
			requestedKey = key;
			configDirty = true;
			lastFrame = -1;
			status = "Skin loaded";
		} catch (Throwable throwable) {
			status = "Could not create animated skin";
			if (!image.isClosed()) {
				image.close();
			}
			dynamicTexture = null;
			originalPixels = null;
			requestedKey = null;
		}
	}

	private void prepareConfig() {
		if (!isReady() || !configDirty) {
			return;
		}

		BlinkConfig config = SimpleBlinkingClient.config();
		NativeImage pixels = dynamicTexture.getPixels();
		if (pixels != null && !pixels.isClosed()) {
			restoreAll(pixels);
			dynamicTexture.upload();
		}

		eyelidArgb = config.automaticEyelidColor
			? EyelidColorPicker.pickNearestColor(originalPixels, config.eyePixels, config.manualEyelidArgb)
			: config.manualEyelidArgb;

		configDirty = false;
		lastFrame = -1;
	}

	private void restoreOriginalIfNeeded() {
		if (lastFrame <= 0 || !isReady()) {
			return;
		}
		NativeImage pixels = dynamicTexture.getPixels();
		if (pixels == null || pixels.isClosed()) {
			return;
		}
		restoreAll(pixels);
		dynamicTexture.upload();
		lastFrame = 0;
	}

	private void restoreAll(NativeImage pixels) {
		if (originalPixels == null || originalPixels.length != 64 * 64) {
			return;
		}
		for (int y = 0; y < 64; y++) {
			for (int x = 0; x < 64; x++) {
				pixels.setPixel(x, y, originalPixels[index(x, y)]);
			}
		}
	}

	private void restorePixel(NativeImage pixels, int x, int y) {
		if (inside(x, y)) {
			pixels.setPixel(x, y, originalPixels[index(x, y)]);
		}
	}

	private void restoreHatPixel(NativeImage pixels, int x, int y) {
		if (isFrontFacePixel(x, y)) {
			int overlayX = x + 32;
			pixels.setPixel(overlayX, y, originalPixels[index(overlayX, y)]);
		}
	}

	private void applyPixel(NativeImage pixels, int x, int y, float amount) {
		if (!inside(x, y)) {
			return;
		}
		int original = originalPixels[index(x, y)];
		pixels.setPixel(x, y, lerpArgb(original, eyelidArgb, amount));
	}

	private void applyHatPixel(NativeImage pixels, int x, int y, float amount) {
		if (!isFrontFacePixel(x, y)) {
			return;
		}

		int overlayX = x + 32;
		int original = originalPixels[index(overlayX, y)];
		int alpha = (original >>> 24) & 0xFF;
		if (alpha == 0) {
			return;
		}

		int target = (alpha << 24) | (eyelidArgb & 0x00FFFFFF);
		pixels.setPixel(overlayX, y, lerpArgb(original, target, amount));
	}

	private static float eyelidProgressForRow(float progress, int y, int minY, int maxY) {
		if (maxY <= minY) {
			return progress;
		}

		float row = (y - minY) / (float) (maxY - minY);
		float delay = row * 0.30F;
		return smoothStep(clamp01((progress - delay) / Math.max(0.001F, 1.0F - delay)));
	}

	private static int lerpArgb(int from, int to, float t) {
		t = smoothStep(clamp01(t));
		int a = lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, t);
		int r = lerp((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, t);
		int g = lerp((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, t);
		int b = lerp(from & 0xFF, to & 0xFF, t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int lerp(int from, int to, float t) {
		return Math.round(from + (to - from) * t);
	}

	private static float smoothStep(float t) {
		t = clamp01(t);
		return t * t * (3.0F - 2.0F * t);
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static boolean inside(int x, int y) {
		return x >= 0 && x < 64 && y >= 0 && y < 64;
	}

	private static boolean isFrontFacePixel(int x, int y) {
		return x >= 8 && x <= 15 && y >= 8 && y <= 15;
	}

	private static int index(int x, int y) {
		return y * 64 + x;
	}

	private static String key(ClientAsset.Texture texture) {
		if (texture instanceof ClientAsset.DownloadedTexture downloaded && downloaded.url() != null) {
			return "url:" + downloaded.url();
		}
		return "texture:" + texture.texturePath();
	}
}
