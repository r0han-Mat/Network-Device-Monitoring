package com.example.network_monitor.metric;

import com.example.network_monitor.device.Device;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class DeviceMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "device_id")
    private Device device;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double latency;
    @CreationTimestamp
    private LocalDateTime recordedAt;


}
