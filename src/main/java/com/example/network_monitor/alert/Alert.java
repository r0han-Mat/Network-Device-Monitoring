package com.example.network_monitor.alert;

import com.example.network_monitor.device.Device;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "device_id")
    private Device device;
    private AlertSeverity severity;
    private AlertStatus status;
    private AlertType alertType;
    private String message;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
