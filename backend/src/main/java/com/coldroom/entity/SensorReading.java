package com.coldroom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single data point recorded by a sensor.
 * Maps to the `sensor_readings` table in cold_room_db.
 */
@Entity
@Table(
    name = "sensor_readings",
    indexes = {
        @Index(name = "idx_readings_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_readings_timestamp",  columnList = "timestamp")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The sensor that produced this reading. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /** The measured value (e.g. -18.5 for temperature in °C). */
    @NotNull
    @Column(name = "value", nullable = false, precision = 10, scale = 4)
    private BigDecimal value;

    /** When the reading was recorded. Defaults to the current time. */
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
