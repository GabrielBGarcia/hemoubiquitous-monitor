package com.ufg.hemoubiquitous_monitor.infrastructure.fhir.parser;

import org.hl7.fhir.r4.model.*;

import java.util.Date;

public class FhirObservationParser {

    public static Double extractHemoglobinValue(Observation observation, String loincCode) {
        if (observation.hasValueQuantity()) {
            Quantity quantity = observation.getValueQuantity();
            if (quantity != null && quantity.getValue() != null) {
                return quantity.getValue().doubleValue();
            }
        }
        
        if (observation.hasComponent()) {
            return observation.getComponent().stream()
                    .filter(component -> component.getCode().getCoding().stream()
                            .anyMatch(coding -> "http://loinc.org".equals(coding.getSystem()) && loincCode.equals(coding.getCode())))
                    .findFirst()
                    .map(Observation.ObservationComponentComponent::getValueQuantity)
                    .map(q -> q.getValue() != null ? q.getValue().doubleValue() : null)
                    .orElse(null);
        }
        
        return null;
    }

    public static String extractFirstIdentifier(Observation observation) {
        if (observation.hasIdentifier() && !observation.getIdentifier().isEmpty()) {
            return observation.getIdentifier().get(0).getValue();
        }
        return observation.getIdElement() != null ? observation.getIdElement().getIdPart() : null;
    }

    public static Date extractIssuedAt(Observation observation) {
        if (observation.hasIssued()) {
            return observation.getIssued();
        }
        
        for (Resource resource : observation.getContained()) {
            if (resource instanceof Specimen specimen) {
                if (specimen.hasCollection() && specimen.getCollection().hasCollectedDateTimeType()) {
                    return specimen.getCollection().getCollectedDateTimeType().getValue();
                }
            }
        }
        return null;
    }

    public static Patient extractPatient(Observation observation) {
        for (Resource resource : observation.getContained()) {
            if (resource instanceof Patient patient) {
                return patient;
            }
        }
        return null;
    }
}
