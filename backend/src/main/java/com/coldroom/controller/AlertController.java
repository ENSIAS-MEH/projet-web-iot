package com.coldroom.controller;

import com.coldroom.dto.AlertDTO;
import com.coldroom.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for alert management.
 *
 * Base path: /api/alerts
 *
 * GET  /api/alerts                  – all alerts
 * GET  /api/alerts/active           – unresolved alerts only
 * GET  /api/alerts/{id}             – single alert by ID
 * PUT  /api/alerts/{id}/resolve     – mark alert as resolved
 * GET  /api/alerts/sensor/{sensorId} – alerts for a specific sensor
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts
     * Returns all alerts, newest first.
     */
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    /**
     * GET /api/alerts/active
     * Returns only unresolved alerts, newest first.
     * NOTE: declared before /{id} to avoid path conflict.
     */
    @GetMapping("/active")
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    /**
     * GET /api/alerts/sensor/{sensorId}
     * Returns all alerts for a specific sensor, newest first.
     * NOTE: declared before /{id} to avoid path conflict.
     */
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<List<AlertDTO>> getAlertsBySensor(@PathVariable Integer sensorId) {
        return ResponseEntity.ok(alertService.getAlertsBySensor(sensorId));
    }

    /**
     * GET /api/alerts/{id}
     * Returns a single alert by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    /**
     * PUT /api/alerts/{id}/resolve
     * Marks an alert as resolved.
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertDTO> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }
}
