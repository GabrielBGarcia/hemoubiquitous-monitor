package com.ufg.hemoubiquitous_monitor.validation;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import com.ufg.hemoubiquitous_monitor.service.HemogramService;
import com.ufg.hemoubiquitous_monitor.util.TestDataLoader;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(ValidationTest.class);

    @Mock
    private ObservationRepository observationRepository;

    @InjectMocks
    private HemogramService hemogramService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testValidateBloodCountCode_ValidCode_ShouldPass() throws IOException, InvalidBloodCountException {
        Observation validObservation = TestDataLoader.loadNormalCompleteHemogram();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(validObservation));
        verify(observationRepository, times(1)).save(any());
        logger.info("Validação de código LOINC válido para hemograma passou");
    }

    @Test
    void testValidateBloodCountCode_InvalidCode_ShouldThrowException() throws IOException {
        Observation invalidObservation = TestDataLoader.loadInvalidWrongCodes();

        InvalidBloodCountException exception = assertThrows(
            InvalidBloodCountException.class,
            () -> hemogramService.receiveHemogram(invalidObservation)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de código LOINC inválido para hemograma lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateHemoglobinCode_ValidCode_ShouldPass() throws IOException, InvalidBloodCountException, InvalidHemoglobinException {
        Observation validObservation = TestDataLoader.loadNormalHemoglobinOnly();
        String testId = "validation-test-001";

        assertDoesNotThrow(() -> hemogramService.receiveHemoglobin(validObservation, testId));
        verify(observationRepository, times(1)).save(any());
        logger.info("Validação de código LOINC válido para hemoglobina passou");
    }

    @Test
    void testValidateHemoglobinCode_InvalidCode_ShouldThrowException() throws IOException {
        Observation invalidObservation = TestDataLoader.loadInvalidWrongCodes();
        String testId = "validation-test-002";

        InvalidHemoglobinException exception = assertThrows(
            InvalidHemoglobinException.class,
            () -> hemogramService.receiveHemoglobin(invalidObservation, testId)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de código LOINC inválido para hemoglobina lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateHemoglobinComponent_MissingComponent_ShouldThrowException() throws IOException {
        Observation missingComponentObservation = TestDataLoader.loadInvalidMissingComponents();

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> hemogramService.receiveHemogram(missingComponentObservation)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de componente hemoglobina ausente lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateHemoglobinValue_NullValue_ShouldThrowException() {
        Observation observationWithNullValue = createObservationWithNullHemoglobin();

        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> hemogramService.receiveHemogram(observationWithNullValue)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de valor hemoglobina nulo lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateHemoglobinValue_NegativeValue_ShouldThrowException() {
        Observation observationWithNegativeValue = createObservationWithNegativeHemoglobin();

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> hemogramService.receiveHemogram(observationWithNegativeValue)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de valor hemoglobina negativo lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateHemoglobinValue_ExtremelyHighValue_ShouldLogWarning() throws InvalidBloodCountException {
        Observation observationWithHighValue = createObservationWithHighHemoglobin();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(observationWithHighValue));
        verify(observationRepository, times(1)).save(any());
        logger.warn("Valor de hemoglobina extremamente alto processado: pode indicar erro de medição");
    }

    @Test
    void testValidateIdentifier_MissingIdentifier_ShouldHandleGracefully() throws IOException {
        Observation observationWithoutIdentifier = TestDataLoader.loadNormalCompleteHemogram();
        observationWithoutIdentifier.getIdentifier().clear();

        IndexOutOfBoundsException exception = assertThrows(
            IndexOutOfBoundsException.class,
            () -> hemogramService.receiveHemogram(observationWithoutIdentifier)
        );

        assertNotNull(exception);
        verify(observationRepository, never()).save(any());
        logger.error("Validação de identificador ausente lançou exceção: {}", exception.getClass().getSimpleName());
    }

    @Test
    void testValidateAnemiaThreshold_BoundaryValues() throws InvalidBloodCountException {
        Observation boundaryObservation = createObservationWithBoundaryHemoglobin();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(boundaryObservation));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == true
        ));
        logger.info("Validação de valor limítrofe para anemia executada");
    }

    private Observation createObservationWithNullHemoglobin() {
        Observation observation = new Observation();
        observation.setId("null-value-test");
        observation.setStatus(Observation.ObservationStatus.FINAL);
        
        // Código correto para hemograma
        observation.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("55429-5");
        
        observation.addIdentifier().setValue("null-test-001");
        
        // Componente hemoglobina com valor nulo
        Observation.ObservationComponentComponent component = observation.addComponent();
        component.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("718-7");        
        return observation;
    }

    private Observation createObservationWithNegativeHemoglobin() {
        Observation observation = new Observation();
        observation.setId("negative-value-test");
        observation.setStatus(Observation.ObservationStatus.FINAL);
        
        observation.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("55429-5");
        
        observation.addIdentifier().setValue("negative-test-001");
        
        // Componente hemoglobina com valor negativo
        Observation.ObservationComponentComponent component = observation.addComponent();
        component.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("718-7");
        
        Quantity quantity = new Quantity();
        quantity.setValue(new BigDecimal("-5.0"));
        quantity.setUnit("g/dL");
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("g/dL");
        component.setValue(quantity);
        
        return observation;
    }

    private Observation createObservationWithHighHemoglobin() {
        Observation observation = new Observation();
        observation.setId("high-value-test");
        observation.setStatus(Observation.ObservationStatus.FINAL);
        
        observation.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("55429-5");
        
        observation.addIdentifier().setValue("high-test-001");
        
        // Componente hemoglobina com valor extremamente alto
        Observation.ObservationComponentComponent component = observation.addComponent();
        component.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("718-7");
        
        Quantity quantity = new Quantity();
        quantity.setValue(new BigDecimal("25.0")); // Valor anormalmente alto
        quantity.setUnit("g/dL");
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("g/dL");
        component.setValue(quantity);
        
        return observation;
    }

    private Observation createObservationWithBoundaryHemoglobin() {
        Observation observation = new Observation();
        observation.setId("boundary-value-test");
        observation.setStatus(Observation.ObservationStatus.FINAL);
        
        observation.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("55429-5");
        
        observation.addIdentifier().setValue("boundary-test-001");
        
        // Componente hemoglobina com valor no limite
        Observation.ObservationComponentComponent component = observation.addComponent();
        component.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("718-7");
        
        Quantity quantity = new Quantity();
        quantity.setValue(new BigDecimal("12.8")); // Ligeiramente abaixo do limite para ser anêmico
        quantity.setUnit("g/dL");
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("g/dL");
        component.setValue(quantity);
        
        return observation;
    }
}
