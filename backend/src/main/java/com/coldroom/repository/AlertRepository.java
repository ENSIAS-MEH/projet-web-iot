package com.coldroom.repository;

import com.coldroom.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Alert} entities.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /** Returns all unresolved alerts, newest first. */
    List<Alert> findByIsResolvedFalseOrderByCreatedAtDesc();

    /** Returns all resolved alerts, newest first. */
    List<Alert> findByIsResolvedTrueOrderByCreatedAtDesc();

    /** Returns all alerts for a given sensor, newest first. */
    List<Alert> findBySensorIdOrderByCreatedAtDesc(Integer sensorId);

    /** Returns all alerts ordered by creation time descending. */
    List<Alert> findAllByOrderByCreatedAtDesc();

    /** Returns alerts for a sensor filtered by resolved status, newest first. */
    List<Alert> findBySensorIdAndIsResolvedOrderByCreatedAtDesc(Integer sensorId, Boolean isResolved);

    /** Returns alerts filtered by severity, newest first. */
    List<Alert> findBySeverityOrderByCreatedAtDesc(String severity);

    /** Returns alerts filtered by severity and resolved status, newest first. */
    List<Alert> findBySeverityAndIsResolvedOrderByCreatedAtDesc(String severity, Boolean isResolved);
}
