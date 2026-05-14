package com.coldroom.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.coldroom.entity.Alert}.
 * Used for response payloads.
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

    /** Name of the sensor – for display purposes. */
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

    private LocalDateTime createdAt;

    /** Null if the alert is still active. */
    private LocalDateTime resolvedAt;
}
