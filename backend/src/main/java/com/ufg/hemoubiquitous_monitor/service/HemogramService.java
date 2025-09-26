package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Service;

@Service
public class HemogramService {

    public void receiveHemogram(Observation observation) throws InvalidBloodCountException {
        Boolean isBloodCount = observation.getCode().getCoding().get(0).getCode().equals("55429-5");

        if(!isBloodCount) {
            throw new InvalidBloodCountException();
        }

        this.analyzeAneamiaInBloodCount(observation);
    }

    private void analyzeAneamiaInBloodCount(Observation bloodCount) {
        Quantity quantity = bloodCount.getComponent().stream().filter(component -> component.getCode().getCoding().stream().anyMatch(coding -> "http://loinc.org".equals(coding.getSystem())
                && "718-7".equals(coding.getCode()))).findFirst().map(Observation.ObservationComponentComponent::getValueQuantity).orElse(null);

        Double haemoglobinInGramsPerLitre = quantity.getValue().doubleValue() * 10;

        System.out.println(haemoglobinInGramsPerLitre);

        if(haemoglobinInGramsPerLitre < 129.0) {
            System.out.println("O paciente tem suspeita anemia");
        } else {
            System.out.println("O paciente não tem anemia");
        }
    }
}
