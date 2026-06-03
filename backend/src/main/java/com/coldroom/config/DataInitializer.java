package com.coldroom.config;

import com.coldroom.entity.Alert;
import com.coldroom.entity.Sensor;
import com.coldroom.entity.SensorReading;
import com.coldroom.repository.AlertRepository;
import com.coldroom.repository.SensorReadingRepository;
import com.coldroom.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final AlertRepository alertRepository;

    @Override
    public void run(String... args) {
        // Create sensors
        Sensor tempSensor = Sensor.builder()
                .name("Temperature Sensor")
                .sensorType("temperature")
                .unit("Ã‚Â°C")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("-25.00"))
                .maxThreshold(new BigDecimal("-15.00"))
                .isActive(true)
                .build();

        Sensor humiditySensor = Sensor.builder()
                .name("Humidity Sensor")
                .sensorType("humidity")
                .unit("%")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("40.00"))
                .maxThreshold(new BigDecimal("60.00"))
                .isActive(true)
                .build();

        Sensor doorSensor = Sensor.builder()
                .name("Door Sensor")
                .sensorType("door")
                .unit("boolean")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("0.00"))
                .maxThreshold(new BigDecimal("0.00"))
                .isActive(true)
                .build();

        Sensor pressureSensor = Sensor.builder()
                .name("Pressure Sensor")
                .sensorType("pressure")
                .unit("hPa")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("1000.00"))
                .maxThreshold(new BigDecimal("1020.00"))
                .isActive(true)
                .build();

        tempSensor = sensorRepository.save(tempSensor);
        humiditySensor = sensorRepository.save(humiditySensor);
        doorSensor = sensorRepository.save(doorSensor);
        pressureSensor = sensorRepository.save(pressureSensor);

        // Add sample readings
        LocalDateTime now = LocalDateTime.now();

        // Temperature readings
        addReading(tempSensor, new BigDecimal("-18.50"), now.minusMinutes(60));
        addReading(tempSensor, new BigDecimal("-18.20"), now.minusMinutes(50));
        addReading(tempSensor, new BigDecimal("-17.80"), now.minusMinutes(40));
        addReading(tempSensor, new BigDecimal("-18.10"), now.minusMinutes(30));
        addReading(tempSensor, new BigDecimal("-18.40"), now.minusMinutes(20));
        addReading(tempSensor, new BigDecimal("-18.00"), now.minusMinutes(10));
        addReading(tempSensor, new BigDecimal("-18.30"), now);

        // Humidity readings
        addReading(humiditySensor, new BigDecimal("52.00"), now.minusMinutes(60));
        addReading(humiditySensor, new BigDecimal("51.50"), now.minusMinutes(50));
        addReading(humiditySensor, new BigDecimal("53.00"), now.minusMinutes(40));
        addReading(humiditySensor, new BigDecimal("52.50"), now.minusMinutes(30));
        addReading(humiditySensor, new BigDecimal("51.00"), now.minusMinutes(20));
        addReading(humiditySensor, new BigDecimal("52.00"), now.minusMinutes(10));
        addReading(humiditySensor, new BigDecimal("52.30"), now);

        // Door readings
        addReading(doorSensor, new BigDecimal("0.00"), now.minusMinutes(60));
        addReading(doorSensor, new BigDecimal("0.00"), now.minusMinutes(30));
        addReading(doorSensor, new BigDecimal("0.00"), now);

        // Pressure readings
        addReading(pressureSensor, new BigDecimal("1013.00"), now.minusMinutes(60));
        addReading(pressureSensor, new BigDecimal("1013.50"), now.minusMinutes(50));
        addReading(pressureSensor, new BigDecimal("1012.80"), now.minusMinutes(40));
        addReading(pressureSensor, new BigDecimal("1013.20"), now.minusMinutes(30));
        addReading(pressureSensor, new BigDecimal("1013.00"), now.minusMinutes(20));
        addReading(pressureSensor, new BigDecimal("1012.90"), now.minusMinutes(10));
        addReading(pressureSensor, new BigDecimal("1013.10"), now);

        // Add anomaly reading and alert
        SensorReading anomalyReading = addReading(tempSensor, new BigDecimal("-10.50"), now.minusMinutes(5));

        Alert alert = Alert.builder()
                .sensor(tempSensor)
                .reading(anomalyReading)
                .alertType("threshold_exceeded")
                .severity("critical")
                .message("Temperature exceeded maximum threshold: -10.5Ã‚Â°C (max: -15.0Ã‚Â°C)")
                .isResolved(false)
                .createdAt(now.minusMinutes(5))
                .build();

        alertRepository.save(alert);
    }

    private SensorReading addReading(Sensor sensor, BigDecimal value, LocalDateTime timestamp) {
        SensorReading reading = SensorReading.builder()
                .sensor(sensor)
                .value(value)
                .timestamp(timestamp)
                .build();
        return sensorReadingRepository.save(reading);
    }
}
