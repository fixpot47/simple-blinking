# Simple Blinking 👀

A lightweight client-side Fabric mod for Minecraft 26.3 that makes the player's skin blink naturally.

## Version 1.0.0 — in development

The blink is **animated**, not a hard texture swap:

1. eyelid smoothly closes;
2. stays closed for a tiny moment;
3. smoothly opens again.

The animation uses an eased curve, so it slows near fully-open and fully-closed states instead of looking robotic.

### Implemented
- Fabric 26.3 project setup.
- Persistent JSON config.
- Natural ~2 second blink interval with small random variation.
- Smooth close / hold / open animation controller.
- In-menu "Preview blink" action.
- Eye-pixel data model for 64x64 skins.
- Automatic eyelid-color algorithm that samples the nearest opaque skin pixels around the selected eyes.
- Mod Menu integration.

### v1.0.0 work still in progress
- Live skin preview inside the menu.
- Click directly on the skin to select eye pixels.
- Draw the animated eyelid over the selected pixels on the player model.
- Preview the selected eyelid color before saving.

## Defaults

- Blink interval: 2000 ms ± 250 ms
- Close: 95 ms
- Closed hold: 45 ms
- Open: 120 ms

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.161.0+26.3
- Java 25
- Mod Menu is optional, but recommended.
