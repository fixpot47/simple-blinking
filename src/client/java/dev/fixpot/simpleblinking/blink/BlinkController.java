package dev.fixpot.simpleblinking.blink;

import dev.fixpot.simpleblinking.config.BlinkConfig;

import java.util.concurrent.ThreadLocalRandom;

public final class BlinkController {
	private BlinkConfig config;
	private long nextBlinkAtMs;
	private long closedUntilMs;

	public BlinkController(BlinkConfig config) {
		applyConfig(config);
	}

	public void applyConfig(BlinkConfig config) {
		this.config = config;
		scheduleNext(System.currentTimeMillis());
	}

	public void tick() {
		if (!config.enabled || config.eyePixels.isEmpty()) {
			closedUntilMs = 0L;
			return;
		}

		long now = System.currentTimeMillis();

		if (closedUntilMs > 0L) {
			if (now >= closedUntilMs) {
				closedUntilMs = 0L;
				scheduleNext(now);
			}
			return;
		}

		if (now >= nextBlinkAtMs) {
			closedUntilMs = now + Math.max(60, config.closedDurationMs);
		}
	}

	public boolean isBlinking() {
		return config.enabled && closedUntilMs > System.currentTimeMillis();
	}

	public float blinkProgress() {
		if (!isBlinking()) {
			return 0.0F;
		}

		long remaining = closedUntilMs - System.currentTimeMillis();
		long duration = Math.max(60, config.closedDurationMs);
		return 1.0F - Math.min(1.0F, Math.max(0.0F, remaining / (float) duration));
	}

	private void scheduleNext(long now) {
		int randomness = Math.max(0, config.blinkRandomnessMs);
		int offset = randomness == 0
			? 0
			: ThreadLocalRandom.current().nextInt(-randomness, randomness + 1);

		nextBlinkAtMs = now + Math.max(250, config.blinkIntervalMs + offset);
	}
}
