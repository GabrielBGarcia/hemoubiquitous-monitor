package com.ufg.hemoubiquitous_monitor.receiver;

import org.hl7.fhir.r4.model.Observation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.uhn.fhir.context.FhirContext;

@RestController
@RequestMapping("/fhir/receptor")
public class HemogramReceiver {
    private final FhirContext fhirContext = FhirContext.forR4();

    @PostMapping(value = "/hemograma", consumes = "application/fhir+json")
    public void receiveHemogram(@RequestBody String hemogramJson) {
        try {
            Observation observation = (Observation) fhirContext.newJsonParser()
                    .parseResource(Observation.class, hemogramJson);

            System.out.println("Novo Hemograma Recebido:");
            System.out.println("ID: " + observation.getId());
            System.out.println("Código: " + observation.getCode().getText());
            System.out.println("Valor: " + observation.getValue().toString());

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
