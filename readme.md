ATAK Plugins
============

Overview
--------
This repository contains example ATAK plugins, including `cotviewer`, `gpstracker`, and `networklistener`.

Requirements
------------
- Android Studio (compatible with the ATAK SDK version used here)
- Gradle (wrapper included)
- ATAK SDK and dependencies per `README_atak_SDK.md`

Getting Started
---------------
1. Open Android Studio.
2. Use "Open" to select a plugin folder under `plugins/` (for example, `plugins/cotviewer`).
3. Let Gradle sync and build.
4. Use the `civDebug` build variant to assemble and install on a device with ATAK.

Build (CLI)
-----------
From a plugin directory (e.g., `plugins/cotviewer`):

```
./gradlew assembleCivDebug
```

Project Structure
-----------------
- `plugins/` – Individual plugin projects
- `docs/` – Documentation and references
- `license/` – Licensing information

License
-------
See files under `license/`.


