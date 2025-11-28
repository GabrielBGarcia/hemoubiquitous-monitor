package com.ufg.hemoubiquitous_monitor.infrastructure.persistence.mapper;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.HemogramAnalysis;
import com.ufg.hemoubiquitous_monitor.model.Observation;
import com.ufg.hemoubiquitous_monitor.model.PatientData;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;

import java.util.Date;

public class ObservationEntityMapper {

    public static Observation toEntity(HemogramAnalysis analysis, PatientDataRepository patientDataRepository) {
        PatientData patientData = patientDataRepository.findByCpf(analysis.getPatientCpf()).orElse(null);
        
        Observation entity = new Observation(
                analysis.getIdentifier(),
                analysis.getLoincCode(),
                analysis.getOrigin(),
                analysis.getHemoglobin().getValueInGramsPerLiter(),
                Date.from(analysis.getAnaemiaStatus().getAnalyzedAt()),
                analysis.getAnaemiaStatus().hasAnaemia()
        );
        
        entity.setPatientData(patientData);
        entity.setIssuedAt(analysis.getIssuedAt());
        
        return entity;
    }
}
