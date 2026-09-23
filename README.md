# Simple Blinking 👀

Client-side Fabric mod for Minecraft 26.3 that adds smooth blinking to your own Minecraft skin.

## Simple Blinking 1.0.1

- Shows your player model in the configuration screen.
- Shows an enlarged 8x8 front-face picker using the loaded skin texture.
- Each face pixel uses a real Minecraft widget hitbox, so selection works correctly with GUI scaling and Retina displays.
- Click the exact eye pixels to select/deselect them.
- Automatically samples an eyelid color from the closest opaque pixels around the selected eyes.
- Also covers matching pixels on the hat/head overlay when needed.
- Blinks naturally around every 2 seconds with a small random offset.
- Smooth closing -> tiny closed hold -> smooth opening.
- Press F7 to open or close the Simple Blinking menu.
- Only changes rendering on your client; the original skin file is never overwritten.
- Mod Menu integration.

### Default animation timing

- Interval: 2000 ms +/- 250 ms
- Closing: 95 ms
- Closed hold: 45 ms
- Opening: 120 ms

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.161.0+26.3
- Java 25
- Mod Menu is optional but recommended.
