package com.example.network_monitor.device;

import com.example.network_monitor.alert.AlertRepository;
import com.example.network_monitor.metric.MetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final MetricRepository metricRepository;
    private final AlertRepository alertRepository;

    public List<Device> getAllDevices(){
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id){
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id : " + id));
    }

    public Device createDevice(Device device){
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device device){
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id : " + id));
        existing.setName(device.getName());
        existing.setIpAddress(device.getIpAddress());
        existing.setMacAddress(device.getMacAddress());
        existing.setDeviceType(device.getDeviceType());
        existing.setStatus(device.getStatus());
        return deviceRepository.save(existing);
    }

    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        // Delete alerts FIRST
        alertRepository.deleteAll(alertRepository.findByDevice(device));
        // Then delete metrics
        metricRepository.deleteAll(metricRepository.findByDevice(device));
        // Finally delete device
        deviceRepository.deleteById(id);
    }
}