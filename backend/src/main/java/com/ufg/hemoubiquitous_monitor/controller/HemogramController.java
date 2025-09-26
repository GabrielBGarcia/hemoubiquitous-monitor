package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.example.ObservationExample;
import com.ufg.hemoubiquitous_monitor.service.HemogramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.uhn.fhir.context.FhirContext;

@RestController
@RequestMapping("/fhir/receptor")
public class HemogramController {
    private final FhirContext fhirContext = FhirContext.forR4();
    @Autowired
    private HemogramService hemogramService;

    @PostMapping(value = "/hemograma", consumes = "application/fhir+json")
    @Operation(description = "Recebe e persiste hemogramas")
    public void receiveHemogram(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(
                    name = "hemogramExample",
                    summary = "Exemplo de Observation de Hemograma",
                    description = "Payload JSON que será recebido via REST Hook",
                    value = ObservationExample.observationExample
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

    @GetMapping(value = "/hemograma/metadata")
    @PostMapping("/hemograma/metadata")
    public ResponseEntity<String> metadata() {
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
