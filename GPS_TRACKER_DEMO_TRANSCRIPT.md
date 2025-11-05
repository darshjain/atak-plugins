# GPS Tracker Plugin - Demo Transcript

Welcome to the GPS Tracker Plugin demo. This plugin simulates GPS tracking by generating synthetic CoT events for a simulated robot called SimBot.

When you start the plugin, SimBot begins moving in a circular pattern around the center of your current map view. The plugin generates CoT events every second, broadcasting location updates that other plugins and ATAK components can receive and display.

To use the plugin, simply click either "GPS Tracker Half" or "GPS Tracker Full" in the ATAK toolbar. The half and full options refer to the size of the control pane that opens. Once the plugin is started, SimBot moves at approximately five meters per second in a circular pattern with a radius of about fifty-five meters.

The plugin also provides UI controls for managing ATAK's interface elements. You can show or hide the action bar, enable or disable action bar buttons, and control the visibility of map control widgets. These controls are useful for testing and customization purposes.

The simulated CoT events include all standard fields: location coordinates, timestamp, callsign set to "SimBot", and speed information. Other plugins, like the CoT Viewer, can receive and display these events just as they would with real GPS data.

This plugin is ideal for testing other plugins that consume CoT data, demonstrating CoT message flow, or testing map visualization features without requiring actual GPS hardware.

That's the GPS Tracker Plugin in short. Thank you for watching.

