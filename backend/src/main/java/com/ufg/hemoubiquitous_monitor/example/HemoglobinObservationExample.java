package com.ufg.hemoubiquitous_monitor.example;

public class HemoglobinObservationExample {
        public static final String observationExample = """
                  {
                                 "resourceType": "Observation",
                                 "id": "49497502",
                                 "meta": {
                                     "versionId": "1",
                                     "lastUpdated": "2025-10-03T00:31:30.455+00:00",
                                     "source": "#IUQtSBJp5YdKyZ31",
                                     "profile": [
                                         "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/exame-simples"
                                     ]
                                 },
                                 "contained": [
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
                                             "code": "718-7"
                                         }
                                     ]
                                 },
                                 "subject": {
                                     "identifier": {
                                         "system": "https://fhir.saude.go.gov.br/sid/cpf",
                                         "value": "01234567891"
                                     }
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
                                                 "url": "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/conselho-profissional",
                                                 "extension": [
                                                     {
                                                         "url": "conselhoProfissional",
                                                         "valueCode": "69"
                                                     },
                                                     {
                                                         "url": "regiao",
                                                         "valueCode": "52"
                                                     },
                                                     {
                                                         "url": "inscricao",
                                                         "valueString": "1234"
                                                     }
                                                 ]
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
                                                 "url": "https://fhir.saude.go.gov.br/r4/core/StructureDefinition/conselho-profissional",
                                                 "extension": [
                                                     {
                                                         "url": "conselhoProfissional",
                                                         "valueCode": "69"
                                                     },
                                                     {
                                                         "url": "regiao",
                                                         "valueCode": "52"
                                                     },
                                                     {
                                                         "url": "inscricao",
                                                         "valueString": "1234"
                                                     }
                                                 ]
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
                                     "text": "Automatizado – Cell-Dyn Ruby, Abbott e Microscopia"
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
                                             "system": "http://unitsofmeasure.or-g",
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
