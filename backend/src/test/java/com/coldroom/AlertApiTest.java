package com.coldroom;

import com.coldroom.dto.AlertDTO;
import com.coldroom.dto.SensorDTO;
import com.coldroom.dto.SensorReadingDTO;
import com.coldroom.service.AlertService;
import com.coldroom.service.SensorReadingService;
import com.coldroom.service.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the Alerts API (T006) using H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlertApiTest {

    @Autowired private AlertService        alertService;
    @Autowired private SensorReadingService readingService;
    @Autowired private SensorService       sensorService;

    private SensorDTO sensor;

    @BeforeEach
    void setUp() {
        // Create a temperature sensor with thresholds [-25, -15]
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

    /** Helper: post a reading that exceeds the max threshold → creates an alert. */
    private AlertDTO createAlertViaReading(BigDecimal value) {
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId())
                .value(value)
                .build());
        List<AlertDTO> alerts = alertService.getAllAlerts();
        assertThat(alerts).isNotEmpty();
        return alerts.get(0);
    }

    // ----------------------------------------------------------------
    // getAllAlerts
    // ----------------------------------------------------------------

    @Test
    void getAllAlerts_whenNoAlerts_shouldReturnEmptyList() {
        // Normal reading – no alert
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-20.00")).build());

        assertThat(alertService.getAllAlerts()).isEmpty();
    }

    @Test
    void getAllAlerts_shouldReturnCreatedAlerts() {
        createAlertViaReading(new BigDecimal("-10.00")); // above max

        List<AlertDTO> alerts = alertService.getAllAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getSensorId()).isEqualTo(sensor.getId());
        assertThat(alerts.get(0).getSensorName()).isEqualTo("Temperature Sensor");
        assertThat(alerts.get(0).getAlertType()).isEqualTo("threshold_exceeded");
        assertThat(alerts.get(0).getIsResolved()).isFalse();
        assertThat(alerts.get(0).getResolvedAt()).isNull();
    }

    // ----------------------------------------------------------------
    // getActiveAlerts
    // ----------------------------------------------------------------

    @Test
    void getActiveAlerts_shouldOnlyReturnUnresolved() {
        AlertDTO alert = createAlertViaReading(new BigDecimal("-10.00"));

        // Resolve it
        alertService.resolveAlert(alert.getId());

        // Active list should now be empty
        assertThat(alertService.getActiveAlerts()).isEmpty();
    }

    @Test
    void getActiveAlerts_shouldReturnUnresolvedAlerts() {
        createAlertViaReading(new BigDecimal("-10.00"));

        List<AlertDTO> active = alertService.getActiveAlerts();
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getIsResolved()).isFalse();
    }

    // ----------------------------------------------------------------
    // getAlertById
    // ----------------------------------------------------------------

    @Test
    void getAlertById_shouldReturnCorrectAlert() {
        AlertDTO created = createAlertViaReading(new BigDecimal("-10.00"));

        AlertDTO found = alertService.getAlertById(created.getId());
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getMessage()).isNotBlank();
    }

    @Test
    void getAlertById_withUnknownId_shouldThrow() {
        assertThatThrownBy(() -> alertService.getAlertById(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Alert not found");
    }

    // ----------------------------------------------------------------
    // resolveAlert
    // ----------------------------------------------------------------

    @Test
    void resolveAlert_shouldMarkAsResolvedWithTimestamp() {
        AlertDTO alert = createAlertViaReading(new BigDecimal("-10.00"));

        AlertDTO resolved = alertService.resolveAlert(alert.getId());

        assertThat(resolved.getIsResolved()).isTrue();
        assertThat(resolved.getResolvedAt()).isNotNull();
    }

    @Test
    void resolveAlert_alreadyResolved_shouldThrow() {
        AlertDTO alert = createAlertViaReading(new BigDecimal("-10.00"));
        alertService.resolveAlert(alert.getId());

        assertThatThrownBy(() -> alertService.resolveAlert(alert.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already resolved");
    }

    @Test
    void resolveAlert_withUnknownId_shouldThrow() {
        assertThatThrownBy(() -> alertService.resolveAlert(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    // ----------------------------------------------------------------
    // getAlertsBySensor
    // ----------------------------------------------------------------

    @Test
    void getAlertsBySensor_shouldReturnOnlyThatSensorsAlerts() {
        // Create a second sensor
        SensorDTO sensor2 = sensorService.createSensor(SensorDTO.builder()
                .name("Humidity Sensor")
                .sensorType("humidity")
                .unit("%")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("40.00"))
                .maxThreshold(new BigDecimal("60.00"))
                .isActive(true)
                .build());

        // Trigger alert on sensor 1
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor.getId()).value(new BigDecimal("-10.00")).build());

        // Trigger alert on sensor 2
        readingService.addReading(SensorReadingDTO.builder()
                .sensorId(sensor2.getId()).value(new BigDecimal("80.00")).build());

        List<AlertDTO> sensor1Alerts = alertService.getAlertsBySensor(sensor.getId());
        assertThat(sensor1Alerts).hasSize(1);
        assertThat(sensor1Alerts.get(0).getSensorId()).isEqualTo(sensor.getId());
    }

    // ----------------------------------------------------------------
    // getFilteredAlerts
    // ----------------------------------------------------------------

    @Test
    void getFilteredAlerts_byIsResolved_false_shouldReturnActiveOnly() {
        AlertDTO alert = createAlertViaReading(new BigDecimal("-10.00"));
        alertService.resolveAlert(alert.getId());

        // Create a second unresolved alert
        createAlertViaReading(new BigDecimal("-5.00"));

        List<AlertDTO> active = alertService.getFilteredAlerts(false, null);
        assertThat(active).allMatch(a -> !a.getIsResolved());
    }

    @Test
    void getFilteredAlerts_bySeverity_shouldReturnMatchingOnly() {
        createAlertViaReading(new BigDecimal("-10.00")); // critical (50% deviation)

        List<AlertDTO> critical = alertService.getFilteredAlerts(null, "critical");
        assertThat(critical).isNotEmpty();
        assertThat(critical).allMatch(a -> "critical".equals(a.getSeverity()));
    }
}
