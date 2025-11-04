#!/usr/bin/env python3
"""
Test Telemetry Sender for ATAK Network Listener Plugin
Sends mock robot telemetry data via TCP or UDP
"""

import socket
import json
import time
import random
import argparse
import math

def send_tcp(host, port, data):
    """Send data via TCP"""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((host, port))
        sock.sendall((json.dumps(data) + '\n').encode('utf-8'))
        sock.close()
        return True
    except Exception as e:
        print(f"TCP Error: {e}")
        return False

def send_udp(host, port, data):
    """Send data via UDP"""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.sendto(json.dumps(data).encode('utf-8'), (host, port))
        sock.close()
        return True
    except Exception as e:
        print(f"UDP Error: {e}")
        return False

def generate_telemetry(robot_id, center_lat, center_lon, angle):
    """Generate mock telemetry data"""
    # Simulate circular movement
    radius = 0.001  # ~111 meters
    lat = center_lat + radius * math.cos(angle)
    lon = center_lon + radius * math.sin(angle)
    
    return {
        "id": robot_id,
        "lat": round(lat, 6),
        "lon": round(lon, 6),
        "speed": round(random.uniform(10, 50), 1),
        "temperature": round(random.uniform(18, 28), 1)
    }

def main():
    parser = argparse.ArgumentParser(description='Send telemetry data to ATAK Network Listener')
    parser.add_argument('--host', default='localhost', help='Target host (default: localhost)')
    parser.add_argument('--tcp-port', type=int, default=8888, help='TCP port (default: 8888)')
    parser.add_argument('--udp-port', type=int, default=9999, help='UDP port (default: 9999)')
    parser.add_argument('--protocol', choices=['tcp', 'udp'], default='tcp', help='Protocol to use')
    parser.add_argument('--robot-id', default='Robot-Alpha', help='Robot identifier')
    parser.add_argument('--lat', type=float, default=34.0522, help='Center latitude (default: LA)')
    parser.add_argument('--lon', type=float, default=-118.2437, help='Center longitude (default: LA)')
    parser.add_argument('--interval', type=float, default=1.0, help='Send interval in seconds')
    parser.add_argument('--count', type=int, default=0, help='Number of messages (0 = infinite)')
    
    args = parser.parse_args()
    
    print(f"Sending telemetry to {args.host}")
    print(f"Protocol: {args.protocol.upper()}")
    print(f"Port: {args.tcp_port if args.protocol == 'tcp' else args.udp_port}")
    print(f"Robot ID: {args.robot_id}")
    print(f"Center: ({args.lat}, {args.lon})")
    print(f"Interval: {args.interval}s")
    print("Press Ctrl+C to stop\n")
    
    angle = 0
    count = 0
    
    try:
        while args.count == 0 or count < args.count:
            telemetry = generate_telemetry(args.robot_id, args.lat, args.lon, angle)
            
            if args.protocol == 'tcp':
                success = send_tcp(args.host, args.tcp_port, telemetry)
            else:
                success = send_udp(args.host, args.udp_port, telemetry)
            
            if success:
                print(f"[{count+1}] Sent: {json.dumps(telemetry)}")
            else:
                print(f"[{count+1}] Failed to send")
            
            angle += math.radians(10)  # Move 10 degrees each time
            count += 1
            time.sleep(args.interval)
            
    except KeyboardInterrupt:
        print("\n\nStopped by user")
    
    print(f"\nTotal messages sent: {count}")

if __name__ == '__main__':
    main()
