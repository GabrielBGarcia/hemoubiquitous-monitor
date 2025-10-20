package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.model.AnaemiaAnalysisResult;
import com.ufg.hemoubiquitous_monitor.model.PatientData;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class HemogramService {
    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private PatientDataRepository patientDataRepository;

    public void receiveHemogram(Observation observation) throws InvalidBloodCountException {
        Boolean isBloodCount = observation.getCode().getCoding().get(0).getCode().equals("55429-5");

        if(!isBloodCount) {
            throw new InvalidBloodCountException();
        }

        Quantity quantity = observation.getComponent().stream().filter(component -> component.getCode().getCoding().stream().anyMatch(coding -> "http://loinc.org".equals(coding.getSystem())
                && "718-7".equals(coding.getCode()))).findFirst().map(Observation.ObservationComponentComponent::getValueQuantity).orElse(null);

        // Validar valor da hemoglobina
        if (quantity != null && quantity.getValue() != null) {
            double hemoglobinValue = quantity.getValue().doubleValue();
            if (hemoglobinValue < 0) {
                throw new RuntimeException("Valor da hemoglobina não pode ser negativo: " + hemoglobinValue);
            }
        }

        AnaemiaAnalysisResult anaemiaAnalyzeResult = this.analyzeAneamiaInBloodCount(quantity);

        this.saveAnalysisResult(anaemiaAnalyzeResult, "BLOODCOUNT", observation);
    }

    public void receiveHemoglobin(Observation observation, String id) throws InvalidBloodCountException, InvalidHemoglobinException {
        Boolean isHemoglobin = observation.getCode().getCoding().get(0).getCode().equals("718-7");

        if(!isHemoglobin) {
            throw new InvalidHemoglobinException();
        }

        Quantity quantity = observation.getValueQuantity();

        AnaemiaAnalysisResult anaemiaAnalyzeResult = this.analyzeAneamiaInBloodCount(quantity);

        // Ajusta o identifier se for passado na rota
        if (observation.getIdentifier() == null || observation.getIdentifier().isEmpty()) {
            observation.addIdentifier().setSystem("http://example.org/integration-id").setValue(id);
        }
        this.saveAnalysisResult(anaemiaAnalyzeResult, "HEMOGLOBIN", observation);
    }

    public void saveAnalysisResult(AnaemiaAnalysisResult anaemiaAnalyzeResult, String origin, Observation fhirObservation){
        Date analyzedAt = anaemiaAnalyzeResult.getAnalyzedAt();
        Double haemoglobinInGramsPerLitre = anaemiaAnalyzeResult.getHaemoglobinInGramsPerLitre();
        Boolean hasAnaemia = anaemiaAnalyzeResult.getHasAnaemia();

        String identifier = extractFirstIdentifier(fhirObservation);
        Date issuedAt = extractIssuedAt(fhirObservation);
        PatientData patientData = upsertPatientFromObservation(fhirObservation);

        com.ufg.hemoubiquitous_monitor.model.Observation observationSaveable = new com.ufg.hemoubiquitous_monitor.model.Observation(identifier, "718-7", origin, haemoglobinInGramsPerLitre, analyzedAt, hasAnaemia);
        observationSaveable.setPatientData(patientData);
        observationSaveable.setIssuedAt(issuedAt);
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

    private String extractFirstIdentifier(Observation observation) {
        if (observation.hasIdentifier() && !observation.getIdentifier().isEmpty()) {
            return observation.getIdentifier().get(0).getValue();
        }
        return observation.getIdElement() != null ? observation.getIdElement().getIdPart() : null;
    }

    private Date extractIssuedAt(Observation observation) {
        if (observation.hasIssued()) {
            return observation.getIssued();
        }
        // fallback: Specimen.collection.collectedDateTime
        for (Resource resource : observation.getContained()) {
            if (resource instanceof Specimen specimen) {
                if (specimen.hasCollection() && specimen.getCollection().hasCollectedDateTimeType()) {
                    return specimen.getCollection().getCollectedDateTimeType().getValue();
                }
            }
        }
        return null;
    }

    private PatientData upsertPatientFromObservation(Observation observation) {
        Patient patient = null;
        for (Resource resource : observation.getContained()) {
            if (resource instanceof Patient p) {
                patient = p;
                break;
            }
        }
        if (patient == null) return null;

        String cpf = null;
        if (patient.hasIdentifier()) {
            for (Identifier id : patient.getIdentifier()) {
                if (id.hasSystem() && id.getSystem().contains("/sid/cpf") && id.hasValue()) {
                    cpf = id.getValue();
                    break;
                }
            }
        }

        Optional<PatientData> existing = (cpf != null) ? patientDataRepository.findByCpf(cpf) : Optional.empty();
        PatientData pd = existing.orElseGet(PatientData::new);

        // Dados pessoais
        pd.setCpf(cpf);
        pd.setName(patient.hasName() && !patient.getName().isEmpty() ? patient.getName().get(0).getText() : null);
        pd.setGender(patient.hasGender() ? patient.getGender().toCode() : null);
        if (patient.hasBirthDate()) {
            pd.setBirthDate(patient.getBirthDate());
        }
        if (patient.hasTelecom()) {
            for (ContactPoint cp : patient.getTelecom()) {
                if (cp.getSystem() == ContactPoint.ContactPointSystem.PHONE && cp.hasValue()) {
                    pd.setPhone(cp.getValue());
                    break;
                }
            }
        }

        // Endereço + geolocalização
        if (patient.hasAddress() && !patient.getAddress().isEmpty()) {
            Address addr = patient.getAddress().get(0);
            if (addr.hasLine()) {
                pd.setAddressLine(String.join(", ", addr.getLine().stream().map(org.hl7.fhir.r4.model.StringType::getValue).toList()));
            }
            pd.setDistrict(addr.hasDistrict() ? addr.getDistrict() : null);
            pd.setCity(addr.hasCity() ? addr.getCity() : null);
            pd.setState(addr.hasState() ? addr.getState() : null);
            pd.setPostalCode(addr.hasPostalCode() ? addr.getPostalCode() : null);
            pd.setCountry(addr.hasCountry() ? addr.getCountry() : null);

            // extensão geolocation
            if (addr.hasExtension()) {
                addr.getExtension().stream()
                        .filter(ext -> "http://hl7.org/fhir/StructureDefinition/geolocation".equals(ext.getUrl()))
                        .findFirst()
                        .ifPresent(geo -> {
                            Double lat = null;
                            Double lon = null;
                            for (Extension e : geo.getExtension()) {
                                if ("latitude".equals(e.getUrl()) && e.getValue() instanceof DecimalType dt) {
                                    lat = dt.getValue().doubleValue();
                                }
                                if ("longitude".equals(e.getUrl()) && e.getValue() instanceof DecimalType dt) {
                                    lon = dt.getValue().doubleValue();
                                }
                            }
                            pd.setLatitude(lat);
                            pd.setLongitude(lon);
                        });
            }
        }

        return patientDataRepository.save(pd);
    }
}
