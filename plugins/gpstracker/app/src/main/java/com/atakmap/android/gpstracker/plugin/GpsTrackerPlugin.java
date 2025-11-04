package com.atakmap.android.gpstracker.plugin;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.navigation.views.NavView;
import com.atakmap.android.navigation.views.buttons.NavButtonsVisibilityListener;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

import com.atakmap.android.gpstracker.plugin.R;


public class GpsTrackerPlugin implements IPlugin {


    IServiceController serviceController;
    Context pluginContext;
    IHostUIService uiService;
    ToolbarItem toolbarItemHalf;
    ToolbarItem toolbarItemFull;
    Pane templatePaneHalf;
    Pane templatePaneFull;

    private GpsSimulatorService gpsSimulator;

    public GpsTrackerPlugin(IServiceController serviceController) {

        this.serviceController = serviceController;
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }

        uiService = serviceController.getService(IHostUIService.class);

        toolbarItemHalf = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name)  + " Half",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        templatePaneHalf = showPane(templatePaneHalf, 0.5D);
                    }
                })
                .build();
        toolbarItemFull = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name)  + " Full",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        templatePaneFull = showPane(templatePaneFull, 1.0D);
                    }
                })
                .build();
    }

    @Override
    public void onStart() {
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItemHalf);
        uiService.addToolbarItem(toolbarItemFull);

        MapView mapView = MapView.getMapView();
        if (mapView != null) {
            if (this.gpsSimulator == null) {
                this.gpsSimulator = new GpsSimulatorService(mapView);
            }
            this.gpsSimulator.startSimulation();
        }
    }

    @Override
    public void onStop() {
        if (uiService == null)
            return;

        uiService.removeToolbarItem(toolbarItemHalf);
        uiService.removeToolbarItem(toolbarItemFull);

        if (this.gpsSimulator != null) {
            this.gpsSimulator.stopSimulation();
            this.gpsSimulator = null;
        }
    }

    private Pane showPane(Pane p, double ratio) {
        if(p == null) {
            final NavView actionBar = NavView.getInstance();
            final boolean[] actionBarVisible = new boolean[] {actionBar.buttonsVisible()};
            final boolean[] actionBarEnabled = new boolean[] {!actionBar.buttonsLocked()};

            View mainLayout = PluginLayoutInflater.inflate(pluginContext,
                    R.layout.main_layout, null);
            p = new PaneBuilder(mainLayout)
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, ratio)
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, ratio)
                    .build();

            final TextView actionBarVisibleTextView = mainLayout.findViewById(R.id.actionBarVisibilityTextView);
            actionBar.addButtonVisibilityListener(new NavButtonsVisibilityListener() {
                @Override
                public void onNavButtonsVisible(boolean b) {
                    actionBarVisibleTextView.setText("Action Bar Visibility: " +
                                (actionBar.buttonsVisible() ? "Visible" : "Not Visible"));
                }
            });
            actionBarVisibleTextView.setText("Action Bar Visibility: " +
                    (actionBar.buttonsVisible() ? "Visible" : "Not Visible"));

            final Button actionBarShow = mainLayout.findViewById(R.id.showActionBarButton);
            actionBarShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(actionBar.buttonsVisible()) return;

                    Intent showButtons = new Intent(NavView.TOGGLE_BUTTONS);
                    AtakBroadcast.getInstance().sendBroadcast(showButtons);
                }
            });

            final Button actionBarHide = mainLayout.findViewById(R.id.hideActionBarButton);
            actionBarHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!actionBar.buttonsVisible()) return;

                    Intent hideButtons = new Intent(NavView.TOGGLE_BUTTONS);
                    AtakBroadcast.getInstance().sendBroadcast(hideButtons);
                }
            });

            final TextView actionBarButtonsEnabledTextView = mainLayout.findViewById(R.id.actionBarEnabledTextView);
            actionBarButtonsEnabledTextView.setText("ActionBar Buttons Enabled: " + !actionBar.buttonsLocked());

            final Button actionBarEnable = mainLayout.findViewById(R.id.enableActionBarButton);
            actionBarEnable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!actionBar.buttonsLocked()) return;

                    Intent enableButtons = new Intent(NavView.LOCK_BUTTONS);
                    enableButtons.putExtra("lock", false);
                    AtakBroadcast.getInstance().sendBroadcast(enableButtons);

                    actionBarButtonsEnabledTextView.setText("Action Bar Buttons Enabled: " + true);
                }
            });

            final Button actionBarDisable = mainLayout.findViewById(R.id.disableActionBarButton);
            actionBarDisable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(actionBar.buttonsLocked()) return;

                    Intent disableButtons = new Intent(NavView.LOCK_BUTTONS);
                    disableButtons.putExtra("lock", true);
                    AtakBroadcast.getInstance().sendBroadcast(disableButtons);

                    actionBarButtonsEnabledTextView.setText("Action Bar Buttons Enabled: " + false);
                }
            });

            final TextView mapControlWidgetsVisibleTextView = mainLayout.findViewById(R.id.mapControlWidgetsVisibleTextView);
            mapControlWidgetsVisibleTextView.setText("Map Control Widgets Visible: " +
                    (actionBar.findViewById(com.atakmap.app.R.id.side_layout).getVisibility() == View.VISIBLE));

            final Button mapControlWidgetsShow = mainLayout.findViewById(R.id.showMapControlWidgetsButton);
            mapControlWidgetsShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View widgets = actionBar.findViewById(com.atakmap.app.R.id.side_layout);
                    if(widgets.getVisibility() == View.VISIBLE) return;

                    widgets.setVisibility(View.VISIBLE);
                    mapControlWidgetsVisibleTextView.setText("Map Control Widgets Visible: " + true);
                }
            });

            final Button mapControlWidgetsHide = mainLayout.findViewById(R.id.hideMapControlWidgetsButton);
            mapControlWidgetsHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View widgets = actionBar.findViewById(com.atakmap.app.R.id.side_layout);
                    if(widgets.getVisibility() == View.GONE) return;

                    widgets.setVisibility(View.GONE);
                    mapControlWidgetsVisibleTextView.setText("Map Control Widgets Visible: " + false);
                }
            });
        }

        if(!uiService.isPaneVisible(p)) {
            uiService.showPane(p, null);
        }

        return p;
    }
}