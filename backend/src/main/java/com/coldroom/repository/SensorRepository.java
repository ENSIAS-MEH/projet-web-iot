package com.coldroom.repository;

import com.coldroom.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Sensor} entities.
 */
@Repository
public interface SensorRepository extends JpaRepository<Sensor, Integer> {

    /** Returns all sensors that are currently active. */
    List<Sensor> findByIsActiveTrue();

    /** Returns all sensors matching the given type (e.g. "temperature"). */
    List<Sensor> findBySensorType(String sensorType);
}
