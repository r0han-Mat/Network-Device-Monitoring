package com.example.network_monitor.metric;

import com.example.network_monitor.alert.AlertService;
import com.example.network_monitor.device.DeviceRepository;
import com.example.network_monitor.snmp.SnmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricScheduler {

    private final MetricRepository metricRepository;
    private final DeviceRepository deviceRepository;
    private final AlertService alertService;
    private final SnmpService snmpService;

    @Scheduled(fixedRate = 30000)
    public void collectMetrics() {
        deviceRepository.findAll().forEach(device -> {
            DeviceMetric dm = new DeviceMetric();
            dm.setDevice(device);
            dm.setCpuUsage(
                    snmpService.getCpuUsage(device.getIpAddress()));
            dm.setMemoryUsage(
                    snmpService.getMemoryUsage(device.getIpAddress()));
            dm.setLatency(
                    snmpService.getUptime(device.getIpAddress()));
            metricRepository.save(dm);
            alertService.checkAndGenerateAlerts(device, dm);
        });
    }
}