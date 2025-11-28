package com.ufg.hemoubiquitous_monitor.infrastructure.persistence.adapter;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.HemogramAnalysis;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.repository.HemogramRepository;
import com.ufg.hemoubiquitous_monitor.infrastructure.persistence.mapper.ObservationEntityMapper;
import com.ufg.hemoubiquitous_monitor.model.Observation;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;
import org.springframework.stereotype.Component;

@Component
public class HemogramRepositoryAdapter implements HemogramRepository {
    private final ObservationRepository observationRepository;
    private final PatientDataRepository patientDataRepository;

    public HemogramRepositoryAdapter(
            ObservationRepository observationRepository,
            PatientDataRepository patientDataRepository) {
        this.observationRepository = observationRepository;
        this.patientDataRepository = patientDataRepository;
    }

    @Override
    public HemogramAnalysis save(HemogramAnalysis analysis) {
        Observation entity = ObservationEntityMapper.toEntity(analysis, patientDataRepository);
        observationRepository.save(entity);
        return analysis;
    }
}
