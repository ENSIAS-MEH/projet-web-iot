package com.coldroom.repository;

import com.coldroom.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Alert} entities.
 * Included here so the anomaly-detection logic in SensorReadingService
 * can persist alerts without depending on T006's AlertRepository.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /** Returns all unresolved alerts, newest first. */
    List<Alert> findByIsResolvedFalseOrderByCreatedAtDesc();

    /** Returns all alerts for a given sensor, newest first. */
    List<Alert> findBySensorIdOrderByCreatedAtDesc(Integer sensorId);

    /** Returns all alerts ordered by creation time descending. */
    List<Alert> findAllByOrderByCreatedAtDesc();
}
