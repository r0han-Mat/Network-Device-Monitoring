package com.example.network_monitor.alert;

import com.example.network_monitor.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDevice(Device device);
    List<Alert> findByStatus(AlertStatus status);
}
