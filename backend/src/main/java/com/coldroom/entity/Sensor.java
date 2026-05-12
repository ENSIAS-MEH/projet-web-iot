package com.coldroom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an IoT sensor installed in the cold room.
 * Maps to the `sensors` table in cold_room_db.
 */
@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Human-readable sensor name, e.g. "Temperature Sensor". */
    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Sensor category: temperature | humidity | door | pressure
     */
    @NotBlank
    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType;

    /** Measurement unit, e.g. °C, %, boolean, hPa. */
    @NotBlank
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /** Physical location of the sensor, e.g. "Main Chamber". */
    @NotBlank
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    /** Minimum safe value; readings below this trigger an alert. */
    @NotNull
    @Column(name = "min_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal minThreshold;

    /** Maximum safe value; readings above this trigger an alert. */
    @NotNull
    @Column(name = "max_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxThreshold;

    /** Whether the sensor is currently active and sending data. */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
