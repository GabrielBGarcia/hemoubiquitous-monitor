package com.ufg.hemoubiquitous_monitor.domain.hemogram.model;

import java.util.Date;

public class HemogramAnalysis {
    private final String identifier;
    private final String loincCode;
    private final String origin;
    private final Hemoglobin hemoglobin;
    private final AnaemiaStatus anaemiaStatus;
    private final Date issuedAt;
    private final String patientCpf;

    public HemogramAnalysis(
            String identifier,
            String loincCode,
            String origin,
            Hemoglobin hemoglobin,
            AnaemiaStatus anaemiaStatus,
            Date issuedAt,
            String patientCpf) {
        this.identifier = identifier;
        this.loincCode = loincCode;
        this.origin = origin;
        this.hemoglobin = hemoglobin;
        this.anaemiaStatus = anaemiaStatus;
        this.issuedAt = issuedAt;
        this.patientCpf = patientCpf;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLoincCode() {
        return loincCode;
    }

    public String getOrigin() {
        return origin;
    }

    public Hemoglobin getHemoglobin() {
        return hemoglobin;
    }

    public AnaemiaStatus getAnaemiaStatus() {
        return anaemiaStatus;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public String getPatientCpf() {
        return patientCpf;
    }
}
