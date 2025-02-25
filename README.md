# LiFX Camera Backlight

When it comes to driving LED lights based on what's playing on the TV, I love the idea of the [Govee Backlight](https://us.govee.com/products/govee-tv-backlight-3-lite-kit) product series. However, I happened to already have some [LiFX lights](https://www.lifx.com/collections/beam) instead and wasn't ready to re-purchase everything just to take advantage of this feature. Instead, what if a simple webcam could be used to mostly duplicate the same effects for LiFX lights?

With this program one or more cameras (typically webcams) attached to a computer can change your LiFX lights in near-realtime based on what image the camera(s) capture.

## Features
- Auto-detection of all available cameras and LiFX lights
- Support for multiple cameras at once
- Configurable  layouts, allowing any one camera to use the `LEFT`, `RIGHT`, `TOP`, `BOTTOM`, or `CENTER` of the image it sees to drive the lighting
- Multiple layouts allowed per camera; for example, one camera can use one `LEFT` layout to power a light on the left and one `RIGHT` layout to power a different light on the right
- Provides an optional graphical JFrame window (on systems which allow graphics) of feeds for any configured cameras so you know how to move the cameras to get the right image
- Does not power on or off LiFX lights by itself, leaving that power in the user's hands
- Only triggers based on motion, so no signals are sent to lights if webcams face a static image or powered-off TV

## Usage
1. Connect at least one webcam to the system you are going to run this program on
2. Run `LiFXCameraBacklight-<version>-jar-with-dependencies.jar` using the command `java -jar <filename>` (or double-click the JAR if your operating system is both graphical and setup to launch JAR files with a JVM by default.)
3. During the first run of the program a default log (`LiFXCameraBacklight.log`) and default configuration (`LiFXCameraBacklight.conf`) will be created in the current working directory.
4. The program will provide listings of each detected webcam and each detected (and compatible) LiFX device. Copy these values, then update the configuration file to reflect your desired setup and options.
5. Restart the program, which should now pick up your configuration file changes.
6. Optionally, set the `show_gui` option to `true` in the configuration file to display a graphical window of what each camera sees. (NOTE: This will disable actually changing any light colors via this program until the option is switched back to `false`, and is provided for calibration purposes only.)

## Building
### Required Software
- Java 11 (or higher) Java Development Kit
- [Maven](https://maven.apache.org/)

Run `mvn clean package` inside the root of this repository to generate two JAR files in the `target/` directory.

## Compatibility
### Operating System
Only Linux and Windows have officially been tested as working. However, as this program is built in Java it is theoretically possible that it will run anywhere a Java Virtual Machine does.

### LiFX
This program should work with any LiFX product which is both supported by the [LIFX-LAN-SDK](https://github.com/stuntguy3000/LIFX-LAN-SDK) project and is also specifically marked as multizone-compatible according to LiFX's own [compatibility file](https://github.com/LIFX/products) hosted on GitHub.
Future LiFX products could work as well, but only after `LIFX-LAN-SDK` (which I do not own) is updated to take them into account.

## Acknowledgements
Both the [LIFX-LAN-SDK](https://github.com/stuntguy3000/LIFX-LAN-SDK) and [webcam-capture](https://github.com/sarxos/webcam-capture) projects were insstrumental, I probably wouldn't have made this program without their libraries to build on top of.
