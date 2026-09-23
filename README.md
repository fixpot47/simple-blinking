# Simple Blinking 👀

A lightweight client-side Fabric mod for Minecraft 26.3 that makes the player's skin blink naturally.

## Goal

- Show your current Minecraft skin inside the mod menu.
- Let you select the eye pixels directly on your skin.
- Automatically choose an eyelid color from the nearest surrounding skin pixels.
- Blink roughly every 2 seconds with a short natural closed-eye animation.
- Keep the original skin untouched on disk.
- Client-side only.

## Current development status

### v0.1 foundation
- Fabric 26.3 project setup.
- Persistent JSON config.
- Blink timing controller with small random timing variation.
- Mod Menu configuration screen entry point.
- Eye-selection data model.

### Next implementation step
- Live player skin preview.
- Click-to-select eye pixels.
- Automatic eyelid color sampling.
- Render-layer overlay that covers only the selected eye pixels while blinking.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.161.0+26.3
- Java 25
- Mod Menu is optional, but recommended for opening the configuration screen.
