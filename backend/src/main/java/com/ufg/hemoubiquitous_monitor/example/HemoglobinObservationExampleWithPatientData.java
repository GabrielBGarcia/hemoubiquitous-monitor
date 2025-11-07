package com.ufg.hemoubiquitous_monitor.example;

public class HemoglobinObservationExampleWithPatientData {
        public static final String observationExample = """
                 {
  "resourceType": "Observation",
  "id": "hemoglobina",
  "meta": {
    "profile": [
      "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/exame-simples"
    ]
  },
  "contained": [
    {
      "resourceType": "Patient",
      "id": "paciente",
      "meta": {
        "profile": [
          "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/br-patient"
        ]
      },
      "identifier": [
        {
          "system": "https://fhir.saude.go.gov.br/sid/cpf",
          "value": "01234567891"
        }
      ],
      "name": [
        {
          "use": "official",
          "text": "João da Silva",
          "family": "Silva",
          "given": ["João"]
        }
      ],
      "gender": "male",
      "birthDate": "1985-03-12",
      "address": [
        {
          "use": "home",
          "type": "both",
          "line": ["Rua das Flores, 123"],
          "district": "Setor Central",
          "city": "Goiânia",
          "state": "GO",
          "postalCode": "74000-000",
          "country": "BR",
          "extension": [
            {
              "url": "http://hl7.org/fhir/StructureDefinition/geolocation",
              "extension": [
                {
                  "url": "latitude",
                  "valueDecimal": -16.6869
                },
                {
                  "url": "longitude",
                  "valueDecimal": -49.2648
                }
              ]
            }
          ]
        }
      ],
      "telecom": [
        {
          "system": "phone",
          "value": "+5562999999999",
          "use": "mobile"
        }
      ]
    },
    {
      "resourceType": "Specimen",
      "id": "amostra",
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/v2-0487",
            "code": "BLD"
          }
        ]
      },
      "collection": {
        "collectedDateTime": "2024-07-24T10:00:00-03:00"
      }
    }
  ],
  "status": "final",
  "category": [
    {
      "coding": [
        {
          "system": "http://www.saude.gov.br/fhir/r4/CodeSystem/BRSubgrupoTabelaSUS",
          "code": "0202"
        }
      ]
    }
  ],
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "718-7",
        "display": "Hemoglobina [Mass/volume] no sangue"
      }
    ]
  },
  "subject": {
    "reference": "#paciente"
  },
  "issued": "2024-08-02T11:41:00-03:00",
  "performer": [
    {
      "id": "laboratorio",
      "identifier": {
        "system": "https://fhir.saude.go.gov.br/sid/cnes",
        "value": "2337991"
      }
    },
    {
      "id": "responsavelTecnico",
      "extension": [
        {
          "extension": [
            { "url": "conselhoProfissional", "valueCode": "69" },
            { "url": "regiao", "valueCode": "52" },
            { "url": "inscricao", "valueString": "1234" }
          ],
          "url": "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/conselho-profissional"
        }
      ],
      "identifier": {
        "system": "https://fhir.saude.go.gov.br/sid/cpf",
        "value": "12345678900"
      }
    },
    {
      "id": "responsavelResultado",
      "extension": [
        {
          "extension": [
            { "url": "conselhoProfissional", "valueCode": "69" },
            { "url": "regiao", "valueCode": "52" },
            { "url": "inscricao", "valueString": "1234" }
          ],
          "url": "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/conselho-profissional"
        }
      ],
      "identifier": {
        "system": "https://fhir.saude.go.gov.br/sid/cpf",
        "value": "00011111100"
      }
    }
  ],
  "valueQuantity": {
    "value": 16.9,
    "system": "http://unitsofmeasure.org",
    "code": "g/dL"
  },
  "method": {
    "text": "Automatizado - Cell-Dyn Ruby, Abbott e Microscopia"
  },
  "specimen": {
    "reference": "#amostra"
  },
  "referenceRange": [
    {
      "low": {
        "value": 13.5,
        "system": "http://unitsofmeasure.org",
        "code": "g/dL"
      },
      "high": {
        "value": 17.5,
        "system": "http://unitsofmeasure.org",
        "code": "g/dL"
      },
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/referencerange-meaning",
            "code": "normal"
          }
        ]
      }
    }
  ]
}

            """;
    }
