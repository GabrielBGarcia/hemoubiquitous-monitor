package com.ufg.hemoubiquitous_monitor.repository;

import com.ufg.hemoubiquitous_monitor.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
}

