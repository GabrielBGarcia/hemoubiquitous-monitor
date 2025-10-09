package com.ufg.hemoubiquitous_monitor.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestDataLoader {
    
    private static final FhirContext fhirContext = FhirContext.forR4();
    private static final IParser jsonParser = fhirContext.newJsonParser();
    
    public static String loadJsonFromFile(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    public static Observation loadObservationFromFile(String filePath) throws IOException {
        String jsonContent = loadJsonFromFile(filePath);
        return jsonParser.parseResource(Observation.class, jsonContent);
    }
    
    public static Observation loadNormalCompleteHemogram() throws IOException {
        return loadObservationFromFile("test-data/hemograms/normal-complete-hemogram.json");
    }
    
    public static Observation loadAnemicCompleteHemogram() throws IOException {
        return loadObservationFromFile("test-data/hemograms/anemic-complete-hemogram.json");
    }
    
    public static Observation loadNormalHemoglobinOnly() throws IOException {
        return loadObservationFromFile("test-data/hemograms/normal-hemoglobin-only.json");
    }
    
    public static Observation loadAnemicHemoglobinOnly() throws IOException {
        return loadObservationFromFile("test-data/hemograms/anemic-hemoglobin-only.json");
    }
    
    public static String loadInvalidMalformedJson() throws IOException {
        return loadJsonFromFile("test-data/hemograms/invalid-malformed.json");
    }
    
    public static Observation loadInvalidMissingComponents() throws IOException {
        return loadObservationFromFile("test-data/hemograms/invalid-missing-components.json");
    }
    
    public static Observation loadInvalidWrongCodes() throws IOException {
        return loadObservationFromFile("test-data/hemograms/invalid-wrong-codes.json");
    }
}
