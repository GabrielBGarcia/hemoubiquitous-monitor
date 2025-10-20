package com.ufg.hemoubiquitous_monitor.repository;

import com.ufg.hemoubiquitous_monitor.model.PatientData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDataRepository extends JpaRepository<PatientData, Long> {
    Optional<PatientData> findByCpf(String cpf);
}
