package com.coldroom.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.coldroom.entity.Alert}.
 * Used for response payloads (alerts are created internally by anomaly detection).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {

    private Long id;

    /** ID of the sensor that triggered this alert. */
    private Integer sensorId;

    /** Human-readable sensor name. */
    private String sensorName;

    /** ID of the reading that caused this alert. */
    private Long readingId;

    /** Type of alert: threshold_exceeded | sensor_offline */
    private String alertType;

    /** Severity level: info | warning | critical */
    private String severity;

    /** Human-readable description of the alert. */
    private String message;

    /** Whether the alert has been acknowledged and resolved. */
    private Boolean isResolved;

    /** When the alert was created. */
    private LocalDateTime createdAt;

    /** When the alert was resolved; null if still active. */
    private LocalDateTime resolvedAt;
}
