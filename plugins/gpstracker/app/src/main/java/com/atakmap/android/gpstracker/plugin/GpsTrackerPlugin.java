package com.atakmap.android.gpstracker.plugin;

import android.content.Context;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.maps.MapView;
import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;
import com.atakmap.android.gpstracker.plugin.R;

public class GpsTrackerPlugin implements IPlugin {

    private final IServiceController serviceController;
    private final IHostUIService uiService;
    private final Context pluginContext;
    private ToolbarItem startButton;
    private ToolbarItem stopButton;
    private GpsSimulatorService gpsSimulator;

    public GpsTrackerPlugin(IServiceController serviceController) {
        this.serviceController = serviceController;

        PluginContextProvider ctxProvider = serviceController.getService(PluginContextProvider.class);
        pluginContext = ctxProvider.getPluginContext();
        pluginContext.setTheme(R.style.ATAKPluginTheme);

        uiService = serviceController.getService(IHostUIService.class);

        startButton = new ToolbarItem.Builder("Start GPS Simulation",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        startSimulation();
                    }
                })
                .build();

        stopButton = new ToolbarItem.Builder("Stop GPS Simulation",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        stopSimulation();
                    }
                })
                .build();
    }

    @Override
    public void onStart() {
        if (uiService == null)
            return;
        uiService.addToolbarItem(startButton);
        uiService.addToolbarItem(stopButton);
    }

    @Override
    public void onStop() {
        if (uiService == null)
            return;
        uiService.removeToolbarItem(startButton);
        uiService.removeToolbarItem(stopButton);
        stopSimulation();
    }

    private void startSimulation() {
        if (gpsSimulator == null) {
            MapView mapView = MapView.getMapView();
            if (mapView != null) {
                gpsSimulator = new GpsSimulatorService(mapView);
            }
        }
        if (gpsSimulator != null) {
            gpsSimulator.startSimulation();
        }
    }

    private void stopSimulation() {
        if (gpsSimulator != null) {
            gpsSimulator.stopSimulation();
            gpsSimulator = null;
        }
    }
}
