package com.coldroom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an alert generated when a sensor reading exceeds its thresholds.
 * Maps to the `alerts` table in cold_room_db.
 */
@Entity
@Table(
    name = "alerts",
    indexes = {
        @Index(name = "idx_alerts_sensor_id",   columnList = "sensor_id"),
        @Index(name = "idx_alerts_is_resolved",  columnList = "is_resolved"),
        @Index(name = "idx_alerts_severity",     columnList = "severity"),
        @Index(name = "idx_alerts_created_at",   columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The sensor that triggered this alert. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /** The reading that caused this alert. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reading_id", nullable = false)
    private SensorReading reading;

    /**
     * Type of alert: threshold_exceeded | sensor_offline
     */
    @NotBlank
    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    /**
     * Severity level: info | warning | critical
     */
    @NotBlank
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    /** Human-readable description of the alert. */
    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Whether the alert has been acknowledged and resolved. */
    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the alert was resolved; null if still active. */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
