# Network Listener Plugin - Demo Transcript

Welcome to the Network Listener Plugin demo. This plugin receives telemetry data over network connections and visualizes it on the ATAK map in real-time.

The plugin supports two network protocols: TCP on port 8888 and UDP on port 9999. You can start either server from the plugin interface. Once running, the plugin listens for incoming connections and data packets containing JSON-formatted telemetry.

To use the plugin, click "Network Listener" in the ATAK toolbar. The interface provides buttons to start TCP or UDP servers, stop the servers, and send test data. When telemetry data arrives, it's parsed from JSON format and displayed in a log list showing the timestamp, protocol type, robot identifier, position coordinates, speed, and temperature.

Most importantly, the plugin creates or updates a marker on the ATAK map for each telemetry update. The marker shows the robot's identifier, current speed, and temperature. As new telemetry arrives, the marker position updates automatically, providing real-time visualization of remote sensor data.

The expected JSON format includes fields for id, latitude, longitude, speed, and temperature. You can test the plugin using the built-in test button, which generates random telemetry near the map center, or send data from external systems using standard network tools.

This plugin demonstrates integration with external telemetry systems, showing how ATAK can visualize real-time sensor data from robots, drones, or other networked devices.

That's the Network Listener Plugin in sixty seconds. Thank you for watching.

