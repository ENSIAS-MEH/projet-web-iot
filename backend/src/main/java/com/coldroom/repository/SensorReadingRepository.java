package com.coldroom.repository;

import com.coldroom.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SensorReading} entities.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /** Returns all readings for a given sensor, newest first. */
    List<SensorReading> findBySensorIdOrderByTimestampDesc(Integer sensorId);

    /** Returns paginated readings for a given sensor, newest first. */
    Page<SensorReading> findBySensorIdOrderByTimestampDesc(Integer sensorId, Pageable pageable);

    /** Returns readings for a sensor within a time range, newest first. */
    List<SensorReading> findBySensorIdAndTimestampBetweenOrderByTimestampDesc(
            Integer sensorId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Returns the single most recent reading for each active sensor.
     * Uses a subquery to find the max timestamp per sensor.
     */
    @Query("""
            SELECT r FROM SensorReading r
            WHERE r.timestamp = (
                SELECT MAX(r2.timestamp)
                FROM SensorReading r2
                WHERE r2.sensor.id = r.sensor.id
            )
            ORDER BY r.sensor.id
            """)
    List<SensorReading> findLatestPerSensor();

    /** Returns the most recent reading for a specific sensor. */
    Optional<SensorReading> findTopBySensorIdOrderByTimestampDesc(Integer sensorId);
}
