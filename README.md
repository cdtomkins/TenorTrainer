# TenorTrainer
Steelpan layout training Android app. Learn the Tenor pan layout by tapping notes on a photo of the pan.

## Modes

- **Tenor Time Attack**: tap 30 random notes as fast as you can. Each prompt names the register (for example "high C#"), and only the named note is accepted. Score = seconds taken + 10 s for each wrong tap. Lower is better.
- **High Scores**: top 10 scores per mode, saved on the device. The screen has a **Clear high scores** button, which deletes all saved scores after confirmation.

New modes can be added by implementing `GameMode` in `app/src/main/java/com/cdtomkins/tenortrainer/game/` and registering them in `ModeRegistry.kt`.

Note positions are in `game/TenorPan.kt`, using pixel coordinates in the 1848×1848 `drawable-nodpi/tenor_pan.jpg`.

## Build

Requires JDK 17 and the Android SDK (platform 35). Put the SDK path in `local.properties`:

```
sdk.dir=/path/to/Android/sdk
```

Then:

```
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # build app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions (`.github/workflows/ci.yml`) runs the unit tests and the debug build on every push and pull request.

## Install on a real phone

Requires Android 8.0 or later.

1. On the phone, enable Developer options: open **Settings > About phone** and tap **Build number** seven times.
2. In **Settings > System > Developer options**, turn on **USB debugging**.
3. Connect the phone with a USB cable and accept the "Allow USB debugging" prompt on the phone.
4. Check the phone shows up:

   ```
   $ANDROID_HOME/platform-tools/adb devices
   ```

5. Install and open the app:

   ```
   ./gradlew launchApp
   ```

   `launchApp` installs the debug build on the connected device and opens the main menu. If more than one device is connected, set `ANDROID_SERIAL` to the phone's serial from `adb devices`.

Without a cable, copy `app/build/outputs/apk/debug/app-debug.apk` to the phone, open it there, and allow installs from that app when Android asks.

## Run on an emulator

Requires the Android SDK at `$ANDROID_HOME` (for example `~/Library/Android/sdk`) and the setup below.

### Dependencies

- JDK 17 (for example `brew install --cask temurin@17`)
- Android SDK packages, installed with the SDK's `sdkmanager`:
  - `platform-tools`
  - `emulator`
  - `platforms;android-35`
  - `build-tools;35.0.0`
  - `system-images;android-35;google_apis;arm64-v8a` (use `x86_64` on Intel Macs and Linux PCs)
  - `cmdline-tools;latest`

### One-off setup

```
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n tenor_test -k "system-images;android-35;google_apis;arm64-v8a" -d "pixel_6"
```

### Run

```
./gradlew runEmulator   # starts the emulator with a window; leave it running
./gradlew launchApp     # in a second terminal: installs the app and opens the main menu
```

Run `launchApp` again after each code change to reinstall. For manual commands and troubleshooting, see [EMULATOR.md](EMULATOR.md).
