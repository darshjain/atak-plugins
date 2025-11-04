package com.atakmap.android.gpstracker.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;

import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;

public class GpsSimulatorService {

    private static final String UID = "SIMBOT-001";
    private static final String TYPE = "a-f-A-M-H";
    private static final long STALE_MS = 60_000;

    private final MapView mapView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    private double angleRad = 0.0;
    private final double radiusDeg = 0.0005;
    private final double velocityMs = 5.0;

    public GpsSimulatorService(MapView mv) {
        this.mapView = mv;
    }

    public void startSimulation() {
        if (running) return;
        running = true;
        handler.post(tick);
    }

    public void stopSimulation() {
        running = false;
        handler.removeCallbacksAndMessages(null);

        long now = System.currentTimeMillis();
        CotEvent clear = buildEvent(0, 0, 0, new CoordinatedTime(now - 1000));
        CotMapComponent.getInstance().getInternalDispatcher().dispatch(clear);
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            GeoPoint c = mapView.getCenterPoint().get();
            double lat = c.getLatitude()  + radiusDeg * Math.cos(angleRad);
            double lon = c.getLongitude() + radiusDeg * Math.sin(angleRad);
            angleRad += Math.toRadians(10);

            CotEvent ev = buildEvent(lat, lon, 0, null);

            CotDetail root = ev.getDetail();
            CotDetail contact = new CotDetail("contact");
            contact.setAttribute("callsign", "SimBot");
            root.addChild(contact);

            CotDetail status = new CotDetail("status");
            status.setAttribute("speed", String.format("%.1f", velocityMs));
            root.addChild(status);

            CotMapComponent.getInstance().getInternalDispatcher().dispatch(ev);
            
            // Also broadcast for plugins to receive
            Intent cotIntent = new Intent("com.atakmap.android.cotviewer.COT_EVENT");
            Bundle bundle = new Bundle();
            bundle.putParcelable("cotevent", ev);
            cotIntent.putExtras(bundle);
            AtakBroadcast.getInstance().sendBroadcast(cotIntent);

            if (running) handler.postDelayed(this, 1000);
        }
    };

    private CotEvent buildEvent(double lat, double lon, double hae, CoordinatedTime forcedStale) {
        long nowMs = System.currentTimeMillis();
        CoordinatedTime now = new CoordinatedTime(nowMs);
        CoordinatedTime stale = forcedStale != null ? forcedStale : new CoordinatedTime(nowMs + STALE_MS);

        CotEvent e = new CotEvent();
        e.setType(TYPE);
        e.setUID(UID);
        e.setHow("m-g");
        e.setTime(now);
        e.setStart(now);
        e.setStale(stale);

        CotPoint p = new CotPoint(lat, lon, hae, 10.0, 10.0);
        e.setPoint(p);

        e.setDetail(new CotDetail("detail"));
        return e;
    }
}