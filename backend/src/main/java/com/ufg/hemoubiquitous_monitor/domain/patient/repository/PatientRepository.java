package com.ufg.hemoubiquitous_monitor.domain.patient.repository;

import com.ufg.hemoubiquitous_monitor.domain.patient.model.Patient;

import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);
    Optional<Patient> findByCpf(String cpf);
}
