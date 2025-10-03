package com.ufg.hemoubiquitous_monitor.model;

import java.util.Date;

public class AnaemiaAnalysisResult {
    private Boolean hasAnaemia;
    private Double haemoglobinInGramsPerLitre;
    private Date AnalyzedAt;

    public AnaemiaAnalysisResult(Boolean hasAnaemia, Double haemoglobinInGramsPerLitre, Date analyzedAt) {
        this.hasAnaemia = hasAnaemia;
        this.haemoglobinInGramsPerLitre = haemoglobinInGramsPerLitre;
        AnalyzedAt = analyzedAt;
    }

    public Boolean getHasAnaemia() {
        return hasAnaemia;
    }

    public void setHasAnaemia(Boolean hasAnaemia) {
        this.hasAnaemia = hasAnaemia;
    }

    public Double getHaemoglobinInGramsPerLitre() {
        return haemoglobinInGramsPerLitre;
    }

    public void setHaemoglobinInGramsPerLitre(Double haemoglobinInGramsPerLitre) {
        this.haemoglobinInGramsPerLitre = haemoglobinInGramsPerLitre;
    }

    public Date getAnalyzedAt() {
        return AnalyzedAt;
    }

    public void setAnalyzedAt(Date analyzedAt) {
        AnalyzedAt = analyzedAt;
    }
}
