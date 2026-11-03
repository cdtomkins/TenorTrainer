plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

// Manual emulator testing. Run `./gradlew runEmulator` in one terminal, then `./gradlew launchApp` in another.
val sdkDir: String = java.util.Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}.getProperty("sdk.dir") ?: System.getenv("ANDROID_HOME") ?: error("Set sdk.dir in local.properties or ANDROID_HOME")
val emulatorBin = "$sdkDir/emulator/emulator"
val adbBin = "$sdkDir/platform-tools/adb"
val testAvd = "tenor_test"

tasks.register<Exec>("runEmulator") {
    group = "emulator"
    description = "Starts the $testAvd emulator with a window. Closing the window stops it."
    commandLine(emulatorBin, "-avd", testAvd, "-no-snapshot-save", "-no-boot-anim", "-gpu", "swiftshader_indirect")
}

val waitForEmulatorBoot = tasks.register<Exec>("waitForEmulatorBoot") {
    group = "emulator"
    description = "Waits until the emulator has finished booting."
    commandLine(
        "sh", "-c",
        "\"$adbBin\" wait-for-device && " +
            "until [ \"\$(\"$adbBin\" shell getprop sys.boot_completed | tr -d '\\r')\" = 1 ]; do sleep 2; done",
    )
}

tasks.register<Exec>("launchApp") {
    group = "emulator"
    description = "Installs the debug app on the running emulator and opens the main menu."
    dependsOn(waitForEmulatorBoot, ":app:installDebug")
    commandLine(adbBin, "shell", "am", "start", "-n", "com.cdtomkins.tenortrainer/.MainActivity")
}

project(":app").tasks.configureEach {
    if (name == "installDebug") mustRunAfter(waitForEmulatorBoot)
}
