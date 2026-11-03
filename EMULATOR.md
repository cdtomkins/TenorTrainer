# Emulator notes

Extra detail for the emulator steps in the [README](README.md). You only need this if the Gradle tasks don't work for you.

## Gradle tasks

The tasks in `build.gradle.kts` read the SDK path from `local.properties`, or from `ANDROID_HOME` if that's missing. List them with:

```
./gradlew tasks --group emulator
```

They start the AVD named `tenor_test`. To use another AVD, change `testAvd` in `build.gradle.kts`.

## Manual commands

```
$ANDROID_HOME/emulator/emulator -avd tenor_test -no-snapshot-save -no-boot-anim
./gradlew assembleDebug
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.cdtomkins.tenortrainer/.MainActivity
```

## Troubleshooting

- **Slow emulator or graphics glitches:** add `-gpu swiftshader_indirect` to the emulator command.
- **Audio problems:** add `-no-audio`.
- **Use the SDK's `avdmanager`, not Homebrew's.** The Homebrew one fails with "Package path is not valid". The SDK's copy is at `$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager`.
- **Different device profile:** run `avdmanager list device` and use another name with `-d` when creating the AVD.
