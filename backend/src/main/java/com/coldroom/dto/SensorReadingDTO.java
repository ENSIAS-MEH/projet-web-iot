package com.coldroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.coldroom.entity.SensorReading}.
 * Used for both request (create) and response payloads.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReadingDTO {

    /** Set by the server on response; ignored on create requests. */
    private Long id;

    /** ID of the sensor that produced this reading. Required on create. */
    @NotNull(message = "sensorId is required")
    private Integer sensorId;

    /** Sensor name – populated on response only. */
    private String sensorName;

    /** The measured value. */
    @NotNull(message = "value is required")
    private BigDecimal value;

    /** Unit of the sensor (e.g. °C). Populated on response only. */
    private String unit;

    /**
     * When the reading was taken.
     * If omitted on create, defaults to the current server time.
     */
    private LocalDateTime timestamp;
}
