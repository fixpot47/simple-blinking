package dev.fixpot.simpleblinking.skin;

import dev.fixpot.simpleblinking.config.BlinkConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Picks an eyelid color from pixels immediately around the selected eyes.
 *
 * The search expands one pixel at a time and uses the first ring containing
 * opaque non-eye pixels. This makes the chosen color come from the skin
 * closest to the eye instead of from an unrelated part of the face.
 */
public final class EyelidColorPicker {
	private EyelidColorPicker() {
	}

	public static int pickNearestColor(int[] argb64x64, List<BlinkConfig.EyePixel> eyePixels, int fallbackArgb) {
		if (argb64x64 == null || argb64x64.length != 64 * 64 || eyePixels == null || eyePixels.isEmpty()) {
			return fallbackArgb;
		}

		Set<Integer> eyeSet = new HashSet<>();
		for (BlinkConfig.EyePixel pixel : eyePixels) {
			eyeSet.add(index(pixel.x(), pixel.y()));
		}

		for (int radius = 1; radius <= 8; radius++) {
			long totalA = 0;
			long totalR = 0;
			long totalG = 0;
			long totalB = 0;
			int count = 0;

			for (BlinkConfig.EyePixel eye : eyePixels) {
				for (int dy = -radius; dy <= radius; dy++) {
					for (int dx = -radius; dx <= radius; dx++) {
						if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
							continue;
						}

						int x = eye.x() + dx;
						int y = eye.y() + dy;
						if (x < 0 || x >= 64 || y < 0 || y >= 64) {
							continue;
						}

						int idx = index(x, y);
						if (eyeSet.contains(idx)) {
							continue;
						}

						int argb = argb64x64[idx];
						int a = (argb >>> 24) & 0xFF;
						if (a < 128) {
							continue;
						}

						totalA += a;
						totalR += (argb >>> 16) & 0xFF;
						totalG += (argb >>> 8) & 0xFF;
						totalB += argb & 0xFF;
						count++;
					}
				}
			}

			if (count > 0) {
				int a = (int) (totalA / count);
				int r = (int) (totalR / count);
				int g = (int) (totalG / count);
				int b = (int) (totalB / count);
				return (a << 24) | (r << 16) | (g << 8) | b;
			}
		}

		return fallbackArgb;
	}

	private static int index(int x, int y) {
		return y * 64 + x;
	}
}
