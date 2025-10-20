package com.ufg.hemoubiquitous_monitor.repository;

import com.ufg.hemoubiquitous_monitor.model.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
	List<Observation> findByIssuedAtBetween(Date from, Date to);
}

