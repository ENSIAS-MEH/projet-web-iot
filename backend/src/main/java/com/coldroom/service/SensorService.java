package com.coldroom.service;

import com.coldroom.dto.SensorDTO;
import com.coldroom.entity.Sensor;
import com.coldroom.repository.SensorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for sensor management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {

    private final SensorRepository sensorRepository;

    // ── Read ────────────────────────────────────────────────

    /** Returns all sensors. */
    public List<SensorDTO> getAllSensors() {
        return sensorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns only active sensors. */
    public List<SensorDTO> getActiveSensors() {
        return sensorRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns a single sensor by ID, or throws 404. */
    public SensorDTO getSensorById(Integer id) {
        return toDTO(findOrThrow(id));
    }

    // ── Write ───────────────────────────────────────────────

    /** Creates a new sensor. */
    @Transactional
    public SensorDTO createSensor(SensorDTO dto) {
        validateThresholds(dto);
        Sensor sensor = toEntity(dto);
        if (sensor.getIsActive() == null) {
            sensor.setIsActive(true);
        }
        return toDTO(sensorRepository.save(sensor));
    }

    /** Fully updates an existing sensor. */
    @Transactional
    public SensorDTO updateSensor(Integer id, SensorDTO dto) {
        validateThresholds(dto);
        Sensor existing = findOrThrow(id);
        existing.setName(dto.getName());
        existing.setSensorType(dto.getSensorType());
        existing.setUnit(dto.getUnit());
        existing.setLocation(dto.getLocation());
        existing.setMinThreshold(dto.getMinThreshold());
        existing.setMaxThreshold(dto.getMaxThreshold());
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }
        return toDTO(sensorRepository.save(existing));
    }

    /** Deletes a sensor by ID. */
    @Transactional
    public void deleteSensor(Integer id) {
        if (!sensorRepository.existsById(id)) {
            throw new EntityNotFoundException("Sensor not found with id: " + id);
        }
        sensorRepository.deleteById(id);
    }

    // ── Helpers ─────────────────────────────────────────────

    public Sensor findOrThrow(Integer id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sensor not found with id: " + id));
    }

    private void validateThresholds(SensorDTO dto) {
        if (dto.getMinThreshold() != null && dto.getMaxThreshold() != null
                && dto.getMinThreshold().compareTo(dto.getMaxThreshold()) >= 0) {
            throw new IllegalArgumentException("minThreshold must be less than maxThreshold");
        }
    }

    // ── Mapping ─────────────────────────────────────────────

    public SensorDTO toDTO(Sensor sensor) {
        return SensorDTO.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .sensorType(sensor.getSensorType())
                .unit(sensor.getUnit())
                .location(sensor.getLocation())
                .minThreshold(sensor.getMinThreshold())
                .maxThreshold(sensor.getMaxThreshold())
                .isActive(sensor.getIsActive())
                .createdAt(sensor.getCreatedAt())
                .updatedAt(sensor.getUpdatedAt())
                .build();
    }

    private Sensor toEntity(SensorDTO dto) {
        return Sensor.builder()
                .name(dto.getName())
                .sensorType(dto.getSensorType())
                .unit(dto.getUnit())
                .location(dto.getLocation())
                .minThreshold(dto.getMinThreshold())
                .maxThreshold(dto.getMaxThreshold())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
