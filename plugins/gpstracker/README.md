# GPS Tracker Plugin

## Purpose

Simulates GPS tracking by generating periodic CoT events for a simulated robot moving in a circular pattern. Provides start/stop controls for manual simulation control.

## Features

- GPS simulation generating CoT events for "SimBot"
- Circular movement pattern around map center
- Manual start/stop control via toolbar buttons
- Broadcasts CoT events for other plugins to consume
- Dispatches CoT events through both internal dispatcher and broadcast

## Usage

1. Launch ATAK
2. Click "Start GPS Simulation" in the toolbar to begin simulation
3. SimBot will begin generating CoT events automatically
4. Click "Stop GPS Simulation" to stop the simulation

## Compilation

```bash
cd plugins/gpstracker
./gradlew assembleCivDebug
```

## Installation

```bash
adb install -r app/build/outputs/apk/civ/debug/ATAK-Plugin-gpstracker-*.apk
```

## Technical Details

- Generates CoT events every second with circular motion
- Uses UID: SIMBOT-001, Type: a-f-A-M-H
- Broadcasts CoT events via: `com.atakmap.android.cotviewer.COT_EVENT`
- Also dispatches through CotMapComponent internal dispatcher
- Speed: 5.0 m/s, Radius: 0.0005 degrees
- Angle increment: 10 degrees per second
