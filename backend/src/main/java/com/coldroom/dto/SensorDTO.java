package com.coldroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.coldroom.entity.Sensor}.
 * Used for both request (create/update) and response payloads.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDTO {

    private Integer id;

    @NotBlank(message = "Sensor name is required")
    private String name;

    @NotBlank(message = "Sensor type is required")
    private String sensorType;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Minimum threshold is required")
    private BigDecimal minThreshold;

    @NotNull(message = "Maximum threshold is required")
    private BigDecimal maxThreshold;

    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
