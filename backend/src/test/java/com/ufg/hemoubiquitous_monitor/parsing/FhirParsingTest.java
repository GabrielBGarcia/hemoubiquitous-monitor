package com.ufg.hemoubiquitous_monitor.parsing;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import com.ufg.hemoubiquitous_monitor.util.TestDataLoader;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FhirParsingTest {

    private static final Logger logger = LoggerFactory.getLogger(FhirParsingTest.class);
    
    private FhirContext fhirContext;
    private IParser jsonParser;

    @BeforeEach
    void setUp() {
        fhirContext = FhirContext.forR4();
        jsonParser = fhirContext.newJsonParser();
    }

    @Test
    void testParseNormalCompleteHemogram_ShouldParseSuccessfully() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/normal-complete-hemogram.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("normal-cbc-test-001"));
        assertEquals("55429-5", observation.getCode().getCoding().get(0).getCode());
        assertEquals("final", observation.getStatus().toCode());

        Quantity hemoglobinQuantity = observation.getComponent().stream()
            .filter(component -> component.getCode().getCoding().stream()
                .anyMatch(coding -> "718-7".equals(coding.getCode())))
            .findFirst()
            .map(Observation.ObservationComponentComponent::getValueQuantity)
            .orElse(null);

        assertNotNull(hemoglobinQuantity);
        assertEquals(15.2, hemoglobinQuantity.getValue().doubleValue(), 0.01);
        assertEquals("g/dL", hemoglobinQuantity.getCode());

        logger.info("Parsing de hemograma normal completo executado com sucesso");
    }

    @Test
    void testParseAnemicCompleteHemogram_ShouldParseSuccessfully() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/anemic-complete-hemogram.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("anemic-cbc-test-001"));
        assertEquals("55429-5", observation.getCode().getCoding().get(0).getCode());

        Quantity hemoglobinQuantity = observation.getComponent().stream()
            .filter(component -> component.getCode().getCoding().stream()
                .anyMatch(coding -> "718-7".equals(coding.getCode())))
            .findFirst()
            .map(Observation.ObservationComponentComponent::getValueQuantity)
            .orElse(null);

        assertNotNull(hemoglobinQuantity);
        assertEquals(9.8, hemoglobinQuantity.getValue().doubleValue(), 0.01);
        assertTrue(hemoglobinQuantity.getValue().doubleValue() < 12.9, "Valor deve indicar anemia");

        logger.info("Parsing de hemograma anêmico executado com sucesso");
    }

    @Test
    void testParseNormalHemoglobinOnly_ShouldParseSuccessfully() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/normal-hemoglobin-only.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("normal-hgb-test-001"));
        assertEquals("718-7", observation.getCode().getCoding().get(0).getCode());

        Quantity hemoglobinQuantity = observation.getValueQuantity();
        assertNotNull(hemoglobinQuantity);
        assertEquals(14.5, hemoglobinQuantity.getValue().doubleValue(), 0.01);
        assertEquals("g/dL", hemoglobinQuantity.getCode());

        logger.info("Parsing de hemoglobina normal isolada executado com sucesso");
    }

    @Test
    void testParseAnemicHemoglobinOnly_ShouldParseSuccessfully() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/anemic-hemoglobin-only.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("anemic-hgb-test-001"));
        assertEquals("718-7", observation.getCode().getCoding().get(0).getCode());

        Quantity hemoglobinQuantity = observation.getValueQuantity();
        assertNotNull(hemoglobinQuantity);
        assertEquals(8.5, hemoglobinQuantity.getValue().doubleValue(), 0.01);
        assertTrue(hemoglobinQuantity.getValue().doubleValue() < 12.9, "Valor deve indicar anemia");

        logger.info("Parsing de hemoglobina anêmica isolada executado com sucesso");
    }

    @Test
    void testParseMalformedJson_ShouldThrowException() throws IOException {
        String malformedJson = TestDataLoader.loadInvalidMalformedJson();

        DataFormatException exception = assertThrows(
            DataFormatException.class,
            () -> jsonParser.parseResource(Observation.class, malformedJson)
        );

        assertNotNull(exception);
        logger.error("Parsing de JSON malformado lançou exceção corretamente: {}", exception.getMessage());
    }

    @Test
    void testParseInvalidWrongCodes_ShouldParseButWithWrongCode() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/invalid-wrong-codes.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("wrong-codes-test-001"));
        assertEquals("12345-6", observation.getCode().getCoding().get(0).getCode());
        assertNotEquals("55429-5", observation.getCode().getCoding().get(0).getCode());
        assertNotEquals("718-7", observation.getCode().getCoding().get(0).getCode());

        logger.warn("Parsing de códigos incorretos executado - JSON válido mas códigos LOINC incorretos");
    }

    @Test
    void testParseMissingComponents_ShouldParseButMissingHemoglobin() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/invalid-missing-components.json");

        Observation observation = assertDoesNotThrow(() ->
            jsonParser.parseResource(Observation.class, jsonContent)
        );

        assertNotNull(observation);
        assertTrue(observation.getId().contains("missing-components-test-001"));
        assertEquals("55429-5", observation.getCode().getCoding().get(0).getCode());

        boolean hasHemoglobinComponent = observation.getComponent().stream()
            .anyMatch(component -> component.getCode().getCoding().stream()
                .anyMatch(coding -> "718-7".equals(coding.getCode())));

        assertFalse(hasHemoglobinComponent, "Não deve ter componente hemoglobina");

        logger.warn("Parsing de componentes ausentes executado - falta componente hemoglobina");
    }

    @Test
    void testParseValidStructure_ShouldValidateFhirStructure() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/normal-complete-hemogram.json");

        Observation observation = jsonParser.parseResource(Observation.class, jsonContent);

        assertNotNull(observation.getResourceType());
        assertEquals("Observation", observation.getResourceType().name());
        assertNotNull(observation.getStatus());
        assertNotNull(observation.getCode());
        assertNotNull(observation.getSubject());
        assertNotNull(observation.getEffectiveDateTimeType());

        assertFalse(observation.getCategory().isEmpty());
        assertEquals("laboratory", observation.getCategory().get(0).getCoding().get(0).getCode());

        logger.info("Validação de estrutura FHIR executada com sucesso");
    }

    @Test
    void testParseAndValidateUnits_ShouldHaveCorrectUnits() throws IOException {
        String jsonContent = TestDataLoader.loadJsonFromFile("test-data/hemograms/normal-complete-hemogram.json");

        Observation observation = jsonParser.parseResource(Observation.class, jsonContent);

        observation.getComponent().forEach(component -> {
            Quantity quantity = component.getValueQuantity();
            if (quantity != null) {
                assertNotNull(quantity.getUnit());
                assertNotNull(quantity.getCode());
                assertNotNull(quantity.getSystem());
                assertEquals("http://unitsofmeasure.org", quantity.getSystem());
            }
        });

        logger.info("Validação de unidades UCUM executada com sucesso");
    }
}
