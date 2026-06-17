package com.example.network_monitor.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<Alert>> getAlertsByDevice(@PathVariable Long deviceId) {
        return ResponseEntity.ok(alertService.getAlertsByDevice(deviceId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Alert>> getAlertsByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }

    @PutMapping("/{alertsId}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long alertsId) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertsId));
    }

    @PutMapping("/{alertsId}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long alertsId) {
        return ResponseEntity.ok(alertService.resolveAlert(alertsId));
    }

}
