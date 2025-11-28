package com.ufg.hemoubiquitous_monitor.domain.patient.model;

import java.util.Date;

public class Patient {
    private final String cpf;
    private final String name;
    private final String gender;
    private final Date birthDate;
    private final String phone;
    private final Address address;

    public Patient(String cpf, String name, String gender, Date birthDate, String phone, Address address) {
        this.cpf = cpf;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
    }

    public String getCpf() {
        return cpf;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }
}
