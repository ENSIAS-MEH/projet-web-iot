package com.coldroom;

import com.coldroom.dto.SensorDTO;
import com.coldroom.dto.SensorReadingDTO;
import com.coldroom.repository.AlertRepository;
import com.coldroom.service.SensorReadingService;
import com.coldroom.service.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the Readings API (T005) using H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SensorReadingApiTest {

    @Autowired
    private SensorReadingService readingService;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private AlertRepository alertRepository;

    private SensorDTO sensor;

    @BeforeEach
    void setUp() {
        sensor = sensorService.createSensor(SensorDTO.builder()
                .name("Temperature Sensor")
                .sensorType("temperature")
                .unit("°C")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("-25.00"))
                .maxThreshold(new BigDecimal("-15.00"))
                .isActive(true)
                .build());
    }

    // ----------------------------------------------------------------
    // addReading
    // ----------------------------------------------------------------

    @Test
    void addReading_shouldPersistAndReturnWithId() {
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-18.50"))
                .build();

        SensorReadingDTO saved = readingService.addReading(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSensorId()).isEqualTo(sensor.getId());
        assertThat(saved.getValue()).isEqualByComparingTo(new BigDecimal("-18.50"));
        assertThat(saved.getSensorName()).isEqualTo("Temperature Sensor");
        assertThat(saved.getUnit()).isEqualTo("°C");
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    void addReading_withExplicitTimestamp_shouldUseProvidedTimestamp() {
        LocalDateTime ts = LocalDateTime.of(2026, 5, 10, 12, 0, 0);
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-20.00"))
                .timestamp(ts)
                .build();

        SensorReadingDTO saved = readingService.addReading(dto);

        assertThat(saved.getTimestamp()).isEqualTo(ts);
    }

    @Test
    void addReading_withUnknownSensor_shouldThrow() {
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(9999)
                .value(new BigDecimal("-18.00"))
                .build();

        assertThatThrownBy(() -> readingService.addReading(dto))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    // ----------------------------------------------------------------
    // Anomaly detection
    // ----------------------------------------------------------------

    @Test
    void addReading_withinThresholds_shouldNotCreateAlert() {
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-20.00")) // within [-25, -15]
                .build();

        readingService.addReading(dto);

        assertThat(alertRepository.findAll()).isEmpty();
    }

    @Test
    void addReading_aboveMaxThreshold_shouldCreateAlert() {
        // -10 is above max (-15) → alert expected
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-10.00"))
                .build();

        readingService.addReading(dto);

        var alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo("threshold_exceeded");
        assertThat(alerts.get(0).getMessage()).contains("above maximum");
        assertThat(alerts.get(0).getIsResolved()).isFalse();
    }

    @Test
    void addReading_belowMinThreshold_shouldCreateAlert() {
        // -30 is below min (-25) → alert expected
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-30.00"))
                .build();

        readingService.addReading(dto);

        var alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("below minimum");
    }

    @Test
    void addReading_criticalDeviation_shouldHaveCriticalSeverity() {
        // Range = 10, deviation = 5 (50% > 20%) → critical
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-10.00")) // 5 above max of -15
                .build();

        readingService.addReading(dto);

        var alerts = alertRepository.findAll();
        assertThat(alerts.get(0).getSeverity()).isEqualTo("critical");
    }

    @Test
    void addReading_infoDeviation_shouldHaveInfoSeverity() {
        // Range = 10, deviation = 0.5 (5% ≤ 10%) → info
        SensorReadingDTO dto = SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(new BigDecimal("-14.50")) // 0.5 above max of -15
                .build();

        readingService.addReading(dto);

        var alerts = alertRepository.findAll();
        assertThat(alerts.get(0).getSeverity()).isEqualTo("info");
    }

    // ----------------------------------------------------------------
    // getAllReadings (paginated)
    // ----------------------------------------------------------------

    @Test
    void getAllReadings_shouldReturnPagedResults() {
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-18.00")).build());
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-19.00")).build());

        Page<SensorReadingDTO> page = readingService.getAllReadings(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    // ----------------------------------------------------------------
    // getReadingsBySensor
    // ----------------------------------------------------------------

    @Test
    void getReadingsBySensor_shouldReturnOnlyThatSensorsReadings() {
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-18.00")).build());

        List<SensorReadingDTO> readings = readingService.getReadingsBySensor(sensor.getId());

        assertThat(readings).isNotEmpty();
        assertThat(readings).allMatch(r -> r.getSensorId().equals(sensor.getId()));
    }

    @Test
    void getReadingsBySensor_withUnknownSensor_shouldThrow() {
        assertThatThrownBy(() -> readingService.getReadingsBySensor(9999))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    // ----------------------------------------------------------------
    // getLatestReadings
    // ----------------------------------------------------------------

    @Test
    void getLatestReadings_shouldReturnMostRecentPerSensor() {
        LocalDateTime earlier = LocalDateTime.now().minusMinutes(10);
        LocalDateTime later   = LocalDateTime.now();

        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-18.00")).timestamp(earlier).build());
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-17.00")).timestamp(later).build());

        List<SensorReadingDTO> latest = readingService.getLatestReadings();

        assertThat(latest).hasSize(1);
        assertThat(latest.get(0).getValue()).isEqualByComparingTo(new BigDecimal("-17.00"));
    }

    // ----------------------------------------------------------------
    // getReadingHistory
    // ----------------------------------------------------------------

    @Test
    void getReadingHistory_shouldFilterByDateRange() {
        LocalDateTime base = LocalDateTime.of(2026, 5, 10, 12, 0, 0);

        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-18.00")).timestamp(base).build());
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-19.00")).timestamp(base.plusHours(2)).build());
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-20.00")).timestamp(base.plusDays(2)).build());

        List<SensorReadingDTO> history = readingService.getReadingHistory(
                sensor.getId(),
                base.minusMinutes(1),
                base.plusHours(3));

        assertThat(history).hasSize(2);
    }

    @Test
    void getReadingHistory_withInvalidRange_shouldThrow() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.minusDays(1); // end before start

        assertThatThrownBy(() -> readingService.getReadingHistory(sensor.getId(), start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startDate must be before endDate");
    }
}
