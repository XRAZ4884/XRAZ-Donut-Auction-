# Donut Auction — Made by XRAZ

A free, client-side Minecraft Fabric utility for streamers running viewer auctions. It is a local timer and display tool: it does **not** verify Donut SMP prices, send economy commands, change server data, or manipulate packets.

## Requirements
- Minecraft Java Edition **1.21.11**
- Java 21
- Fabric Loader 0.18.1 or compatible
- Fabric API for 1.21.11

## Build
1. Install Java 21.
2. Open this folder in IntelliJ IDEA, VS Code, or a terminal.
3. Run `./gradlew build` (Windows: `gradlew.bat build`).
4. The mod jar will be in `build/libs/`.

The included `gradlew` / `gradlew.bat` scripts bootstrap Gradle 8.14.3 on first run, so no wrapper JAR is needed. Internet access is required on that first run.

## Install
1. Build the project.
2. Copy `build/libs/donut-auction-1.0.0.jar` into your Minecraft `mods` folder.
3. Start Minecraft 1.21.11 using the Fabric profile with Fabric API installed.

## Use
- **Right Shift** opens/closes the dashboard.
- **Right Control** starts or pauses/resumes the timer.
- Reset key is unassigned by default.
- Change bindings under **Options → Controls → Key Binds → Donut Auction**.
- Select an item, set a custom display name, starting price, estimated value, minimum, quantity and duration in seconds.
- The compact overlay appears during gameplay. Use Settings to toggle it, change scale, or make its background transparent.
- Save and load presets from Presets. Export history as CSV from History.
- Data is stored in `.minecraft/config/donut-auction/data.json`; CSV exports to `auction-history.csv`.

## Notes
- Auction timing uses a wall-clock delta, so it continues while the GUI is closed. If the game is restarted during a running auction, it is restored paused for safety.
- Item search uses the active game's item registry.
- Price/value fields are manual estimates, **not live Donut SMP market prices**.
- All controls are local. No server commands or packets are sent by this mod.
- Overlay position defaults to the top-left. `overlayX` and `overlayY` can be adjusted in `data.json`.
- Made by XRAZ. No ads, telemetry, accounts, payments, license keys, or artificial usage limits.
