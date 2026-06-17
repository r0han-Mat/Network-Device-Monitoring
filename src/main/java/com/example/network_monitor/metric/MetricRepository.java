package com.example.network_monitor.metric;

import com.example.network_monitor.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricRepository extends JpaRepository<DeviceMetric, Long> {
    List<DeviceMetric> findByDevice(Device device);
}