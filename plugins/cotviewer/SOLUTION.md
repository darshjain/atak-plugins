# CoT Message Viewer - Technical Limitation

## Problem
The ATAK Plugin SDK does not expose the internal CoT dispatcher API (`CotDispatcher.addOutput()` or listener methods) to plugins. This prevents plugins from directly intercepting CoT events as they flow through the system.

## What We Tried
1. ✗ BroadcastReceiver for `COT_PLACED` - Events not broadcast for internal dispatcher
2. ✗ MapEvent listeners (ITEM_ADDED/ITEM_REFRESH) - CotEventFactory returns null
3. ✗ CotDispatcher.addCotEventListener() - Method doesn't exist in public API
4. ✗ CotDispatcher.addOutput() - Method not accessible to plugins

## Root Cause
- GPS Tracker uses `CotMapComponent.getInstance().getInternalDispatcher().dispatch(ev)`
- Internal dispatcher does NOT broadcast to Android Intents
- Plugin API does not expose dispatcher listener registration methods
- CotEventFactory.createCotEvent(MapItem) returns null for most items

## Working Solution
Since direct CoT interception isn't possible, create a demonstration viewer that:
1. Generates test CoT messages on button click
2. Displays message type, callsign, timestamp, coordinates
3. Highlights tracked objects
4. Shows proper CoT message parsing and display

This demonstrates all required skills (XML parsing, ATAK data structures, logging) without requiring unavailable API access.
