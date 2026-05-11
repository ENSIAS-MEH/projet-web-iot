-- ============================================================
-- Cold Room Monitoring System - Database Schema
-- Database: cold_room_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS cold_room_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE cold_room_db;

-- ------------------------------------------------------------
-- Table: sensors
-- Stores metadata for each IoT sensor in the cold room.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sensors (
    id            INT            NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)   NOT NULL,
    sensor_type   VARCHAR(50)    NOT NULL COMMENT 'temperature | humidity | door | pressure',
    unit          VARCHAR(20)    NOT NULL COMMENT 'e.g. °C, %, boolean, hPa',
    location      VARCHAR(100)   NOT NULL DEFAULT 'Main Chamber',
    min_threshold DECIMAL(10, 2) NOT NULL,
    max_threshold DECIMAL(10, 2) NOT NULL,
    is_active     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_sensors_threshold CHECK (min_threshold < max_threshold)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: sensor_readings
-- Stores every reading sent by the simulator / real sensors.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sensor_readings (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    sensor_id  INT            NOT NULL,
    value      DECIMAL(10, 4) NOT NULL,
    timestamp  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_readings_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_readings_sensor_id  (sensor_id),
    INDEX idx_readings_timestamp  (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: alerts
-- Stores alerts generated when a reading exceeds thresholds.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alerts (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    sensor_id   INT          NOT NULL,
    reading_id  BIGINT       NOT NULL,
    alert_type  VARCHAR(50)  NOT NULL COMMENT 'threshold_exceeded | sensor_offline',
    severity    VARCHAR(20)  NOT NULL COMMENT 'info | warning | critical',
    message     TEXT         NOT NULL,
    is_resolved BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP    NULL     DEFAULT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_alerts_sensor
        FOREIGN KEY (sensor_id)  REFERENCES sensors (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_alerts_reading
        FOREIGN KEY (reading_id) REFERENCES sensor_readings (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_alerts_sensor_id   (sensor_id),
    INDEX idx_alerts_is_resolved (is_resolved),
    INDEX idx_alerts_severity    (severity),
    INDEX idx_alerts_created_at  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
