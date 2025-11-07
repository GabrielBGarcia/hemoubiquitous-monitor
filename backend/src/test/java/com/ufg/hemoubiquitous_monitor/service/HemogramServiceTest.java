package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.exception.InvalidBloodCountException;
import com.ufg.hemoubiquitous_monitor.exception.InvalidHemoglobinException;
import com.ufg.hemoubiquitous_monitor.model.AnaemiaAnalysisResult;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import com.ufg.hemoubiquitous_monitor.util.TestDataLoader;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private PatientDataRepository patientDataRepository;

    @InjectMocks
    private HemogramService hemogramService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testReceiveHemogram_NormalCase_ShouldProcessSuccessfully() throws IOException, InvalidBloodCountException {
        Observation normalHemogram = TestDataLoader.loadNormalCompleteHemogram();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(normalHemogram));

        verify(observationRepository, times(1)).save(any(com.ufg.hemoubiquitous_monitor.model.Observation.class));
        logger.info("Teste de hemograma normal processado com sucesso");
    }

    @Test
    void testReceiveHemogram_AnemicCase_ShouldDetectAnemia() throws IOException, InvalidBloodCountException {
        Observation anemicHemogram = TestDataLoader.loadAnemicCompleteHemogram();

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(anemicHemogram));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == true &&
            observation.getHaemoglobinInGramsPerLitre() < 129.0
        ));
        logger.info("Teste de hemograma anêmico detectou anemia corretamente");
    }

    @Test
    void testReceiveHemogram_InvalidBloodCountCode_ShouldThrowException() throws IOException {
        Observation invalidCodeObservation = TestDataLoader.loadInvalidWrongCodes();

        InvalidBloodCountException exception = assertThrows(
            InvalidBloodCountException.class,
            () -> hemogramService.receiveHemogram(invalidCodeObservation)
        );

        verify(observationRepository, never()).save(any());
        logger.error("Teste de código LOINC inválido lançou exceção corretamente: {}", exception.getMessage());
    }

    @Test
    void testReceiveHemogram_MissingHemoglobinComponent_ShouldHandleGracefully() throws IOException {
        Observation missingComponentsObservation = TestDataLoader.loadInvalidMissingComponents();

        assertThrows(
            NullPointerException.class,
            () -> hemogramService.receiveHemogram(missingComponentsObservation)
        );

        verify(observationRepository, never()).save(any());
        logger.warn("Teste de componente hemoglobina ausente tratado adequadamente");
    }

    @Test
    void testReceiveHemoglobin_NormalCase_ShouldProcessSuccessfully() throws IOException, InvalidBloodCountException, InvalidHemoglobinException {
        Observation normalHemoglobin = TestDataLoader.loadNormalHemoglobinOnly();
        String testId = "normal-hgb-test-001";

        assertDoesNotThrow(() -> hemogramService.receiveHemoglobin(normalHemoglobin, testId));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == false &&
            observation.getHaemoglobinInGramsPerLitre() >= 129.0
        ));
        logger.info("Teste de hemoglobina normal processada com sucesso");
    }

    @Test
    void testReceiveHemoglobin_AnemicCase_ShouldDetectAnemia() throws IOException, InvalidBloodCountException, InvalidHemoglobinException {
        Observation anemicHemoglobin = TestDataLoader.loadAnemicHemoglobinOnly();
        String testId = "anemic-hgb-test-001";

        assertDoesNotThrow(() -> hemogramService.receiveHemoglobin(anemicHemoglobin, testId));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == true &&
            observation.getHaemoglobinInGramsPerLitre() < 129.0
        ));
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

        verify(observationRepository, never()).save(any());
        logger.error("Teste de código hemoglobina inválido lançou exceção corretamente: {}", exception.getMessage());
    }

    @Test
    void testAnalyzeAneamiaInBloodCount_BoundaryValues() throws InvalidBloodCountException {
        // Teste com valor exatamente no limite (12.9 g/dL = 129 g/L)
        Observation boundaryObservation = createObservationWithBoundaryHemoglobin(12.9);

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(boundaryObservation));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == false &&
            observation.getHaemoglobinInGramsPerLitre() == 129.0
        ));

        // Reset mock para próximo teste
        reset(observationRepository);

        // Teste com valor ligeiramente abaixo do limite (12.8 g/dL = 128 g/L)
        Observation anemicBoundaryObservation = createObservationWithBoundaryHemoglobin(12.8);

        assertDoesNotThrow(() -> hemogramService.receiveHemogram(anemicBoundaryObservation));

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getHasAnaemia() == true &&
            observation.getHaemoglobinInGramsPerLitre() == 128.0
        ));

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

    @Test
    void testSaveAnalysisResult_ShouldSaveCorrectData() {
        AnaemiaAnalysisResult result = new AnaemiaAnalysisResult(true, 85.0, new java.util.Date());
        String origin = "TEST_ORIGIN";
        String id = "test-id-001";

        // Monta Observation FHIR com o identifier esperado
        Observation fhirObservation = new Observation();
        fhirObservation.addIdentifier().setValue(id);

        hemogramService.saveAnalysisResult(result, origin, fhirObservation);

        verify(observationRepository, times(1)).save(argThat(observation ->
            observation.getIdentifier().equals(id) &&
            observation.getHasAnaemia() == true &&
            observation.getHaemoglobinInGramsPerLitre().equals(85.0)
        ));
        logger.info("Teste de salvamento de resultado de análise executado com sucesso");
    }
}
