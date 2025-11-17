package com.atakmap.android.networklistener.plugin;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.networklistener.plugin.R;
import com.atakmap.coremap.maps.coords.GeoPoint;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

/**
 * Network Data Listener Plugin
 * Receives telemetry data via TCP/UDP and visualizes on ATAK map
 */
public class NetworkListenerPlugin implements IPlugin {

    private static final String TAG = "NetworkListenerPlugin";
    private static final int TCP_PORT = 8888;
    private static final int UDP_PORT = 9999;

    private final IServiceController sc;
    private final Context pluginCtx;
    private final IHostUIService ui;

    private Pane pane;
    private View paneRoot;
    private ToolbarItem button;

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> telemetryLog = new ArrayList<>();
    
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;
    
    private Marker telemetryMarker;
    private TextView statusText;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public NetworkListenerPlugin(IServiceController sc) {
        this.sc = sc;
        PluginContextProvider p = sc.getService(PluginContextProvider.class);
        this.pluginCtx = p.getPluginContext();
        this.pluginCtx.setTheme(R.style.ATAKPluginTheme);
        this.ui = sc.getService(IHostUIService.class);

        this.button = new ToolbarItem.Builder(
                "Network Listener",
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
        Log.d(TAG, "NetworkListenerPlugin started");
    }

    @Override
    public void onStop() {
        stopServers();
        if (ui != null) ui.removeToolbarItem(button);
        if (telemetryMarker != null) {
            telemetryMarker.removeFromGroup();
        }
        Log.d(TAG, "NetworkListenerPlugin stopped");
    }

    private void showPane() {
        if (pane == null) {
            paneRoot = PluginLayoutInflater.inflate(pluginCtx, R.layout.network_listener, null);

            ListView list = paneRoot.findViewById(R.id.telemetry_list);
            statusText = paneRoot.findViewById(R.id.status_text);
            Button startTcpBtn = paneRoot.findViewById(R.id.btn_start_tcp);
            Button startUdpBtn = paneRoot.findViewById(R.id.btn_start_udp);
            Button stopBtn = paneRoot.findViewById(R.id.btn_stop);
            Button testBtn = paneRoot.findViewById(R.id.btn_test);

            adapter = new ArrayAdapter<>(pluginCtx, android.R.layout.simple_list_item_1, telemetryLog);
            list.setAdapter(adapter);

            startTcpBtn.setOnClickListener(v -> startTcpServer());
            startUdpBtn.setOnClickListener(v -> startUdpServer());
            stopBtn.setOnClickListener(v -> stopServers());
            testBtn.setOnClickListener(v -> sendTestData());

            updateStatus("Ready");

            pane = new PaneBuilder(paneRoot)
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.7d)
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.7d)
                    .build();
        }

