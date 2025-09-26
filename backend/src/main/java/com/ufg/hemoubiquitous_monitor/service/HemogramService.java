package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HemogramService {
    @Autowired
    private ObservationRepository observationRepository;

    public void receiveHemogram(Observation observation) throws InvalidBloodCountException {
        Boolean isBloodCount = observation.getCode().getCoding().get(0).getCode().equals("55429-5");

        if(!isBloodCount) {
            throw new InvalidBloodCountException();
        }

        this.analyzeAneamiaInBloodCount(observation);
    }
        /**
        * Função de análise da presença de anemia no hemograma.
         * Critério de decisão baseada na base de conhecimento da Organização Mundial de Saúde
        * @link https://iris.who.int/server/api/core/bitstreams/b82789de-df63-41ba-8058-25f9f5da0c40/content
        * @author João Vitor
        * @version 1.0.0
        * */
    private void analyzeAneamiaInBloodCount(Observation bloodCount) {
        Quantity quantity = bloodCount.getComponent().stream().filter(component -> component.getCode().getCoding().stream().anyMatch(coding -> "http://loinc.org".equals(coding.getSystem())
                && "718-7".equals(coding.getCode()))).findFirst().map(Observation.ObservationComponentComponent::getValueQuantity).orElse(null);
        Boolean hasAneamia = false;
        Double haemoglobinInGramsPerLitre = quantity.getValue().doubleValue() * 10;
        Date todaysDate = new Date();

        if(haemoglobinInGramsPerLitre < 129.0)
            hasAneamia = true;

        com.ufg.hemoubiquitous_monitor.model.Observation observation = new com.ufg.hemoubiquitous_monitor.model.Observation("718-7", quantity.getDisplay(), haemoglobinInGramsPerLitre, todaysDate, hasAneamia);
        observationRepository.save(observation);
    }
}
