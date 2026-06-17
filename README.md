# Network Device Monitoring Platform

A backend platform that collects and analyzes real network metrics from a virtual infrastructure using SNMP — the same protocol used by Cisco's production monitoring tools like DNA Center and Prime Infrastructure.

---

## Overview

This project monitors a 3-node virtual network provisioned with Vagrant, polling real system metrics (CPU, memory, uptime) every 30 seconds via SNMP4J. It exposes REST APIs for device management, stores time-series metrics in PostgreSQL, and automatically generates alerts when thresholds are exceeded.

```
Spring Boot App
      │
      ├─── SNMP4J (UDP port 161) ──► Vagrant-Gateway    192.168.56.10
      │                          ──► Vagrant-WebServer  192.168.56.11
      │                          ──► Vagrant-DBServer   192.168.56.12
      │
      ├─── PostgreSQL (Docker)
      │         └── devices, device_metrics, alerts
      │
      └─── Dashboard (localhost:8080)
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.1, Java 21 |
| Database | PostgreSQL 15 (Docker) |
| ORM | Spring Data JPA, Hibernate |
| SNMP | SNMP4J 2.8.18 |
| Scheduler | Spring `@Scheduled` |
| API Docs | Springdoc OpenAPI (Swagger) |
| Frontend | Vanilla HTML/JS, Chart.js |
| Virtual Network | Vagrant + VirtualBox |
| Containerization | Docker, Docker Compose |

---

## Architecture

### Modules

```
src/main/java/com/example/network_monitor/
├── device/
│   ├── Device.java              Entity — stores device info
│   ├── DeviceRepository.java    JPA repository
│   ├── DeviceService.java       Business logic
│   └── DeviceController.java    REST endpoints
│
├── metric/
│   ├── DeviceMetric.java        Entity — time-series metrics
│   ├── MetricRepository.java    JPA repository
│   ├── MetricService.java       Query logic
│   ├── MetricController.java    REST endpoints
│   └── MetricScheduler.java     Polls SNMP every 30s
│
├── alert/
│   ├── Alert.java               Entity — alert records
│   ├── AlertRepository.java     JPA repository
│   ├── AlertService.java        Detection + lifecycle
│   └── AlertController.java     REST endpoints
│
└── snmp/
    └── SnmpService.java         SNMP4J query engine
```

### How SNMP Polling Works

Every 30 seconds, `MetricScheduler` iterates over all registered devices and calls `SnmpService` for each one:

```
MetricScheduler (@Scheduled fixedRate=30000)
    └── for each Device in DB:
            SnmpService.getCpuUsage(device.ipAddress)
                └── SNMP GET → OID 1.3.6.1.4.1.2021.11.11.0
                └── returns CPU idle % → usage = 100 - idle
            SnmpService.getMemoryUsage(device.ipAddress)
                └── SNMP GET → OID 1.3.6.1.4.1.2021.4.5.0 (total RAM)
                └── SNMP GET → OID 1.3.6.1.4.1.2021.4.11.0 (available RAM)
                └── returns (total - avail) / total * 100
            SnmpService.getUptime(device.ipAddress)
                └── SNMP GET → OID 1.3.6.1.2.1.1.3.0
                └── returns system uptime in centiseconds
            → saves DeviceMetric to PostgreSQL
            → AlertService.checkAndGenerateAlerts()
```

### Alert Engine

Alerts are generated automatically when thresholds are exceeded:

| Condition | Alert Type | Severity |
|---|---|---|
| CPU > 80% | CPU_HIGH | CRITICAL |
| Memory > 75% | MEMORY_HIGH | MEDIUM |
| Latency > 150ms | LATENCY_HIGH | CRITICAL |

Alert lifecycle: `OPEN` → `ACKNOWLEDGED` → `RESOLVED`

---

## Prerequisites

- Java 21
- Docker Desktop
- VirtualBox
- Vagrant 2.x

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/network-monitor.git
cd network-monitor
```

### 2. Provision the virtual network

```bash
cd network-lab
vagrant up
```

This automatically creates 3 Ubuntu VMs with SNMP configured:

| VM | IP | Role |
|---|---|---|
| gateway | 192.168.56.10 | Network gateway |
| webserver | 192.168.56.11 | Web server |
| dbserver | 192.168.56.12 | Database server |

