package com.coldroom.controller;

import com.coldroom.dto.SensorDTO;
import com.coldroom.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for sensor CRUD operations.
 *
 * Base path: /api/sensors
 */
@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /**
     * GET /api/sensors
     * Returns all sensors.
     */
    @GetMapping
    public ResponseEntity<List<SensorDTO>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    /**
     * GET /api/sensors/active
     * Returns only active sensors.
     * NOTE: must be declared before /{id} to avoid path conflict.
     */
    @GetMapping("/active")
    public ResponseEntity<List<SensorDTO>> getActiveSensors() {
        return ResponseEntity.ok(sensorService.getActiveSensors());
    }

    /**
     * GET /api/sensors/{id}
     * Returns a single sensor by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SensorDTO> getSensorById(@PathVariable Integer id) {
        return ResponseEntity.ok(sensorService.getSensorById(id));
    }

    /**
     * POST /api/sensors
     * Creates a new sensor.
     */
    @PostMapping
    public ResponseEntity<SensorDTO> createSensor(@Valid @RequestBody SensorDTO dto) {
        SensorDTO created = sensorService.createSensor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/sensors/{id}
     * Fully updates an existing sensor.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SensorDTO> updateSensor(
            @PathVariable Integer id,
            @Valid @RequestBody SensorDTO dto) {
        return ResponseEntity.ok(sensorService.updateSensor(id, dto));
    }

    /**
     * DELETE /api/sensors/{id}
     * Deletes a sensor by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Integer id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }
}
