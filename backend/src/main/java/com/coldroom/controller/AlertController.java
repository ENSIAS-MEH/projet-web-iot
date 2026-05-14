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
 * <p>Base path: {@code /api/alerts}</p>
 *
 * <ul>
 *   <li>{@code GET  /api/alerts}                    – all alerts (optional filters)</li>
 *   <li>{@code GET  /api/alerts/active}              – unresolved alerts only</li>
 *   <li>{@code GET  /api/alerts/{id}}                – single alert by ID</li>
 *   <li>{@code PUT  /api/alerts/{id}/resolve}        – mark alert as resolved</li>
 *   <li>{@code GET  /api/alerts/sensor/{sensorId}}   – alerts for a specific sensor</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts?isResolved=false&severity=critical
     * Returns all alerts, optionally filtered by resolved status and/or severity.
     *
     * @param isResolved optional filter (true/false)
     * @param severity   optional filter (info/warning/critical)
     */
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts(
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(required = false) String severity) {

        if (isResolved != null || severity != null) {
            return ResponseEntity.ok(alertService.getFilteredAlerts(isResolved, severity));
        }
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
     * GET /api/alerts/{id}
     * Returns a single alert by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    /**
     * PUT /api/alerts/{id}/resolve
     * Marks an alert as resolved and records the resolution timestamp.
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertDTO> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlert(id));
    }

    /**
     * GET /api/alerts/sensor/{sensorId}
     * Returns all alerts for a specific sensor, newest first.
     */
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<List<AlertDTO>> getAlertsBySensor(@PathVariable Integer sensorId) {
        return ResponseEntity.ok(alertService.getAlertsBySensor(sensorId));
    }
}
