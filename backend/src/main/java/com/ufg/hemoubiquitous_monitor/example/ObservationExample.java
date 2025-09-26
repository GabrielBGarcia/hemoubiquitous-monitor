package com.ufg.hemoubiquitous_monitor.example;

public class ObservationExample {
        public static final String observationExample = """
                {
                   "resourceType": "Observation",
                   "id": "cbc-55429-5-example",
                   "status": "final",
                   "category": [
                     {
                       "coding": [
                         {
                           "system": "http://terminology.hl7.org/CodeSystem/observation-category",
                           "code": "laboratory",
                           "display": "Laboratory"
                         }
                       ]
                     }
                   ],
                   "code": {
                     "coding": [
                       {
                         "system": "http://loinc.org",
                         "code": "55429-5",
                         "display": "Short blood count panel - Blood"
                       }
                     ],
                     "text": "Hemograma (painel reduzido)"
                   },
                   "subject": {
                     "reference": "Patient/example",
                     "display": "Paciente Exemplo"
                   },
                   "effectiveDateTime": "2025-09-26T10:30:00-03:00",
                   "component": [
                     {
                       "code": {
                         "coding": [
                           {
                             "system": "http://loinc.org",
                             "code": "26464-8",
                             "display": "Leukocytes [#/volume] in Blood"
                           }
                         ],
                         "text": "Leucócitos"
                       },
                       "valueQuantity": {
                         "value": 6.2,
                         "unit": "10*3/uL",
                         "system": "http://unitsofmeasure.org",
                         "code": "10*3/uL"
                       },
                       "referenceRange": [
                         {
                           "low": { "value": 4.0, "unit": "10*3/uL", "system": "http://unitsofmeasure.org", "code": "10*3/uL" },
                           "high": { "value": 11.0, "unit": "10*3/uL", "system": "http://unitsofmeasure.org", "code": "10*3/uL" }
                         }
                       ]
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "26453-1", "display": "Erythrocytes [#/volume] in Blood" }
                         ],
                         "text": "Eritrócitos (RBC)"
                       },
                       "valueQuantity": {
                         "value": 4.56,
                         "unit": "10*6/uL",
                         "system": "http://unitsofmeasure.org",
                         "code": "10*6/uL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "718-7", "display": "Hemoglobin [Mass/volume] in Blood" }
                         ],
                         "text": "Hemoglobina (Hgb)"
                       },
                       "valueQuantity": {
                         "value": 14.2,
                         "unit": "g/dL",
                         "system": "http://unitsofmeasure.org",
                         "code": "g/dL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "20570-8", "display": "Hematocrit [Volume Fraction] of Blood by calculation" }
                         ],
                         "text": "Hematócrito (Hct)"
                       },
                       "valueQuantity": {
                         "value": 42.1,
                         "unit": "%",
                         "system": "http://unitsofmeasure.org",
                         "code": "%"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "30428-7", "display": "MCV [Entitic mean volume] in Red Blood Cells" }
                         ],
                         "text": "MCV"
                       },
                       "valueQuantity": {
                         "value": 92.4,
                         "unit": "fL",
                         "system": "http://unitsofmeasure.org",
                         "code": "fL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "28539-5", "display": "MCH [Entitic mass]" }
                         ],
                         "text": "MCH"
                       },
                       "valueQuantity": {
                         "value": 31.2,
                         "unit": "pg",
                         "system": "http://unitsofmeasure.org",
                         "code": "pg"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "28540-3", "display": "MCHC [Entitic Mass/volume] in Red Blood Cells" }
                         ],
                         "text": "MCHC"
                       },
                       "valueQuantity": {
                         "value": 34.3,
                         "unit": "g/dL",
                         "system": "http://unitsofmeasure.org",
                         "code": "g/dL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "26515-7", "display": "Platelets [#/volume] in Blood" }
                         ],
                         "text": "Plaquetas"
                       },
                       "valueQuantity": {
                         "value": 250,
                         "unit": "10*3/uL",
                         "system": "http://unitsofmeasure.org",
                         "code": "10*3/uL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "26499-4", "display": "Neutrophils [#/volume] in Blood" }
                         ],
                         "text": "Neutrófilos (absoluto)"
                       },
                       "valueQuantity": {
                         "value": 3.72,
                         "unit": "10*3/uL",
                         "system": "http://unitsofmeasure.org",
                         "code": "10*3/uL"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "26511-6", "display": "Neutrophils/Leukocytes in Blood" }
                         ],
                         "text": "Neutrófilos (%)"
                       },
                       "valueQuantity": {
                         "value": 60,
                         "unit": "%",
                         "system": "http://unitsofmeasure.org",
                         "code": "%"
                       }
                     },
                     {
                       "code": {
                         "coding": [
                           { "system": "http://loinc.org", "code": "26478-8", "display": "Lymphocytes/Leukocytes in Blood" }
                         ],
                         "text": "Linfócitos (%)"
                       },
                       "valueQuantity": {
                         "value": 30,
                         "unit": "%",
                         "system": "http://unitsofmeasure.org",
                         "code": "%"
                       }
                     }
                   ]
                 }
            """;
    }
