package com.ufg.hemoubiquitous_monitor.infrastructure.persistence.mapper;

import com.ufg.hemoubiquitous_monitor.domain.patient.model.Address;
import com.ufg.hemoubiquitous_monitor.domain.patient.model.Coordinates;
import com.ufg.hemoubiquitous_monitor.domain.patient.model.Patient;
import com.ufg.hemoubiquitous_monitor.model.PatientData;
import com.ufg.hemoubiquitous_monitor.repository.PatientDataRepository;

public class PatientEntityMapper {

    public static PatientData toEntity(Patient patient, PatientDataRepository repository) {
        if (patient == null) {
            return null;
        }

        PatientData entity = repository.findByCpf(patient.getCpf()).orElseGet(PatientData::new);

        entity.setCpf(patient.getCpf());
        entity.setName(patient.getName());
        entity.setGender(patient.getGender());
        entity.setBirthDate(patient.getBirthDate());
        entity.setPhone(patient.getPhone());

        if (patient.getAddress() != null) {
            Address address = patient.getAddress();
            entity.setAddressLine(address.getAddressLine());
            entity.setDistrict(address.getDistrict());
            entity.setCity(address.getCity());
            entity.setState(address.getState());
            entity.setPostalCode(address.getPostalCode());
            entity.setCountry(address.getCountry());

            if (address.getCoordinates() != null && address.getCoordinates().isValid()) {
                entity.setLatitude(address.getCoordinates().getLatitude());
                entity.setLongitude(address.getCoordinates().getLongitude());
            }
        }

        return entity;
    }

    public static Patient toDomain(PatientData entity) {
        if (entity == null) {
            return null;
        }

        Coordinates coordinates = new Coordinates(entity.getLatitude(), entity.getLongitude());
        
        Address address = new Address(
                entity.getAddressLine(),
                entity.getDistrict(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry(),
                coordinates
        );

        return new Patient(
                entity.getCpf(),
                entity.getName(),
                entity.getGender(),
                entity.getBirthDate(),
                entity.getPhone(),
                address
        );
    }
}
