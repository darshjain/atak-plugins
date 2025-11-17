ATAK Plugins
============

Overview
--------
This repository contains three custom ATAK plugins for enhanced situational awareness and data visualization:

- **CoT Viewer** - View and track Cursor-on-Target (CoT) events from entities on the map
- **GPS Tracker** - Simulate GPS-enabled robots broadcasting position updates in real-time
- **Network Listener** - Receive and visualize telemetry data from external systems via TCP/UDP

These plugins demonstrate CoT event handling, map marker management, network communication, and ATAK plugin architecture.

Table of Contents
-----------------
- [Quick Start](#quick-start)
- [Requirements](#requirements)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
  - [Manual Installation](#manual-installation-physical-android-device)
  - [ADB Installation](#adb-installation-developer-method)
- [Loading and Activating Plugins](#loading-and-activating-plugins-in-atak)
- [Testing the Plugins](#testing-the-plugins)
  - [GPS Tracker](#1-gps-tracker-plugin)
  - [CoT Viewer](#2-cot-viewer-plugin)
  - [Network Listener](#3-network-listener-plugin)
- [Integration Testing](#integration-testing)
- [Project Structure](#project-structure)
- [Key Features](#key-features)

Quick Start
-----------
For end users who want to install and use the plugins immediately:

1. Copy the `finalAPKs/` folder to your Android device
2. Install ATAK APK (`Atak.apk`)
3. Install all three plugin APKs
4. Open ATAK → Settings → Plugin Management → Enable plugins
5. Use plugin buttons in the toolbar

See **Manual Installation** section below for detailed step-by-step instructions.

Requirements
------------
- Android Studio (compatible with the ATAK SDK version used here)
- Gradle (wrapper included)
- ATAK SDK and dependencies per `README_atak_SDK.md`

Tech Stack
----------

**Languages:**
- Java
- XML
- Gradle

**Platform:**
- ATAK SDK 5.4.0
- Android SDK
- Gradle 8.13

**Key Libraries:**
- ATAK APIs (IPlugin, MapView, CotEvent, UI components)
- Android networking (ServerSocket, DatagramSocket)
- Android UI widgets

**Tools:**
- Android Studio
- ADB (Android Debug Bridge)
- Git
- Bluestacks

**Networking:**
- TCP (port 8888)
- UDP (port 9999)
- JSON data format

Getting Started
---------------
1. Open Android Studio.
2. Use "Open" to select a plugin folder under `plugins/` (for example, `plugins/cotviewer`).
3. Let Gradle sync and build.
4. Use the `civDebug` build variant to assemble and install on a device with ATAK.

Build (CLI)
-----------
From a plugin directory (e.g., `plugins/cotviewer`):

```bash
./gradlew assembleCivDebug
```

Installation
------------

### Prerequisites

Before installing the plugins, you need to have ATAK installed on your Android device.

**Note:** Make sure your Android device/emulator is connected and ADB is configured. Verify with:
```bash
adb devices
```

### Manual Installation (Physical Android Device)

**Simple method without using command line:**

1. **Prepare the APKs:**
   - All APK files are located in the `finalAPKs/` folder
   - You need: `Atak.apk` + 3 plugin APKs (cotviewer, gpstracker, networklistener)
   - All files are included and ready to install

2. **Transfer APKs to your Android device:**
   - Connect your phone to your computer via USB
   - Copy the entire `finalAPKs/` folder to your phone's `Downloads` folder
   - Or use cloud storage (Google Drive, Dropbox) to transfer files
   - Or email the APKs to yourself and download on your phone

3. **Enable "Install Unknown Apps" on your Android device:**
   - Go to **Settings** → **Security** (or **Apps**)
   - Find **Install unknown apps** (or **Unknown sources**)
   - Enable it for **Files** app, **Chrome**, or whichever app you'll use to open the APKs

4. **Install ATAK first:**
   - Open your **Files** app or **Downloads** folder
   - Navigate to the `finalAPKs/` folder
   - Tap on **Atak.apk**
   - Tap **Install**
   - Wait for installation to complete
   - Open ATAK to verify it works

5. **Install the three plugins:**
   - In the same `finalAPKs/` folder, tap on each plugin APK one by one:
     - `ATAK-Plugin-cotviewer-*.apk`
     - `ATAK-Plugin-gpstracker-*.apk`
     - `ATAK-Plugin-networklistener-*.apk`
   - Tap **Install** for each one
   - Wait for each to complete

6. **Launch ATAK:**
   - Open ATAK
   - Look for the plugin buttons in the toolbar:
     - **"CoT Viewer"**
     - **"Start GPS Simulation"** / **"Stop GPS Simulation"**
     - **"Network Listener"**

### ADB Installation (Developer Method)

If you prefer using ADB from your computer:

#### Step 1: Install ATAK

```bash
adb install -r finalAPKs/Atak.apk
```

Launch ATAK on your device to ensure it's working properly before installing plugins.

#### Step 2: Install Pre-built Plugins from finalAPKs Folder

```bash
# Install all three plugins from finalAPKs folder
adb install -r finalAPKs/ATAK-Plugin-cotviewer-*.apk
adb install -r finalAPKs/ATAK-Plugin-gpstracker-*.apk
adb install -r finalAPKs/ATAK-Plugin-networklistener-*.apk
```

#### Option B: Build and Install Plugins from Source

From the root directory, build and install all three plugins:

```bash
# Build all plugins
cd plugins/cotviewer && ./gradlew assembleCivDebug && cd ../..
cd plugins/gpstracker && ./gradlew assembleCivDebug && cd ../..
cd plugins/networklistener && ./gradlew assembleCivDebug && cd ../..

# Install all plugins via adb
adb install -r plugins/cotviewer/app/build/outputs/apk/civ/debug/ATAK-Plugin-cotviewer-*.apk
adb install -r plugins/gpstracker/app/build/outputs/apk/civ/debug/ATAK-Plugin-gpstracker-*.apk
adb install -r plugins/networklistener/app/build/outputs/apk/civ/debug/ATAK-Plugin-networklistener-*.apk
```

**Build and install a single plugin (e.g., GPS Tracker):**

```bash
cd plugins/gpstracker
./gradlew assembleCivDebug
adb install -r app/build/outputs/apk/civ/debug/ATAK-Plugin-gpstracker-*.apk
```

**Note:** The `-r` flag replaces existing installations.

Loading and Activating Plugins in ATAK
---------------------------------------

After installing the plugins, you need to load and activate them within ATAK:

### Step 1: Open ATAK Plugin Manager

1. **Launch ATAK** on your device
2. Tap the **overflow menu** (three dots or hamburger icon) in the top-right corner
3. Select **Settings** from the menu
4. Scroll down and tap on **Plugin Management** (or **Manage Plugins**)

### Step 2: Load Plugins

1. In the Plugin Management screen, you'll see a list of installed plugins:
   - **CoT Viewer**
   - **GPS Tracker** (or gpstracker)
   - **Network Listener** (or networklistener)

2. **Enable each plugin:**
   - If a plugin has a checkbox, check it to enable
   - If a plugin has a toggle switch, turn it ON
   - Some plugins may auto-load on first launch

3. Each enabled plugin should show as "Loaded" or "Active"

### Step 3: Verify Plugins are Active

1. **Go back to the main ATAK screen** (tap back arrow or home)
2. Look for the plugin buttons in the toolbar:
   - **"CoT Viewer"** button - Opens the CoT event viewer
   - **"Start GPS Simulation"** button - Starts SimBot simulation
   - **"Network Listener"** button - Opens the network listener panel

3. If you don't see the buttons:
   - Return to Plugin Management and verify plugins are enabled
   - **Restart ATAK** completely (close and reopen the app)
   - Check that plugins were installed successfully

### Step 4: Grant Permissions (if prompted)

When you first use each plugin, Android may prompt for permissions:
- **Location** - Grant for GPS Tracker
- **Network** - Grant for Network Listener
- **Storage** - Grant if requested

Tap **Allow** or **Grant** for each permission request.

### Troubleshooting

**Plugins not showing in Plugin Management:**
- Verify APKs were installed (check Settings → Apps → Show all apps)
- Reinstall plugins using the installation steps above
- Ensure ATAK version is compatible with the plugins

**Plugins enabled but buttons not visible:**
- Restart ATAK completely
- Check ATAK logs (Settings → About → View Logs)
- Try disabling and re-enabling the plugin

**Plugin buttons visible but not working:**
- Check permissions are granted
- Try stopping and restarting the plugin
- Check device logs: `adb logcat -s CotViewerPlugin GpsTrackerPlugin NetworkListenerPlugin`

Testing the Plugins
-------------------

### 1. GPS Tracker Plugin

**Purpose:** Simulates a moving robot (SimBot) that broadcasts GPS position updates via CoT events.

**How to Test:**

1. Launch ATAK on your device
2. Look for the **"Start GPS Simulation"** button in the toolbar
3. Click **"Start GPS Simulation"**
   - SimBot will appear at a fixed off-center position on the map
   - SimBot will move in a circular pattern around that position
   - Position updates are sent every second
4. Click **"Stop GPS Simulation"**
   - SimBot marker will disappear from the map
   - CoT events stop broadcasting

**Expected Behavior:**
- SimBot appears at a fixed location (shifted east from center)
- Moves in a smooth circular path
- Broadcasts CoT events with UID: `SIMBOT-001`, Type: `a-f-A-M-H`
- Marker is removed cleanly when simulation stops

---

### 2. CoT Viewer Plugin

**Purpose:** Displays all incoming CoT events and allows you to track specific entities.

**How to Test:**

1. Launch ATAK and ensure GPS Tracker plugin is installed
2. Click the **"CoT Viewer"** button in the toolbar to open the viewer pane
3. Start the GPS Simulation (see GPS Tracker steps above)
4. Watch CoT events appear in the CoT Viewer list
   - Each entry shows: callsign, type, timestamp, and coordinates
   - SimBot events will be listed with callsign "SimBot"

**Tracking Feature:**

1. **Long-press** (press and hold) any entry in the CoT Viewer list
2. A toast message will appear: "Now tracking [callsign]"
3. The tracked entry will turn **blue**
4. Check the **"Only Tracked"** checkbox at the top to filter and show only tracked items
5. Long-press again to stop tracking

**Color Coding:**
- **Blue background:** Manually tracked items
- **Transparent:** Not tracked

---

### 3. Network Listener Plugin

**Purpose:** Receives telemetry data via TCP or UDP and displays it on the ATAK map.

**How to Test:**

1. Click the **"Network Listener"** button in the toolbar to open the listener pane
2. Choose either:
   - **Start TCP (Port 8888)** – Starts a TCP server
   - **Start UDP (Port 9999)** – Starts a UDP server
3. The status will update to show "TCP/UDP Server listening on port XXXX"

**Send Test Data:**

Click the **"Send Test Data"** button to generate simulated telemetry:
- Creates a random position near the current map center
- Shows in the telemetry log
- Places a marker on the map

**Send Data via Network:**

From your development machine, send JSON telemetry data:

```bash
# TCP Example (port 8888)
echo '{"id":"Robot1","lat":37.7749,"lon":-122.4194,"speed":15.5,"temperature":22.3}' | nc localhost 8888

# UDP Example (port 9999)
echo '{"id":"Drone1","lat":37.7749,"lon":-122.4194,"speed":25.0,"temperature":18.5}' | nc -u localhost 9999
```

**JSON Format:**
```json
{
  "id": "Robot1",
  "lat": 37.7749,
  "lon": -122.4194,
  "speed": 15.5,
  "temperature": 22.3
}
```

**Stop Server:**

Click **"Stop Server"** button:
- Server stops listening
- TCP/UDP sockets are closed
- Ports are released (verified by the fix)
- Status updates to "Stopped"

**Expected Behavior:**
- Telemetry data appears in the log with timestamp
- Markers are placed/updated on the map at received coordinates
- Port is properly released when server stops (no "port still active" errors)

---

Integration Testing
-------------------

**Test all plugins together:**

1. Start **Network Listener** (TCP or UDP)
2. Start **GPS Tracker** simulation
3. Open **CoT Viewer**
4. Verify:
   - GPS Tracker CoT events appear in CoT Viewer
   - SimBot moves on the map
   - Can track SimBot via long-press in CoT Viewer
   - Network Listener can receive external data simultaneously
5. Stop GPS Tracker
   - SimBot disappears from map
   - CoT events stop appearing in viewer
6. Stop Network Listener
   - Server stops cleanly
   - Port is released

Project Structure
-----------------
- `plugins/` – Individual plugin projects
  - `cotviewer/` – CoT event viewer and tracker
  - `gpstracker/` – GPS simulation with circular motion
  - `networklistener/` – TCP/UDP telemetry receiver
- `finalAPKs/` – Pre-built APK files for easy installation
  - Contains ATAK APK (`Atak.apk`) and all three plugin APKs
  - Ready for distribution and installation

Key Features
------------
- **Real-time CoT event tracking** with visual indicators
- **GPS simulation** with customizable circular motion patterns
- **Network telemetry ingestion** supporting both TCP and UDP protocols
- **Map marker management** with automatic cleanup
- **Plugin lifecycle handling** with proper resource management
- **Broadcast receiver integration** for inter-plugin communication

