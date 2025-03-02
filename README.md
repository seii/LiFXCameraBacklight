# LiFX Camera Backlight

With this program one or more cameras (typically webcams) attached to a computer can change your LiFX lights in near-realtime based on what image the camera(s) capture.

When it comes to driving LED lights based on what's playing on the TV, I love the idea of the [Govee Backlight](https://us.govee.com/products/govee-tv-backlight-3-lite-kit) product series. However, I happened to already have some [LiFX lights](https://www.lifx.com/collections/beam) instead and wasn't ready to re-purchase all of my smart lights just to take advantage of a single feature. Instead, what if a simple webcam could be used to replicate much of the same effects, but using the local API for LiFX lights?

## Features
- Auto-detection of all available cameras and LiFX lights
- Auto-detection of webcam connect/disconnect events
- Support for multiple cameras at once
- Configurable layouts, allowing any one camera to use the `LEFT`, `RIGHT`, `TOP`, `BOTTOM`, or `CENTER` of the image it sees to drive the lighting
- Multiple layouts allowed per camera
  - Example: One `LEFT` layout and one `RIGHT` layout can each power their own lights using the same camera (using the left and right of the webcam's single image)
- Only triggers changes to lights based on the camera detecting motion, so no signals are sent to lights if webcams face a static image or powered-off TV
- Motion events can be optionally ignored if the image is not bright enough (threshold for brightness is configurable)
- Provides an optional graphical JFrame window (on systems which allow graphics) of feeds for any configured cameras so you know how to move the cameras to get the right image
- Does not power on or off LiFX lights by itself, leaving that power in the user's hands

## Usage
### Launching a native binary
For those who don't want to (or don't know how to) install a Java Virtual Machine (JVM) on their system, native binaries are provided which package a standalone JVM. The file size is, as a result, higher than the standalone JAR also provided (instructions below). (The binaries are made possible by the [jpackage](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html) software.)

1. Connect at least one webcam to the system you are going to run this program on
2. From the "Releases" section on Github, download the ZIP which has a suffix (`-linux64` or `-win64`) corresponding to your platform
4. Unzip the file to a new directory
5. In the new directory, run the installer `lifx-camera-backlight-<version>-installer.exe` (in Windows) or the binary `bin/lifx-camera-backlight` (in Linux)
  1. (Windows only) Select the directory you would like to install the binary to
  2. (Windows only) Navigate to the directory you selected and run `lifx-camera-backlight.exe`

### Launching a JAR
1. Connect at least one webcam to the system you are going to run this program on
2. Run `LiFXCameraBacklight-<version>.jar` using the command `java -jar <filename>` (or double-click the JAR if your operating system is both graphics-enabled and set up to launch JAR files with a JVM by default.)

## Configuration
During the first run of the program a default log (`LiFXCameraBacklight.log`) and default configuration (`LiFXCameraBacklight.conf`) will be created in the current working directory (that is, where you launch the program from). The program's output will provide listings of each detected webcam and each detected (and compatible) LiFX device. By default the program will take the first detected webcam and first detected LiFX light available in order to demonstrate a minimum working setup. To tailor the configuration to your needs, use the previously displayed values for available webcams and LiFX lights to update the configuration file. Once finished, restart the program to pick up your changes. *Changes to the configuration file always require a program restart.*

Optionally, set the `show_gui` option to `true` in the configuration file to display a graphical window (if your system is graphics-enabled) of what each camera sees. (NOTE: This will disable actually changing any light colors via this program until the option is switched back to `false`, and is provided for calibration purposes only.)

## Building
### Required Software
- Java 11 (or higher) Java Development Kit
- [Maven](https://maven.apache.org/)

Run `mvn clean package` inside the root of this repository to generate two JAR files in the `target/` directory.

## Compatibility
### Operating System
Only Linux and Windows have officially been tested as working. However, as this program is built in Java it is theoretically possible that the JAR will run anywhere a Java Virtual Machine does. Please note that there are some bug reports for the `webcam-capture` dependency which suggest a dependency of theirs may prevent ARM-based systems from working correctly with webcams.

### LiFX
This program should work with any LiFX product which is both supported by the [LIFX-LAN-SDK](https://github.com/stuntguy3000/LIFX-LAN-SDK) project and is also specifically marked as multizone-compatible according to LiFX's own [compatibility file](https://github.com/LIFX/products) hosted on GitHub as of March 2025.
Future LiFX products may work as well, but this is not guaranteed.

## Acknowledgements
Both the [LIFX-LAN-SDK](https://github.com/stuntguy3000/LIFX-LAN-SDK) and [webcam-capture](https://github.com/sarxos/webcam-capture) projects were instrumental, I probably wouldn't have made this program without the boost their work provided.
