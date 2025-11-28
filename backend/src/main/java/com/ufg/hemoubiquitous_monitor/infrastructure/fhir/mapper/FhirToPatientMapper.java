package com.ufg.hemoubiquitous_monitor.infrastructure.fhir.mapper;

import com.ufg.hemoubiquitous_monitor.domain.patient.model.Address;
import com.ufg.hemoubiquitous_monitor.domain.patient.model.Coordinates;
import com.ufg.hemoubiquitous_monitor.domain.patient.model.Patient;
import org.hl7.fhir.r4.model.*;

public class FhirToPatientMapper {

    public static Patient toDomain(org.hl7.fhir.r4.model.Patient fhirPatient) {
        if (fhirPatient == null) {
            return new Patient(null, null, null, null, null, null);
        }

        String cpf = extractCpf(fhirPatient);
        String name = extractName(fhirPatient);
        String gender = fhirPatient.hasGender() ? fhirPatient.getGender().toCode() : null;
        java.util.Date birthDate = fhirPatient.hasBirthDate() ? fhirPatient.getBirthDate() : null;
        String phone = extractPhone(fhirPatient);
        Address address = extractAddress(fhirPatient);

        return new Patient(cpf, name, gender, birthDate, phone, address);
    }

    private static String extractCpf(org.hl7.fhir.r4.model.Patient patient) {
        if (patient.hasIdentifier()) {
            for (Identifier id : patient.getIdentifier()) {
                if (id.hasSystem() && id.getSystem().contains("/sid/cpf") && id.hasValue()) {
                    return id.getValue();
                }
            }
        }
        return null;
    }

    private static String extractName(org.hl7.fhir.r4.model.Patient patient) {
        return patient.hasName() && !patient.getName().isEmpty() 
                ? patient.getName().get(0).getText() 
                : null;
    }

    private static String extractPhone(org.hl7.fhir.r4.model.Patient patient) {
        if (patient.hasTelecom()) {
            for (ContactPoint cp : patient.getTelecom()) {
                if (cp.getSystem() == ContactPoint.ContactPointSystem.PHONE && cp.hasValue()) {
                    return cp.getValue();
                }
            }
        }
        return null;
    }

    private static Address extractAddress(org.hl7.fhir.r4.model.Patient patient) {
        if (!patient.hasAddress() || patient.getAddress().isEmpty()) {
            return null;
        }

        org.hl7.fhir.r4.model.Address fhirAddress = patient.getAddress().get(0);
        
        String addressLine = null;
        if (fhirAddress.hasLine()) {
            addressLine = String.join(", ", fhirAddress.getLine().stream()
                    .map(StringType::getValue)
                    .toList());
        }

        String district = fhirAddress.hasDistrict() ? fhirAddress.getDistrict() : null;
        String city = fhirAddress.hasCity() ? fhirAddress.getCity() : null;
        String state = fhirAddress.hasState() ? fhirAddress.getState() : null;
        String postalCode = fhirAddress.hasPostalCode() ? fhirAddress.getPostalCode() : null;
        String country = fhirAddress.hasCountry() ? fhirAddress.getCountry() : null;
        
        Coordinates coordinates = extractCoordinates(fhirAddress);

        return new Address(addressLine, district, city, state, postalCode, country, coordinates);
    }

    private static Coordinates extractCoordinates(org.hl7.fhir.r4.model.Address address) {
        if (!address.hasExtension()) {
            return null;
        }

        return address.getExtension().stream()
                .filter(ext -> "http://hl7.org/fhir/StructureDefinition/geolocation".equals(ext.getUrl()))
                .findFirst()
                .map(geo -> {
                    Double lat = null;
                    Double lon = null;
                    for (Extension e : geo.getExtension()) {
                        if ("latitude".equals(e.getUrl()) && e.getValue() instanceof DecimalType dt) {
                            lat = dt.getValue().doubleValue();
                        }
                        if ("longitude".equals(e.getUrl()) && e.getValue() instanceof DecimalType dt) {
                            lon = dt.getValue().doubleValue();
                        }
                    }
                    return new Coordinates(lat, lon);
                })
                .orElse(null);
    }
}
