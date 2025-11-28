package com.ufg.hemoubiquitous_monitor.domain.hemogram.model;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.exception.InvalidHemoglobinValueException;

public class Hemoglobin {
    private final double valueInGramsPerLiter;

    private Hemoglobin(double valueInGramsPerLiter) {
        this.valueInGramsPerLiter = valueInGramsPerLiter;
    }

    public static Hemoglobin fromGramsPerDeciliter(double valueInGramsPerDeciliter) {
        double valueInGramsPerLiter = valueInGramsPerDeciliter * 10;
        validate(valueInGramsPerLiter);
        return new Hemoglobin(valueInGramsPerLiter);
    }

    public static Hemoglobin fromGramsPerLiter(double valueInGramsPerLiter) {
        validate(valueInGramsPerLiter);
        return new Hemoglobin(valueInGramsPerLiter);
    }

    private static void validate(double value) {
        if (value < 0) {
            throw new InvalidHemoglobinValueException("Valor da hemoglobina não pode ser negativo: " + value);
        }
    }

    public double getValueInGramsPerLiter() {
        return valueInGramsPerLiter;
    }

    public double getValueInGramsPerDeciliter() {
        return valueInGramsPerLiter / 10;
    }
}