        if (ui != null) {
            ui.showPane(pane, null);
        }
    }

    private void startTcpServer() {
        if (isRunning) {
            addLog("Server already running");
            return;
        }

        isRunning = true;
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.submit(() -> {
            try {
                tcpServerSocket = new ServerSocket(TCP_PORT);
                updateStatus("TCP Server listening on port " + TCP_PORT);
                addLog("TCP Server started on port " + TCP_PORT);

                while (isRunning && !tcpServerSocket.isClosed()) {
                    Socket clientSocket = tcpServerSocket.accept();
                    addLog("TCP Client connected: " + clientSocket.getInetAddress());
                    
                    handleTcpClient(clientSocket);
                }
            } catch (Exception e) {
                if (!isRunning) {
                    // Likely closed due to stopServers(); ignore
                    return;
                }
                Log.e(TAG, "TCP Server error", e);
                addLog("TCP Error: " + e.getMessage());
                updateStatus("TCP Error");
            } finally {
                if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                    try {
                        tcpServerSocket.close();
                    } catch (Exception ignore) { }
                }
                tcpServerSocket = null;
                isRunning = false;
            }
        });
    }

    private void handleTcpClient(Socket socket) {
        executorService.submit(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                
                while (isRunning && (line = reader.readLine()) != null) {
                    processData(line, "TCP");
                }
                
                socket.close();
                addLog("TCP Client disconnected");
            } catch (Exception e) {
                Log.e(TAG, "TCP Client error", e);
            }
        });
    }

    private void startUdpServer() {
        if (isRunning) {
            addLog("Server already running");
            return;
        }

        isRunning = true;
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.submit(() -> {
            try {
                udpSocket = new DatagramSocket(UDP_PORT);
                updateStatus("UDP Server listening on port " + UDP_PORT);
                addLog("UDP Server started on port " + UDP_PORT);

                byte[] buffer = new byte[1024];
                
                while (isRunning && !udpSocket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    
                    String data = new String(packet.getData(), 0, packet.getLength());
                    processData(data, "UDP");
                }
            } catch (Exception e) {
                if (!isRunning) {
                    // Likely closed due to stopServers(); ignore
                    return;
                }
                Log.e(TAG, "UDP Server error", e);
                addLog("UDP Error: " + e.getMessage());
                updateStatus("UDP Error");
            } finally {
                if (udpSocket != null && !udpSocket.isClosed()) {
                    try {
                        udpSocket.close();
                    } catch (Exception ignore) { }
                }
                udpSocket = null;
                isRunning = false;
            }
        });
    }

    private void processData(String data, String protocol) {
        try {
            JSONObject json = new JSONObject(data);
            
            String id = json.optString("id", "unknown");
            double lat = json.optDouble("lat", 0);
            double lon = json.optDouble("lon", 0);
            double speed = json.optDouble("speed", 0);
            double temp = json.optDouble("temperature", 0);
            
            String logEntry = String.format("[%s] %s - ID:%s Pos:(%.6f,%.6f) Speed:%.1f Temp:%.1f°C",
                    fmt.format(new Date()), protocol, id, lat, lon, speed, temp);
            
            addLog(logEntry);
            updateMarker(id, lat, lon, speed, temp);
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + data, e);
            addLog("Parse error: " + e.getMessage());
        }
    }

    private void updateMarker(String id, double lat, double lon, double speed, double temp) {
        mainHandler.post(() -> {
            try {
                MapView mapView = MapView.getMapView();
                if (mapView == null) return;

                if (telemetryMarker == null) {
                    telemetryMarker = new Marker(new GeoPoint(lat, lon), id);
                    telemetryMarker.setType("a-f-G-E-S");
                    telemetryMarker.setMetaString("callsign", id);
                    telemetryMarker.setColor(Color.BLUE);
                    mapView.getRootGroup().addItem(telemetryMarker);
                } else {
                    telemetryMarker.setPoint(new GeoPoint(lat, lon));
                }
                
                telemetryMarker.setMetaDouble("speed", speed);
                telemetryMarker.setMetaDouble("temperature", temp);
                telemetryMarker.setTitle(String.format("%s\nSpeed: %.1f\nTemp: %.1f°C", id, speed, temp));
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating marker", e);
            }
        });
    }

    private void stopServers() {
        isRunning = false;
        
        if (tcpServerSocket != null) {
            try {
                tcpServerSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing TCP server socket", e);
            } finally {
                tcpServerSocket = null;
            }
        }

        if (udpSocket != null) {
            try {
                udpSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing UDP socket", e);
            } finally {
                udpSocket = null;
            }
        }

        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        updateStatus("Stopped");
        addLog("Server stopped");
    }

    private void sendTestData() {
        // Simulate test telemetry data
        MapView mapView = MapView.getMapView();
        if (mapView == null) return;
        
        GeoPoint center = mapView.getCenterPoint().get();
        double lat = center.getLatitude() + (Math.random() - 0.5) * 0.001;
        double lon = center.getLongitude() + (Math.random() - 0.5) * 0.001;
        
        String testData = String.format(
                "{\"id\":\"TestRobot\",\"lat\":%.6f,\"lon\":%.6f,\"speed\":%.1f,\"temperature\":%.1f}",
                lat, lon, Math.random() * 50, 20 + Math.random() * 10);
        
        processData(testData, "TEST");
    }

    private void addLog(String message) {
        mainHandler.post(() -> {
            telemetryLog.add(0, message);
            if (telemetryLog.size() > 100) {
                telemetryLog.remove(telemetryLog.size() - 1);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateStatus(String status) {
        mainHandler.post(() -> {
            if (statusText != null) {
                statusText.setText("Status: " + status);
            }
        });
    }
}
