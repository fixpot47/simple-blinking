package dev.fixpot.simpleblinking.blink;

import dev.fixpot.simpleblinking.config.BlinkConfig;

import java.util.concurrent.ThreadLocalRandom;

public final class BlinkController {
	private BlinkConfig config;

	private long nextBlinkAtMs;
	private long blinkStartedAtMs = -1L;
	private boolean previewBlink;

	public BlinkController(BlinkConfig config) {
		applyConfig(config);
	}

	public void applyConfig(BlinkConfig config) {
		this.config = config;
		this.blinkStartedAtMs = -1L;
		this.previewBlink = false;
		scheduleNext(System.currentTimeMillis());
	}

	public void tick() {
		long now = System.currentTimeMillis();

		if (!config.enabled) {
			blinkStartedAtMs = -1L;
			previewBlink = false;
			return;
		}

		if (blinkStartedAtMs >= 0L) {
			if (now - blinkStartedAtMs >= config.totalBlinkDurationMs()) {
				blinkStartedAtMs = -1L;
				if (!previewBlink) {
					scheduleNext(now);
				}
				previewBlink = false;
			}
			return;
		}

		// Normal blinking only starts after the eyes have been selected.
		if (!config.eyePixels.isEmpty() && now >= nextBlinkAtMs) {
			beginBlink(now, false);
		}
	}

	/**
	 * Starts one animation immediately. Used by the settings-screen preview.
	 */
	public void triggerPreviewBlink() {
		if (!config.enabled) {
			return;
		}

		beginBlink(System.currentTimeMillis(), true);
	}

	/**
	 * 0 = fully open, 1 = fully closed.
	 *
	 * The value is eased instead of moving linearly, so the eyelid starts and
	 * finishes softly rather than snapping between skin states.
	 */
	public float blinkProgress() {
		if (blinkStartedAtMs < 0L) {
			return 0.0F;
		}

		long elapsed = Math.max(0L, System.currentTimeMillis() - blinkStartedAtMs);

		int closing = Math.max(1, config.closingDurationMs);
		int hold = Math.max(0, config.closedHoldDurationMs);
		int opening = Math.max(1, config.openingDurationMs);

		if (elapsed < closing) {
			float t = elapsed / (float) closing;
			return smoothStep(t);
		}

		elapsed -= closing;
		if (elapsed < hold) {
			return 1.0F;
		}

		elapsed -= hold;
		if (elapsed < opening) {
			float t = elapsed / (float) opening;
			return 1.0F - smoothStep(t);
		}

		return 0.0F;
	}

	public boolean isBlinking() {
		return blinkProgress() > 0.0001F;
	}

	private void beginBlink(long now, boolean preview) {
		blinkStartedAtMs = now;
		previewBlink = preview;
	}

	private void scheduleNext(long now) {
		int randomness = Math.max(0, config.blinkRandomnessMs);
		int offset = randomness == 0
			? 0
			: ThreadLocalRandom.current().nextInt(-randomness, randomness + 1);

		nextBlinkAtMs = now + Math.max(250, config.blinkIntervalMs + offset);
	}

	private static float smoothStep(float t) {
		t = Math.max(0.0F, Math.min(1.0F, t));
		return t * t * (3.0F - 2.0F * t);
	}
}
