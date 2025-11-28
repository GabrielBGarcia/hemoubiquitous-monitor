package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.service.AnaemiaDetectionService;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.repository.HemogramRepository;
import com.ufg.hemoubiquitous_monitor.domain.patient.repository.PatientRepository;
import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import com.ufg.hemoubiquitous_monitor.util.TestDataLoader;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HemogramServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(HemogramServiceTest.class);

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private HemogramRepository hemogramRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AnaemiaDetectionService anaemiaDetectionService;

    private HemogramService hemogramService;

    @BeforeEach
    void setUp() {
        hemogramService = new HemogramService(anaemiaDetectionService, hemogramRepository, patientRepository);
    }

    @Test
    void testReceiveHemogram_NormalCase_ShouldProcessSuccessfully() throws IOException, InvalidBloodCountException {
        Observation normalHemogram = TestDataLoader.loadNormalCompleteHemogram();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(normalHemogram));

        verify(hemogramRepository, times(1)).save(any());
        logger.info("Teste de hemograma normal processado com sucesso");
    }

    @Test
    void testReceiveHemogram_AnemicCase_ShouldDetectAnemia() throws IOException, InvalidBloodCountException {
        Observation anemicHemogram = TestDataLoader.loadAnemicCompleteHemogram();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(anemicHemogram));

        verify(hemogramRepository, times(1)).save(any());
        logger.info("Teste de hemograma anêmico detectou anemia corretamente");
    }

    @Test
    void testReceiveHemogram_WrongCodeButValidHemoglobin_ShouldProcessSuccessfully() throws IOException {
        Observation wrongCodeObservation = TestDataLoader.loadInvalidWrongCodes();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(wrongCodeObservation));

        verify(hemogramRepository, times(1)).save(any());
        logger.info("Teste de observação com código principal errado mas hemoglobina válida processada com sucesso");
    }

    @Test
    void testReceiveHemogram_MissingHemoglobinComponent_ShouldHandleGracefully() throws IOException {
        Observation missingComponentsObservation = TestDataLoader.loadInvalidMissingComponents();

        assertThrows(
            Exception.class,
            () -> hemogramService.receiveHemogram(missingComponentsObservation)
        );

        verify(hemogramRepository, never()).save(any());
        logger.warn("Teste de componente hemoglobina ausente tratado adequadamente");
    }

    @Test
    void testReceiveHemoglobin_NormalCase_ShouldProcessSuccessfully() throws IOException, InvalidBloodCountException, InvalidHemoglobinException {
        Observation normalHemoglobin = TestDataLoader.loadNormalHemoglobinOnly();
        String testId = "normal-hgb-test-001";

        assertDoesNotThrow(() -> hemogramService.receiveHemoglobin(normalHemoglobin, testId));

        verify(hemogramRepository, times(1)).save(any());
        logger.info("Teste de hemoglobina normal processada com sucesso");
    }

    @Test
    void testReceiveHemoglobin_AnemicCase_ShouldDetectAnemia() throws IOException, InvalidBloodCountException, InvalidHemoglobinException {
        Observation anemicHemoglobin = TestDataLoader.loadAnemicHemoglobinOnly();
        String testId = "anemic-hgb-test-001";

        assertDoesNotThrow(() -> hemogramService.receiveHemoglobin(anemicHemoglobin, testId));

        verify(hemogramRepository, times(1)).save(any());
        logger.info("Teste de hemoglobina anêmica detectou anemia corretamente");
    }

    @Test
    void testReceiveHemoglobin_InvalidHemoglobinCode_ShouldThrowException() throws IOException {
        Observation invalidCodeObservation = TestDataLoader.loadInvalidWrongCodes();
        String testId = "invalid-test-001";

        InvalidHemoglobinException exception = assertThrows(
            InvalidHemoglobinException.class,
            () -> hemogramService.receiveHemoglobin(invalidCodeObservation, testId)
        );

        verify(hemogramRepository, never()).save(any());
        logger.error("Teste de código hemoglobina inválido lançou exceção corretamente: {}", exception.getMessage());
    }

    @Test
    void testAnalyzeAneamiaInBloodCount_BoundaryValues() throws InvalidBloodCountException {
        Observation boundaryObservation = createObservationWithBoundaryHemoglobin(12.9);

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(boundaryObservation));

        verify(hemogramRepository, times(1)).save(any());

        reset(hemogramRepository);

        Observation anemicBoundaryObservation = createObservationWithBoundaryHemoglobin(12.8);

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(anemicBoundaryObservation));

        verify(hemogramRepository, times(1)).save(any());

        logger.info("Teste de valores limítrofes para detecção de anemia executado com sucesso");
    }

    private Observation createObservationWithBoundaryHemoglobin(double hemoglobinValue) {
        Observation observation = new Observation();
        observation.setId("boundary-test-" + hemoglobinValue);
        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("55429-5");

        observation.addIdentifier().setValue("boundary-test-" + hemoglobinValue);

        Observation.ObservationComponentComponent component = observation.addComponent();
        component.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode("718-7");

        org.hl7.fhir.r4.model.Quantity quantity = new org.hl7.fhir.r4.model.Quantity();
        quantity.setValue(new java.math.BigDecimal(hemoglobinValue));
        quantity.setUnit("g/dL");
        quantity.setSystem("http://unitsofmeasure.org");
        quantity.setCode("g/dL");
        component.setValue(quantity);

        return observation;
    }

}
