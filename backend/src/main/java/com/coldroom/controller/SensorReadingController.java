package com.coldroom.controller;

import com.coldroom.dto.SensorReadingDTO;
import com.coldroom.service.SensorReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for sensor reading operations.
 *
 * <p>Base path: {@code /api/readings}</p>
 *
 * <ul>
 *   <li>{@code POST   /api/readings}                                  – add a new reading</li>
 *   <li>{@code GET    /api/readings}                                  – all readings (paginated)</li>
 *   <li>{@code GET    /api/readings/sensor/{sensorId}}                – readings for one sensor</li>
 *   <li>{@code GET    /api/readings/latest}                           – latest reading per sensor</li>
 *   <li>{@code GET    /api/readings/history?sensorId=&startDate=&endDate=} – history with date range</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/readings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService readingService;

    /**
     * POST /api/readings
     * Adds a new sensor reading and triggers anomaly detection.
     *
     * @param dto reading payload (sensorId and value are required)
     * @return 201 Created with the persisted reading
     */
    @PostMapping
    public ResponseEntity<SensorReadingDTO> addReading(@Valid @RequestBody SensorReadingDTO dto) {
        SensorReadingDTO created = readingService.addReading(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/readings?page=0&size=20
     * Returns all readings with pagination, sorted newest first.
     *
     * @param page zero-based page index (default 0)
     * @param size page size (default 20)
     * @return paginated readings
     */
    @GetMapping
    public ResponseEntity<Page<SensorReadingDTO>> getAllReadings(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(readingService.getAllReadings(pageable));
    }

    /**
     * GET /api/readings/latest
     * Returns the most recent reading for each sensor.
     * NOTE: declared before /{sensorId} to avoid path conflict.
     *
     * @return list of latest readings (one per sensor)
     */
    @GetMapping("/latest")
    public ResponseEntity<List<SensorReadingDTO>> getLatestReadings() {
        return ResponseEntity.ok(readingService.getLatestReadings());
    }

    /**
     * GET /api/readings/sensor/{sensorId}
     * Returns all readings for a specific sensor, newest first.
     *
     * @param sensorId sensor identifier
     * @return list of readings
     */
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<List<SensorReadingDTO>> getReadingsBySensor(
            @PathVariable Integer sensorId) {
        return ResponseEntity.ok(readingService.getReadingsBySensor(sensorId));
    }

    /**
     * GET /api/readings/history?sensorId=1&startDate=2026-05-01T00:00:00&endDate=2026-05-14T23:59:59
     * Returns readings for a sensor within a date/time range.
     *
     * @param sensorId  sensor identifier (required)
     * @param startDate range start in ISO-8601 format (optional)
     * @param endDate   range end   in ISO-8601 format (optional)
     * @return list of readings within the range
     */
    @GetMapping("/history")
    public ResponseEntity<List<SensorReadingDTO>> getReadingHistory(
            @RequestParam Integer sensorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(readingService.getReadingHistory(sensorId, startDate, endDate));
    }
}
