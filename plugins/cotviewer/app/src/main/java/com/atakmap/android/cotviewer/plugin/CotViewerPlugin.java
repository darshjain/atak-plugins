package com.atakmap.android.cotviewer.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cotviewer.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

/**
 * CoT Message Viewer using the public CoT broadcast.
 * No dependency on CotDetail or inner listener types.
 */
public class CotViewerPlugin implements IPlugin {

    private final IServiceController sc;
    private final Context pluginCtx;
    private final IHostUIService ui;

    private Pane pane;
    private View paneRoot;             // keep root view; Pane has no getView() on some SDKs
    private ToolbarItem button;

    private ArrayAdapter<Row> adapter;
    private final ArrayList<Row> allRows = new ArrayList<>();

    private final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    // loose callsign and "tracked" detection from XML text
    private static final Pattern CALLSIGN_RE =
            Pattern.compile("<contact[^>]*\\bcallsign\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRACK_RE =
            Pattern.compile("\\btrack\\b|\\btype=.*?track", Pattern.CASE_INSENSITIVE);

    private static final String TAG = "CotViewerPlugin";
    private static final String COT_EVENT_ACTION = "com.atakmap.android.cotviewer.COT_EVENT";

    private final BroadcastReceiver cotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received CoT broadcast");
            try {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    CotEvent event = bundle.getParcelable("cotevent");
                    if (event != null) {
                        Log.d(TAG, "Processing CoT event: " + event.getType() + " uid: " + event.getUID());
                        Row r = from(event, event.toString());
                        if (r == null) return;

                        pluginCtx.getMainExecutor().execute(() -> {
                            allRows.add(0, r);
                            if (adapter != null) {
                                applyFilter();
                            }
                            Log.d(TAG, "Added CoT to list: " + r.callsign + ", total: " + allRows.size());
                        });
                    } else {
                        Log.w(TAG, "CotEvent is null in bundle");
                    }
                } else {
                    Log.w(TAG, "Bundle is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing CoT event", e);
            }
        }
    };

    public CotViewerPlugin(IServiceController sc) {
        this.sc = sc;
        PluginContextProvider p = sc.getService(PluginContextProvider.class);
        this.pluginCtx = p.getPluginContext();
        this.pluginCtx.setTheme(R.style.ATAKPluginTheme);

        this.ui = sc.getService(IHostUIService.class);

        this.button = new ToolbarItem.Builder(
                "CoT Viewer",
                MarshalManager.marshal(
                        pluginCtx.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showPane();
                    }
                })
                .build();
    }

    @Override
    public void onStart() {
        if (ui != null) ui.addToolbarItem(button);

        Log.d(TAG, "CotViewerPlugin starting, registering for CoT events");

        // Register broadcast receiver for CoT events
        AtakBroadcast.DocumentedIntentFilter filter = new AtakBroadcast.DocumentedIntentFilter();
        filter.addAction(COT_EVENT_ACTION, "CoT events from GPS Tracker and other plugins");
        AtakBroadcast.getInstance().registerReceiver(cotReceiver, filter);
        Log.d(TAG, "Registered for CoT broadcasts on action: " + COT_EVENT_ACTION);
    }

    @Override
    public void onStop() {
        // Unregister broadcast receiver
        try {
            AtakBroadcast.getInstance().unregisterReceiver(cotReceiver);
            Log.d(TAG, "Unregistered CoT broadcast receiver");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unregister receiver", e);
        }
        
        if (ui != null) ui.removeToolbarItem(button);
    }

    private void showPane() {
        if (pane == null) {
            paneRoot = PluginLayoutInflater.inflate(pluginCtx, R.layout.cot_log, null);

            ListView list = paneRoot.findViewById(R.id.list);
            CheckBox onlyTracked = paneRoot.findViewById(R.id.cb_only_tracked);

            adapter = new ArrayAdapter<Row>(pluginCtx, android.R.layout.simple_list_item_2, android.R.id.text1) {
                @Override
                public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                    android.view.View row = super.getView(position, convertView, parent);
                    Row r = getItem(position);
                    android.widget.TextView t1 = row.findViewById(android.R.id.text1);
                    android.widget.TextView t2 = row.findViewById(android.R.id.text2);
                    t1.setText(r.titleLine());
                    t2.setText(r.detailLine());
                    row.setBackgroundColor(r.tracked ? 0x22FF9800 : 0x00000000);
                    return row;
                }
            };
            list.setAdapter(adapter);
            onlyTracked.setOnCheckedChangeListener((b, checked) -> applyFilter());

            pane = new PaneBuilder(paneRoot)
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.6d)
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.6d)
                    .build();
        }

        if (!ui.isPaneVisible(pane)) ui.showPane(pane, null);
        applyFilter();
    }

    private void applyFilter() {
        if (adapter == null || paneRoot == null) return;
        CheckBox onlyTracked = paneRoot.findViewById(R.id.cb_only_tracked);
        boolean filter = onlyTracked.isChecked();

        adapter.clear();
        if (!filter) {
            adapter.addAll(allRows);
        } else {
            for (Row r : allRows) if (r.tracked) adapter.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    private Row from(CotEvent ev, String xml) {
        try {
            String type = "";
            String callsign = "";
            long ms = System.currentTimeMillis();
            double lat = 0, lon = 0, alt = 0;
            boolean tracked = false;

            if (ev != null) {
                type = safe(ev.getType());
                if (ev.getTime() != null) {
                    try { ms = ev.getTime().getMilliseconds(); } catch (Throwable ignore) { }
                }
                CotPoint p = null;
                try { p = ev.getCotPoint(); } catch (Throwable ignore) { }
                if (p != null) {
                    lat = p.getLat();
                    lon = p.getLon();
                    alt = p.getHae();
                }
            }

            if (xml != null && !xml.isEmpty()) {
                // callsign
                Matcher m = CALLSIGN_RE.matcher(xml);
                if (m.find()) callsign = m.group(1);
                // basic track highlight heuristics
                tracked = TRACK_RE.matcher(xml).find() || tracked;
                // fallback for type
                if (type.isEmpty()) {
                    int i = xml.indexOf("type=\"");
                    if (i >= 0) {
                        int j = xml.indexOf('"', i + 6);
                        if (j > i) type = xml.substring(i + 6, j);
                    }
                }
                // fallback point parse if needed
                if (lat == 0 && lon == 0) {
                    try {
                        int i = xml.indexOf("<point");
                        if (i >= 0) {
                            String seg = xml.substring(i, Math.min(i + 200, xml.length()));
                            double la = attrD(seg, "lat");
                            double lo = attrD(seg, "lon");
                            double ha = attrD(seg, "hae");
                            if (!Double.isNaN(la)) lat = la;
                            if (!Double.isNaN(lo)) lon = lo;
                            if (!Double.isNaN(ha)) alt = ha;
                        }
                    } catch (Throwable ignore) { }
                }
            }

            String ts = fmt.format(new Date(ms));
            return new Row(type, callsign, ts, lat, lon, alt, tracked);
        } catch (Throwable t) {
            return null;
        }
    }

    private static double attrD(String s, String name) {
        int i = s.indexOf(name + "=\"");
        if (i < 0) return Double.NaN;
        int j = s.indexOf('"', i + name.length() + 2);
        if (j < 0) return Double.NaN;
        try { return Double.parseDouble(s.substring(i + name.length() + 2, j)); }
        catch (Exception e) { return Double.NaN; }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static class Row {
        final String type;
        final String callsign;
        final String ts;
        final double lat, lon, alt;
        final boolean tracked;

        Row(String type, String callsign, String ts, double lat, double lon, double alt, boolean tracked) {
            this.type = type;
            this.callsign = callsign;
            this.ts = ts;
            this.lat = lat;
            this.lon = lon;
            this.alt = alt;
            this.tracked = tracked;
        }

        String titleLine() {
            String cs = (callsign == null || callsign.isEmpty()) ? "(no callsign)" : callsign;
            return cs + "  •  " + (type.isEmpty() ? "(no type)" : type);
        }

        String detailLine() {
            return ts + "  •  " + String.format(Locale.US, "%.6f, %.6f, %.0f m", lat, lon, alt);
        }
    }
}