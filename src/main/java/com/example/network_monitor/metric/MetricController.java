package com.example.network_monitor.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {
    private final MetricService metricService;

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<DeviceMetric>> findByDevice(@PathVariable("deviceId") Long deviceId) {
        return ResponseEntity.ok(metricService.getMetricsByDevice(deviceId));
    }

    @GetMapping("/device/{deviceId}/latest")
    public ResponseEntity<DeviceMetric> findLatestByDevice(@PathVariable("deviceId") Long deviceId) {
        return ResponseEntity.ok(metricService.getLatestMetricByDevice(deviceId));
    }
}