Verify they are running:

```bash
vagrant status
ping 192.168.56.10
ping 192.168.56.11
ping 192.168.56.12
```

### 3. Start the application

```bash
cd network-monitor
docker-compose up --build
```

This starts:
- PostgreSQL on port 5432
- Spring Boot app on port 8080

Wait for:
```
Tomcat started on port 8080
Started NetworkMonitorApplication
```

### 4. Register devices

Open Swagger at `http://localhost:8080/swagger-ui.html` and POST to `/api/devices`:

```json
{ "name": "Vagrant-Gateway", "ipAddress": "192.168.56.10", "macAddress": "08:00:27:00:00:01", "deviceType": "ROUTER", "status": "ONLINE" }
```

```json
{ "name": "Vagrant-WebServer", "ipAddress": "192.168.56.11", "macAddress": "08:00:27:00:00:02", "deviceType": "SERVER", "status": "ONLINE" }
```

```json
{ "name": "Vagrant-DBServer", "ipAddress": "192.168.56.12", "macAddress": "08:00:27:00:00:03", "deviceType": "SERVER", "status": "ONLINE" }
```

### 5. Open the dashboard

```
http://localhost:8080
```

Metrics will start appearing within 30 seconds.

---

## API Reference

### Devices

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/devices` | List all devices |
| GET | `/api/devices/{id}` | Get device by ID |
| POST | `/api/devices` | Register a new device |
| PUT | `/api/devices/{id}` | Update device |
| DELETE | `/api/devices/{id}` | Remove device and all its data |

### Metrics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/metrics/device/{id}` | All metrics for a device |
| GET | `/api/metrics/device/{id}/latest` | Latest metric snapshot |

### Alerts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/alerts/device/{id}` | Alerts for a specific device |
| GET | `/api/alerts/status/{status}` | Filter by OPEN / ACKNOWLEDGED / RESOLVED |
| PUT | `/api/alerts/{id}/acknowledge` | Acknowledge an alert |
| PUT | `/api/alerts/{id}/resolve` | Resolve an alert |

Full interactive documentation: `http://localhost:8080/swagger-ui.html`

---

## Dashboard

The dashboard is served at `http://localhost:8080` and auto-refreshes every 30 seconds.

**Pages:**
- **Dashboard** — stat cards, device status, live metric bars, CPU/Memory charts, recent alerts
- **Devices** — full device table, add device form, delete device
- **Metrics** — per-device CPU and memory charts, poll history table
- **Alerts** — filter by status, acknowledge and resolve alerts inline

---

## Project Structure

```
network-monitor/
├── src/
│   └── main/
│       ├── java/com/example/network_monitor/
│       │   ├── device/
│       │   ├── metric/
│       │   ├── alert/
│       │   └── snmp/
│       └── resources/
│           ├── static/
│           │   └── index.html        Dashboard
│           └── application.properties
├── network-lab/
│   └── Vagrantfile                   3-VM virtual network
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## Configuration

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/networkdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## Vagrantfile

The `network-lab/Vagrantfile` provisions all 3 VMs automatically with:
- Ubuntu 22.04 (jammy64)
- `snmpd` installed and configured
- SNMP community string `public` with full MIB access
- Host-only networking on `192.168.56.0/24`

To recreate the network from scratch:

```bash
vagrant destroy -f
vagrant up
```

---

## How It Relates to Real Network Monitoring

| This Project | Production Tools |
|---|---|
| SNMP4J polling | Cisco DNA Center, SolarWinds |
| OID-based metric queries | Same OIDs used in enterprise monitoring |
| Threshold alerting | Same concept as Nagios, Grafana alerts |
| Alert lifecycle (OPEN → RESOLVED) | Standard NOC workflow |
| Vagrant virtual network | Represents real router/switch/server topology |

The key difference is scale — production tools monitor thousands of real devices; this project monitors 3 virtual ones using the same underlying protocol.

---

## Known Limitations

- SNMP community string is `public` (no authentication) — production deployments use SNMP v3 with authentication and encryption
- CPU OID availability depends on the SNMP MIB configuration of the target device
- Vagrant VMs must be running before the Spring Boot app starts polling

---

## License

MIT
#   N e t w o r k - D e v i c e - M o n i t o r i n g  
 