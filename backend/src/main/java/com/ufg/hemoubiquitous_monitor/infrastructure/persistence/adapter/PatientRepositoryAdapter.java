package com.ufg.hemoubiquitous_monitor.infrastructure.persistence.adapter;

import com.ufg.hemoubiquitous_monitor.domain.patient.model.Patient;
import com.ufg.hemoubiquitous_monitor.domain.patient.repository.PatientRepository;
import com.ufg.hemoubiquitous_monitor.infrastructure.persistence.mapper.PatientEntityMapper;
import com.ufg.hemoubiquitous_monitor.model.PatientData;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PatientRepositoryAdapter implements PatientRepository {
    private final PatientDataRepository patientDataRepository;

    public PatientRepositoryAdapter(PatientDataRepository patientDataRepository) {
        this.patientDataRepository = patientDataRepository;
    }

    @Override
    public Patient save(Patient patient) {
        PatientData entity = PatientEntityMapper.toEntity(patient, patientDataRepository);
        patientDataRepository.save(entity);
        return patient;
    }

    @Override
    public Optional<Patient> findByCpf(String cpf) {
        return patientDataRepository.findByCpf(cpf)
                .map(PatientEntityMapper::toDomain);
    }
}
