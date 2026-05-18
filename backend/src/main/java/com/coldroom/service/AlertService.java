package com.coldroom.service;

import com.coldroom.dto.AlertDTO;
import com.coldroom.entity.Alert;
import com.coldroom.repository.AlertRepository;
import com.coldroom.repository.SensorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for alert management.
 *
 * Alerts are created automatically by {@link SensorReadingService} during
 * anomaly detection. This service handles reading and resolving them.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final SensorRepository sensorRepository;

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    /** Returns all alerts, newest first. */
    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns only unresolved (active) alerts, newest first. */
    public List<AlertDTO> getActiveAlerts() {
        return alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Returns a single alert by ID, or throws 404. */
    public AlertDTO getAlertById(Long id) {
        return toDTO(findOrThrow(id));
    }

    /** Returns all alerts for a specific sensor, newest first. */
    public List<AlertDTO> getAlertsBySensor(Integer sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new EntityNotFoundException("Sensor not found with id: " + sensorId);
        }
        return alertRepository.findBySensorIdOrderByCreatedAtDesc(sensorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // WRITE
    // ----------------------------------------------------------------

    /**
     * Marks an alert as resolved and records the resolution timestamp.
     *
     * @param id alert identifier
     * @return the updated alert DTO
     * @throws EntityNotFoundException if no alert exists with the given id
     * @throws IllegalStateException   if the alert is already resolved
     */
    @Transactional
    public AlertDTO resolveAlert(Long id) {
        Alert alert = findOrThrow(id);

        if (Boolean.TRUE.equals(alert.getIsResolved())) {
            throw new IllegalStateException("Alert with id " + id + " is already resolved");
        }

        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        log.info("Alert id={} resolved at {}", id, saved.getResolvedAt());
        return toDTO(saved);
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    private Alert findOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found with id: " + id));
    }

    // ----------------------------------------------------------------
    // MAPPING
    // ----------------------------------------------------------------

    public AlertDTO toDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .sensorId(alert.getSensor().getId())
                .sensorName(alert.getSensor().getName())
                .readingId(alert.getReading().getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .isResolved(alert.getIsResolved())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}
