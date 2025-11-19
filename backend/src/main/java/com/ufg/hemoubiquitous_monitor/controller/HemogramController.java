package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.example.BloodCountObservationExample;
import com.ufg.hemoubiquitous_monitor.example.HemoglobinObservationExample;
import com.ufg.hemoubiquitous_monitor.example.HemoglobinObservationExampleWithPatientData;
import com.ufg.hemoubiquitous_monitor.service.HemogramService;
import com.ufg.hemoubiquitous_monitor.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.uhn.fhir.context.FhirContext;

@RestController
@RequestMapping("/fhir/receptor")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class HemogramController {
    private final FhirContext fhirContext = FhirContext.forR4();

    @Autowired
    private HemogramService hemogramService;

    @Autowired
    private NotificationService notificationService;


    @PostMapping(value = "/hemograma", consumes = "application/fhir+json")
    @Operation(description = "Recebe e persiste hemogramas")
    public void receiveHemogram(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(
                    name = "hemogramExample",
                    summary = "Exemplo de Observation de Hemograma",
                    description = "Payload JSON que será recebido via REST Hook",
                    value = BloodCountObservationExample.observationExample
            ))) @org.springframework.web.bind.annotation.RequestBody String hemogramJson) {
        try {
            System.out.println(hemogramJson);
            Observation observation = (Observation) fhirContext.newJsonParser()
                    .parseResource(Observation.class, hemogramJson);

            this.hemogramService.receiveHemogram(observation);

        } catch (Exception e) {
            System.err.println("Erro ao processar hemograma: " + e.getMessage());
        }
    }
    @PutMapping(value = "/hemoglobin/Observation/{id}", consumes = "application/fhir+json")
    @Operation(description = "Recebe e persiste exames de hemoglobina")

    public void receiveHemoglobin(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(
                    name = "hemoglobin",
                    summary = "Exemplo de Observation de Hemoglobina com Dados do Paciente",
                    description = "Payload JSON que será recebido via REST Hook",
                    value = HemoglobinObservationExampleWithPatientData.observationExample
            ))) @org.springframework.web.bind.annotation.RequestBody String hemoglobinJson,
                                  @PathVariable("id") String id) {
        try {
            System.out.println(hemoglobinJson);
            Observation observation = (Observation) fhirContext.newJsonParser()
                    .parseResource(Observation.class, hemoglobinJson);

            this.hemogramService.receiveHemoglobin(observation, id);

        } catch (Exception e) {
            System.err.println("Erro ao processar hemograma: " + e.getMessage());
        }
    }

    @GetMapping(value = "/hemoglobin/metadata")
    public ResponseEntity<String> metadata() {
//        this.notificationService.notifyEpidemicSuspect("GO", "GOIANIA");
        String capabilityStatement = """
        {
          "resourceType": "CapabilityStatement",
          "status": "active",
          "date": "2024-01-01",
          "kind": "instance",
          "software": {
            "name": "REST Hook Receiver"
          },
          "implementation": {
            "url": "http://localhost:8080/fhir"
          },
          "fhirVersion": "4.0.1",
          "format": ["application/fhir+json"],
          "rest": [
            {
              "mode": "server",
              "resource": [
                {
                  "type": "Observation",
                  "interaction": [
                    {
                      "code": "create"
                    }
                  ]
                }
              ]
            }
          ]
        }
        """;

        return ResponseEntity.ok()
                .header("Content-Type", "application/fhir+json")
                .body(capabilityStatement);
    }
}
