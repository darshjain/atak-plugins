# CoT Viewer Plugin

## Purpose

Displays CoT (Cursor on Target) messages received via broadcast in ATAK. Shows real-time CoT events with type, callsign, timestamp, and coordinates. Includes filtering to show only tracked items.

## Features

- Receives CoT events via broadcast messages
- Displays type, callsign, timestamp, and coordinates
- **Manual tracking**: Long-press any object to toggle tracking
- Filters tracked items (both auto-tracked and manually tracked)
- Visual indicators:
  - **Orange background**: Auto-tracked objects (detected from CoT XML)
  - **Blue background**: Manually tracked objects (set by user)
- Real-time updates as CoT events are received

## Usage

1. Launch ATAK
2. Click "CoT Viewer" in the toolbar
3. View CoT messages in the list
4. **Long-press any item** to track/untrack it manually
5. Check "Only Tracked" to filter tracked items (shows both auto-tracked and manually tracked)

## Compilation

```bash
cd plugins/cotviewer
./gradlew assembleCivDebug
```

## Installation

```bash
adb install -r app/build/outputs/apk/civ/debug/ATAK-Plugin-cotviewer-*.apk
```

## Technical Details

- Listens for CoT broadcasts on action: `com.atakmap.android.cotviewer.COT_EVENT`
- Parses CoT XML to extract callsign and track information
- Updates UI on main thread via executor service
