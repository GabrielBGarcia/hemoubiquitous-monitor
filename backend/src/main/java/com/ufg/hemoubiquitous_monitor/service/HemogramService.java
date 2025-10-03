package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.model.AnaemiaAnalysisResult;
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

        Quantity quantity = observation.getComponent().stream().filter(component -> component.getCode().getCoding().stream().anyMatch(coding -> "http://loinc.org".equals(coding.getSystem())
                && "718-7".equals(coding.getCode()))).findFirst().map(Observation.ObservationComponentComponent::getValueQuantity).orElse(null);

        AnaemiaAnalysisResult anaemiaAnalyzeResult = this.analyzeAneamiaInBloodCount(quantity);

        this.saveAnalysisResult(anaemiaAnalyzeResult, "BLOODCOUNT", observation.getIdentifier().get(0).getValue());
    }

    public void receiveHemoglobin(Observation observation, String id) throws InvalidBloodCountException, InvalidHemoglobinException {
        Boolean isHemoglobin = observation.getCode().getCoding().get(0).getCode().equals("718-7");

        if(!isHemoglobin) {
            throw new InvalidHemoglobinException();
        }

        Quantity quantity = observation.getValueQuantity();

        AnaemiaAnalysisResult anaemiaAnalyzeResult = this.analyzeAneamiaInBloodCount(quantity);

        this.saveAnalysisResult(anaemiaAnalyzeResult, "HEMOGLOBIN", id);
    }

    public void saveAnalysisResult(AnaemiaAnalysisResult anaemiaAnalyzeResult, String origin, String id){
        Date analyzedAt = anaemiaAnalyzeResult.getAnalyzedAt();
        Double haemoglobinInGramsPerLitre = anaemiaAnalyzeResult.getHaemoglobinInGramsPerLitre();
        Boolean hasAnaemia = anaemiaAnalyzeResult.getHasAnaemia();

        com.ufg.hemoubiquitous_monitor.model.Observation observationSaveable = new com.ufg.hemoubiquitous_monitor.model.Observation(id, "718-7", origin, haemoglobinInGramsPerLitre, analyzedAt, hasAnaemia);
        observationRepository.save(observationSaveable);
    }

        /**
        * Função de análise da presença de anemia no hemograma.\
         * Critério de decisão baseada na base de conhecimento da Organização Mundial de Saúde
        * @link https://iris.who.int/server/api/core/bitstreams/b82789de-df63-41ba-8058-25f9f5da0c40/content
        * @author João Vitor
        * @version 1.0.0
        * */
    private AnaemiaAnalysisResult analyzeAneamiaInBloodCount(Quantity quantity) {
        Boolean hasAneamia = false;
        Double haemoglobinInGramsPerLitre = quantity.getValue().doubleValue() * 10;
        Date todaysDate = new Date();

        if(haemoglobinInGramsPerLitre < 129.0)
            hasAneamia = true;

        AnaemiaAnalysisResult result = new AnaemiaAnalysisResult(hasAneamia, haemoglobinInGramsPerLitre, todaysDate);

        return result;
    }
}
