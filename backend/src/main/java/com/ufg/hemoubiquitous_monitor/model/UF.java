package com.ufg.hemoubiquitous_monitor.model;

import java.util.Arrays;
import java.util.Optional;

public enum UF {
    AC("Acre"),
    AL("Alagoas"),
    AP("Amapá"),
    AM("Amazonas"),
    BA("Bahia"),
    CE("Ceará"),
    DF("Distrito Federal"),
    ES("Espírito Santo"),
    GO("Goiás"),
    MA("Maranhão"),
    MT("Mato Grosso"),
    MS("Mato Grosso do Sul"),
    MG("Minas Gerais"),
    PA("Pará"),
    PB("Paraíba"),
    PR("Paraná"),
    PE("Pernambuco"),
    PI("Piauí"),
    RJ("Rio de Janeiro"),
    RN("Rio Grande do Norte"),
    RS("Rio Grande do Sul"),
    RO("Rondônia"),
    RR("Roraima"),
    SC("Santa Catarina"),
    SP("São Paulo"),
    SE("Sergipe"),
    TO("Tocantins");

    private final String nome;

    UF(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return name();
    }

    public String getNome() {
        return nome;
    }

    public static Optional<UF> fromSigla(String sigla) {
        if (sigla == null) return Optional.empty();
        String s = sigla.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(u -> u.name().equals(s))
                .findFirst();
    }
}
