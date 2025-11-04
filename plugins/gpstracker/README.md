# GPS Tracker Plugin

## Purpose

Simulates GPS tracking by generating periodic CoT events for a simulated robot moving in a circular pattern. Provides UI controls for managing ATAK action bars and map control widgets.

## Features

- GPS simulation generating CoT events for "SimBot"
- Circular movement pattern around map center
- Broadcasts CoT events for other plugins to consume
- UI controls for showing/hiding action bars
- UI controls for enabling/disabling action bar buttons
- UI controls for showing/hiding map control widgets

## Usage

1. Launch ATAK
2. Click "GPS Tracker Half" or "GPS Tracker Full" in the toolbar
3. SimBot will begin generating CoT events automatically
4. Use UI controls to manage action bars and widgets

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
- Speed: 5.0 m/s, Radius: 0.0005 degrees
