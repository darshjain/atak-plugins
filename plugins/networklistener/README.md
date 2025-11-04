# Network Listener Plugin

## Purpose

Receives telemetry data via TCP or UDP and visualizes it on the ATAK map. Parses JSON telemetry data and updates map markers in real-time.

## Features

- TCP server listening on port 8888
- UDP server listening on port 9999
- JSON telemetry parsing
- Real-time marker visualization on ATAK map
- Telemetry log display
- Test data generation

## Usage

1. Launch ATAK
2. Click "Network Listener" in the toolbar
3. Click "Start TCP" or "Start UDP" to begin listening
4. Send JSON telemetry data to the appropriate port
5. View telemetry in the log and on the map

## JSON Telemetry Format

```json
{
  "id": "Robot-Alpha",
  "lat": 34.052235,
  "lon": -118.243683,
  "speed": 25.5,
  "temperature": 22.3
}
```

Fields: id (string), lat (number), lon (number), speed (number), temperature (number)

## Compilation

```bash
cd plugins/networklistener
./gradlew assembleCivDebug
```

## Installation

```bash
adb install -r app/build/outputs/apk/civ/debug/ATAK-Plugin-networklistener-*.apk
```

## Ports Required

- TCP: 8888
- UDP: 9999

## Technical Details

- Uses ExecutorService for non-blocking network operations
- Updates UI via Handler from background threads
- Supports multiple TCP client connections
- Marker updates use main thread handler
