package com.example.network_monitor.metric;

import com.example.network_monitor.device.Device;
import com.example.network_monitor.device.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final DeviceRepository deviceRepository;

    public List<DeviceMetric> getMetricsByDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return metricRepository.findByDevice(device);
    }

    public DeviceMetric getLatestMetricByDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        List<DeviceMetric> metrics = metricRepository.findByDevice(device);
        return metrics.get(metrics.size() - 1); // ← last element
    }

}
