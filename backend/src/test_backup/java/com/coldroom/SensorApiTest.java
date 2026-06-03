package com.coldroom;

import com.coldroom.dto.SensorDTO;
import com.coldroom.service.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the Sensor API layer using H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SensorApiTest {

    @Autowired
    private SensorService sensorService;

    private SensorDTO buildSampleDTO() {
        return SensorDTO.builder()
                .name("Temperature Sensor")
                .sensorType("temperature")
                .unit("°C")
                .location("Main Chamber")
                .minThreshold(new BigDecimal("-25.00"))
                .maxThreshold(new BigDecimal("-15.00"))
                .isActive(true)
                .build();
    }

    @Test
    void createSensor_shouldPersistAndReturnWithId() {
        SensorDTO created = sensorService.createSensor(buildSampleDTO());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Temperature Sensor");
        assertThat(created.getSensorType()).isEqualTo("temperature");
        assertThat(created.getIsActive()).isTrue();
    }

    @Test
    void getAllSensors_shouldReturnCreatedSensor() {
        sensorService.createSensor(buildSampleDTO());

        List<SensorDTO> sensors = sensorService.getAllSensors();
        assertThat(sensors).isNotEmpty();
    }

    @Test
    void getActiveSensors_shouldOnlyReturnActiveSensors() {
        SensorDTO active = buildSampleDTO();
        active.setIsActive(true);
        sensorService.createSensor(active);

        SensorDTO inactive = buildSampleDTO();
        inactive.setName("Inactive Sensor");
        inactive.setIsActive(false);
        sensorService.createSensor(inactive);

        List<SensorDTO> activeSensors = sensorService.getActiveSensors();
        assertThat(activeSensors).allMatch(SensorDTO::getIsActive);
    }

    @Test
    void getSensorById_shouldReturnCorrectSensor() {
        SensorDTO created = sensorService.createSensor(buildSampleDTO());

        SensorDTO found = sensorService.getSensorById(created.getId());
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Temperature Sensor");
    }

    @Test
    void updateSensor_shouldModifyFields() {
        SensorDTO created = sensorService.createSensor(buildSampleDTO());

        SensorDTO update = buildSampleDTO();
        update.setName("Updated Sensor");
        update.setMinThreshold(new BigDecimal("-30.00"));

        SensorDTO updated = sensorService.updateSensor(created.getId(), update);
        assertThat(updated.getName()).isEqualTo("Updated Sensor");
        assertThat(updated.getMinThreshold()).isEqualByComparingTo(new BigDecimal("-30.00"));
    }

    @Test
    void deleteSensor_shouldRemoveSensor() {
        SensorDTO created = sensorService.createSensor(buildSampleDTO());
        Integer id = created.getId();

        sensorService.deleteSensor(id);

        assertThatThrownBy(() -> sensorService.getSensorById(id))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void createSensor_withInvalidThresholds_shouldThrow() {
        SensorDTO bad = buildSampleDTO();
        bad.setMinThreshold(new BigDecimal("10.00"));
        bad.setMaxThreshold(new BigDecimal("5.00")); // min > max

        assertThatThrownBy(() -> sensorService.createSensor(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minThreshold must be less than maxThreshold");
    }
}
