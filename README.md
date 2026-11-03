# N Buggy bench test 0.1

This package is source code, not a compiled APK or flashed controller.
No GPIO is configured or driven. No motor, shaker, real sensor, dosing weight,
remote alert service or manual selector input is implemented in this bench build.
Disconnect actuator hardware for this first test.

## Load the ESP32 using the installed Arduino IDE

1. Open firmware/NBuggyTest/NBuggyTest.ino (keep interface.h beside it).
2. Select the actual ESP32 board model and COM11. For a standard original
   ESP32-WROOM development board, use ESP32 Dev Module. Do not use that target
   for a C3, S3, P4 or other variant.
3. Verify, then Upload. This replaces existing firmware. Serial Monitor: 115200.
4. The board prints its N-Buggy-Test-xxxxxx Wi-Fi name and 192.168.4.1 address.

## Connect from Android

Join the N-Buggy-Test-xxxxxx Wi-Fi network. Password: NBuggyTest26
Choose to stay connected if Android says the network has no internet.
Open http://192.168.4.1 in a browser, or use the Android app after building it.
Controller login: admin / NBuggyTest26
These are bench-only shared credentials; do not expose this service publicly.

The Android app opens system Wi-Fi settings for network selection and password
entry. It stores the controller address and binds its connection to Wi-Fi so a
mobile data connection does not take precedence over the offline ESP32 network.
It does not provision a router SSID/password into the controller. The controller
creates its own hotspot in this version. Bluetooth is not implemented.

## Trial sequence

1. Confirm STOPPED. Defaults: ON 5 s, OFF 15 s, no-flow delay 3 s.
2. Start cycle. Confirm alternating FEEDING and WAITING.
3. Select No product while feeding. Confirm NO FLOW within the configured delay,
   with the cycle stopped. If selected while waiting, checking begins next feed.
4. Select Product flowing, Reset fault, then Start cycle.
5. Stop, change settings and Save. Reload to confirm settings on the controller.
6. Power cycle: settings should remain, operation should start STOPPED.
7. Disconnect Wi-Fi: interface should show disconnected/state unknown.
   The ESP32's bench cycle continues independently; reconnect to read its state.
8. Try ON 10 / OFF 10: reject because OFF must be at least 3 x ON.

The simulated product signal defaults to flowing after every reboot.
Manual fault reset is only a test action, not proof of a physically cleared fault.
Timing settings cannot be changed while running. No shaker control is included.

## Android build

Requires JDK 17, Android SDK platform 35/build-tools 35.0.0, Gradle 8.9.
Open android/ in Android Studio, sync, then Build APK, or run:

    gradle -p android assembleDebug

APK output: android/app/build/outputs/apk/debug/app-debug.apk
The included .github/workflows/build.yml can build the APK and verify firmware
when the contents of this folder are placed at a GitHub repository root.
No repository has been created or uploaded as part of this package.

## Validation status

Sources prepared on 3 November 2026. The installed ESP32 core is 3.3.12.
Local compilation was attempted but the compiler could not launch in the
restricted execution session (Illegal System DLL Relocation). Android SDK/JDK
were unavailable, and download host DNS was unavailable. Neither binary has
been built or hardware-tested here. Run the checks above before further work.

## Next stage

Add real sensor conditioning, isolated motor interfaces and physical selector
inputs only after board model, wiring and electrical ratings are confirmed.
Calibrated dosing, shaker recovery and Radxa integration follow the basic trial.
