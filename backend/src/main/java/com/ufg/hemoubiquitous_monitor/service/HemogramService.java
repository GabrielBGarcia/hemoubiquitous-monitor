package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.AnaemiaStatus;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.HemogramAnalysis;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.repository.HemogramRepository;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.service.AnaemiaDetectionService;
import com.ufg.hemoubiquitous_monitor.domain.patient.repository.PatientRepository;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.Hemoglobin;
import com.ufg.hemoubiquitous_monitor.domain.patient.model.Patient;
import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.infrastructure.fhir.mapper.FhirToPatientMapper;
import com.ufg.hemoubiquitous_monitor.infrastructure.fhir.parser.FhirObservationParser;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service

public class HemogramService {
    private final AnaemiaDetectionService anaemiaDetectionService;
    private final HemogramRepository hemogramRepository;
    private final PatientRepository patientRepository;

    public HemogramService(
            AnaemiaDetectionService anaemiaDetectionService,
            HemogramRepository hemogramRepository,
            PatientRepository patientRepository) {
        this.anaemiaDetectionService = anaemiaDetectionService;
        this.hemogramRepository = hemogramRepository;
        this.patientRepository = patientRepository;
    }


    public void receiveHemogram(Observation observation) throws InvalidBloodCountException {
        String identifier = FhirObservationParser.extractFirstIdentifier(observation);
        String loincCode = "718-7";
        Double hemoglobinValue = FhirObservationParser.extractHemoglobinValue(observation, loincCode);
        Date issuedAt = FhirObservationParser.extractIssuedAt(observation);
        org.hl7.fhir.r4.model.Patient fhirPatient = FhirObservationParser.extractPatient(observation);

        if (hemoglobinValue == null) {
            throw new InvalidBloodCountException();
        }

        Hemoglobin hemoglobin = Hemoglobin.fromGramsPerDeciliter(hemoglobinValue);
        Patient patient = FhirToPatientMapper.toDomain(fhirPatient);
        Patient savedPatient = patientRepository.save(patient);
        AnaemiaStatus anaemiaStatus = anaemiaDetectionService.detect(hemoglobin, patient.getGender(), patient.getBirthDate());
        HemogramAnalysis analysis = new HemogramAnalysis(
                identifier,
                loincCode,
                "BLOODCOUNT",
                hemoglobin,
                anaemiaStatus,
                issuedAt,
                savedPatient.getCpf()
        );
        hemogramRepository.save(analysis);
    }


    public void receiveHemoglobin(Observation observation, String id) throws InvalidBloodCountException, InvalidHemoglobinException {
        Boolean isHemoglobin = observation.getCode().getCoding().get(0).getCode().equals("718-7");
        if (!isHemoglobin) {
            throw new InvalidHemoglobinException();
        }

        if (observation.getIdentifier() == null || observation.getIdentifier().isEmpty()) {
            observation.addIdentifier().setSystem("http://example.org/integration-id").setValue(id);
        }

        String identifier = FhirObservationParser.extractFirstIdentifier(observation);
        String loincCode = "718-7";
        Quantity quantity = observation.getValueQuantity();
        Double hemoglobinValue = quantity != null && quantity.getValue() != null ? quantity.getValue().doubleValue() : null;
        Date issuedAt = FhirObservationParser.extractIssuedAt(observation);
        org.hl7.fhir.r4.model.Patient fhirPatient = FhirObservationParser.extractPatient(observation);

        if (hemoglobinValue == null) {
            throw new InvalidHemoglobinException();
        }

        Hemoglobin hemoglobin = Hemoglobin.fromGramsPerDeciliter(hemoglobinValue);
        Patient patient = FhirToPatientMapper.toDomain(fhirPatient);
        Patient savedPatient = patientRepository.save(patient);
        AnaemiaStatus anaemiaStatus = anaemiaDetectionService.detect(hemoglobin, patient.getGender(), patient.getBirthDate());
        HemogramAnalysis analysis = new HemogramAnalysis(
                identifier,
                loincCode,
                "HEMOGLOBIN",
                hemoglobin,
                anaemiaStatus,
                issuedAt,
                savedPatient.getCpf()
        );
        hemogramRepository.save(analysis);
    }

}
