package com.example.network_monitor.alert;

import com.example.network_monitor.device.Device;
import com.example.network_monitor.device.DeviceRepository;
import com.example.network_monitor.metric.DeviceMetric;
import com.example.network_monitor.metric.MetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;

    public void checkAndGenerateAlerts(Device device, DeviceMetric metric) {
        if (metric.getCpuUsage() > 80) {
            Alert alert = new Alert();
            alert.setDevice(device);
            alert.setAlertType(AlertType.CPU_HIGH);
            alert.setSeverity(AlertSeverity.CRITICAL);
            alert.setStatus(AlertStatus.OPEN);
            alert.setMessage("CPU usage is " + metric.getCpuUsage() + "% which exceeds 80%");
            alertRepository.save(alert);
        }
        if (metric.getMemoryUsage() > 75) {
            Alert alert = new Alert();
            alert.setDevice(device);
            alert.setAlertType(AlertType.MEMORY_HIGH);
            alert.setSeverity(AlertSeverity.MEDIUM);
            alert.setStatus(AlertStatus.OPEN);
            alert.setMessage("Memory usage is " + metric.getMemoryUsage() + "% which exceeds 75%");
            alertRepository.save(alert);
        }
        if (metric.getLatency() > 150) {
            Alert alert = new Alert();
            alert.setDevice(device);
            alert.setAlertType(AlertType.LATENCY_HIGH);
            alert.setSeverity(AlertSeverity.CRITICAL);
            alert.setStatus(AlertStatus.OPEN);
            alert.setMessage("Latency is " + metric.getLatency() + "ms which exceeds 150ms");
            alertRepository.save(alert);
        }
    }

    public List<Alert> getAlertsByDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).
                orElseThrow(() ->new RuntimeException("Device not found"));
        return alertRepository.findByDevice(device);
    }

    public List<Alert> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status);
    }

    public Alert acknowledgeAlert(Long alertId) {
        Alert al = alertRepository.findById(alertId)
                .orElseThrow(() ->new RuntimeException("Alert not found"));
        al.setStatus(AlertStatus.ACKNOWLEDGED);
        return alertRepository.save(al);
    }

    public Alert resolveAlert(Long alertId) {
        Alert al = alertRepository.findById(alertId)
                .orElseThrow(() ->new RuntimeException("Alert not found"));
        al.setStatus(AlertStatus.RESOLVED);
        return alertRepository.save(al);
    }
}
