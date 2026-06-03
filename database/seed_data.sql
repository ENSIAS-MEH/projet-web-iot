-- ============================================================
-- Cold Room Monitoring System - Seed Data
-- Run AFTER schema.sql
-- ============================================================

USE cold_room_db;

-- ------------------------------------------------------------
-- Sample sensors (4 sensors as required)
-- ------------------------------------------------------------
INSERT INTO sensors (name, sensor_type, unit, location, min_threshold, max_threshold, is_active)
VALUES
    ('Temperature Sensor', 'temperature', '°C',      'Main Chamber', -25.00, -15.00, TRUE),
    ('Humidity Sensor',    'humidity',    '%',        'Main Chamber',  40.00,  60.00, TRUE),
    ('Door Sensor',        'door',        'boolean',  'Main Chamber',   0.00,   1.00, TRUE),
    ('Pressure Sensor',    'pressure',    'hPa',      'Main Chamber', 1000.00, 1020.00, TRUE);

-- ------------------------------------------------------------
-- Sample readings (recent, realistic values)
-- ------------------------------------------------------------
INSERT INTO sensor_readings (sensor_id, value, timestamp)
VALUES
    -- Temperature readings (normal range: -25 to -15 °C)
    (1, -18.50, NOW() - INTERVAL 60 MINUTE),
    (1, -18.20, NOW() - INTERVAL 50 MINUTE),
    (1, -17.80, NOW() - INTERVAL 40 MINUTE),
    (1, -18.10, NOW() - INTERVAL 30 MINUTE),
    (1, -18.40, NOW() - INTERVAL 20 MINUTE),
    (1, -18.00, NOW() - INTERVAL 10 MINUTE),
    (1, -18.30, NOW()),

    -- Humidity readings (normal range: 40–60 %)
    (2, 52.00, NOW() - INTERVAL 60 MINUTE),
    (2, 51.50, NOW() - INTERVAL 50 MINUTE),
    (2, 53.00, NOW() - INTERVAL 40 MINUTE),
    (2, 52.50, NOW() - INTERVAL 30 MINUTE),
    (2, 51.00, NOW() - INTERVAL 20 MINUTE),
    (2, 52.00, NOW() - INTERVAL 10 MINUTE),
    (2, 52.30, NOW()),

    -- Door readings (0 = closed, 1 = open)
    (3, 0.00, NOW() - INTERVAL 60 MINUTE),
    (3, 0.00, NOW() - INTERVAL 30 MINUTE),
    (3, 0.00, NOW()),

    -- Pressure readings (normal range: 1000–1020 hPa)
    (4, 1013.00, NOW() - INTERVAL 60 MINUTE),
    (4, 1013.50, NOW() - INTERVAL 50 MINUTE),
    (4, 1012.80, NOW() - INTERVAL 40 MINUTE),
    (4, 1013.20, NOW() - INTERVAL 30 MINUTE),
    (4, 1013.00, NOW() - INTERVAL 20 MINUTE),
    (4, 1012.90, NOW() - INTERVAL 10 MINUTE),
    (4, 1013.10, NOW());

-- ------------------------------------------------------------
-- Sample anomaly reading + alert (temperature spike)
-- ------------------------------------------------------------
INSERT INTO sensor_readings (sensor_id, value, timestamp)
VALUES (1, -10.50, NOW() - INTERVAL 5 MINUTE);

-- Alert for the anomaly reading above (reading id = last inserted)
INSERT INTO alerts (sensor_id, reading_id, alert_type, severity, message, is_resolved, created_at)
VALUES (
    1,
    LAST_INSERT_ID(),
    'threshold_exceeded',
    'critical',
    'Temperature exceeded maximum threshold: -10.5°C (max: -15.0°C)',
    FALSE,
    NOW() - INTERVAL 5 MINUTE
);
