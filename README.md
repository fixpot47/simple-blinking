# Simple Blinking 👀

Client-side Fabric mod for Minecraft 26.3 that adds smooth blinking to your own Minecraft skin.

## Simple Blinking 1.0.3

- The menu hotkey is now **X**, registered through Minecraft 26.3's KEYBOARD input API.
- Fixed Minecraft 26.3 SDL mouse-button handling: left-click now uses `InputConstants.MOUSE_BUTTON_LEFT`.
- Fixed the enlarged face texture UV/destination argument order, so the actual 8x8 face is shown instead of mostly transparent pixels.
- The eye picker uses direct screen hit-testing with visible hover feedback.
- Clicking a face pixel immediately updates the selected-pixel counter and shows which skin pixel was changed.
- Enlarged face picker for easier eye selection.
- New **3x upper-body portrait**: head, torso and shoulders are shown instead of the tiny full-body preview.
- Automatically samples an eyelid color from the closest opaque pixels around the selected eyes.
- Smooth closing -> short closed hold -> smooth opening.
- X opens/closes the Simple Blinking menu.
- The original skin file is never overwritten.

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
