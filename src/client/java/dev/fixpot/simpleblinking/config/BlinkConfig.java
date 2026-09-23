package dev.fixpot.simpleblinking.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BlinkConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("simple-blinking.json");

	public boolean enabled = true;

	/**
	 * Average time between blinks. A small random offset is added so blinking
	 * does not look robotic.
	 */
	public int blinkIntervalMs = 2000;
	public int blinkRandomnessMs = 250;

	/**
	 * Smooth animation phases.
	 * open -> closing -> closed hold -> opening -> open
	 */
	public int closingDurationMs = 95;
	public int closedHoldDurationMs = 45;
	public int openingDurationMs = 120;

	public boolean automaticEyelidColor = true;

	/**
	 * Skin pixel coordinates in the normal 64x64 skin texture.
	 */
	public List<EyePixel> eyePixels = new ArrayList<>();

	/**
	 * Optional manual ARGB color. Used only when automaticEyelidColor is false.
	 */
	public int manualEyelidArgb = 0xFFFFFFFF;

	public static BlinkConfig load() {
		if (!Files.isRegularFile(PATH)) {
			BlinkConfig config = new BlinkConfig();
			config.save();
			return config;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			BlinkConfig loaded = GSON.fromJson(reader, BlinkConfig.class);
			return loaded == null ? new BlinkConfig() : loaded;
		} catch (Exception ignored) {
			return new BlinkConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			throw new RuntimeException("Failed to save Simple Blinking config", exception);
		}
	}

	public int totalBlinkDurationMs() {
		return Math.max(1, closingDurationMs)
			+ Math.max(0, closedHoldDurationMs)
			+ Math.max(1, openingDurationMs);
	}

	public record EyePixel(int x, int y) {
		public EyePixel {
			if (x < 0 || x > 63 || y < 0 || y > 63) {
				throw new IllegalArgumentException("Skin pixel must be inside a 64x64 texture");
			}
		}
	}
}
