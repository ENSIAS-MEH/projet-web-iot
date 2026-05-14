package com.coldroom.service;

import com.coldroom.dto.SensorReadingDTO;
import com.coldroom.entity.Alert;
import com.coldroom.entity.Sensor;
import com.coldroom.entity.SensorReading;
import com.coldroom.repository.AlertRepository;
import com.coldroom.repository.SensorReadingRepository;
import com.coldroom.repository.SensorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for sensor readings, including anomaly detection.
 *
 * <p>When a new reading is saved, the service automatically checks whether
 * the value falls outside the sensor's configured thresholds.  If it does,
 * an {@link Alert} is created and persisted in the same transaction.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorReadingService {

    private final SensorReadingRepository readingRepository;
    private final SensorRepository        sensorRepository;
    private final AlertRepository         alertRepository;

    // ----------------------------------------------------------------
    // WRITE
    // ----------------------------------------------------------------

    /**
     * Persists a new sensor reading and triggers anomaly detection.
     *
     * @param dto incoming reading payload
     * @return the saved reading as a DTO
     */
    @Transactional
    public SensorReadingDTO addReading(SensorReadingDTO dto) {
        Sensor sensor = findSensorOrThrow(dto.getSensorId());

        SensorReading reading = SensorReading.builder()
                .sensor(sensor)
                .value(dto.getValue())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .build();

        SensorReading saved = readingRepository.save(reading);
        log.debug("Saved reading id={} for sensor id={} value={}", saved.getId(), sensor.getId(), saved.getValue());

        // Anomaly detection – runs in the same transaction
        checkThresholdsAndAlert(sensor, saved);

        return toDTO(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    /**
     * Returns all readings with pagination (newest first).
     *
     * @param pageable pagination parameters
     * @return page of reading DTOs
     */
    public Page<SensorReadingDTO> getAllReadings(Pageable pageable) {
        return readingRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Returns all readings for a specific sensor (newest first).
     *
     * @param sensorId sensor identifier
     * @return list of reading DTOs
     */
    public List<SensorReadingDTO> getReadingsBySensor(Integer sensorId) {
        findSensorOrThrow(sensorId); // validate sensor exists
        return readingRepository.findBySensorIdOrderByTimestampDesc(sensorId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recent reading for every sensor.
     *
     * @return list of reading DTOs (one per sensor that has data)
     */
    public List<SensorReadingDTO> getLatestReadings() {
        return readingRepository.findLatestPerSensor()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns readings for a sensor within a date/time range (newest first).
     *
     * @param sensorId  sensor identifier
     * @param startDate range start (inclusive)
     * @param endDate   range end (inclusive)
     * @return list of reading DTOs
     */
    public List<SensorReadingDTO> getReadingHistory(Integer sensorId,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        findSensorOrThrow(sensorId);
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        LocalDateTime from = startDate != null ? startDate : LocalDateTime.MIN;
        LocalDateTime to   = endDate   != null ? endDate   : LocalDateTime.now();

        return readingRepository
                .findBySensorIdAndTimestampBetweenOrderByTimestampDesc(sensorId, from, to)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // ANOMALY DETECTION
    // ----------------------------------------------------------------

    /**
     * Checks whether {@code reading.value} is outside the sensor's thresholds.
     * If so, creates and persists an {@link Alert} with an appropriate severity.
     *
     * <p>Severity rules:</p>
     * <ul>
     *   <li><b>critical</b> – value deviates by more than 20 % of the threshold range</li>
     *   <li><b>warning</b>  – value deviates by 10–20 % of the threshold range</li>
     *   <li><b>info</b>     – value is just outside the threshold (≤ 10 % deviation)</li>
     * </ul>
     */
    private void checkThresholdsAndAlert(Sensor sensor, SensorReading reading) {
        BigDecimal value = reading.getValue();
        BigDecimal min   = sensor.getMinThreshold();
        BigDecimal max   = sensor.getMaxThreshold();

        boolean belowMin = value.compareTo(min) < 0;
        boolean aboveMax = value.compareTo(max) > 0;

        if (!belowMin && !aboveMax) {
            return; // value is within safe range – no alert needed
        }

        // Calculate how far outside the threshold the value is
        BigDecimal range     = max.subtract(min);
        BigDecimal deviation = belowMin
                ? min.subtract(value)
                : value.subtract(max);

        String severity = computeSeverity(deviation, range);

        String direction = belowMin ? "below minimum" : "above maximum";
        BigDecimal threshold = belowMin ? min : max;
        String message = String.format(
                "%s reading is %s threshold: %.4f%s (threshold: %.2f%s)",
                sensor.getName(), direction,
                value, sensor.getUnit(),
                threshold, sensor.getUnit());

        Alert alert = Alert.builder()
                .sensor(sensor)
                .reading(reading)
                .alertType("threshold_exceeded")
                .severity(severity)
                .message(message)
                .isResolved(false)
                .build();

        alertRepository.save(alert);
        log.warn("Alert created [{}] for sensor '{}': {}", severity, sensor.getName(), message);
    }

    /**
     * Determines severity based on how far the deviation is relative to the
     * sensor's threshold range.
     *
     * @param deviation absolute distance outside the threshold
     * @param range     total threshold range (max - min)
     * @return "critical", "warning", or "info"
     */
    private String computeSeverity(BigDecimal deviation, BigDecimal range) {
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return "critical"; // degenerate range – always critical
        }
        // deviation as a percentage of the range
        double pct = deviation.doubleValue() / range.doubleValue();
        if (pct > 0.20) return "critical";
        if (pct > 0.10) return "warning";
        return "info";
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    private Sensor findSensorOrThrow(Integer sensorId) {
        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sensor not found with id: " + sensorId));
    }

    // ----------------------------------------------------------------
    // MAPPING
    // ----------------------------------------------------------------

    public SensorReadingDTO toDTO(SensorReading reading) {
        return SensorReadingDTO.builder()
                .id(reading.getId())
                .sensorId(reading.getSensor().getId())
                .sensorName(reading.getSensor().getName())
                .value(reading.getValue())
                .unit(reading.getSensor().getUnit())
                .timestamp(reading.getTimestamp())
                .build();
    }
}
