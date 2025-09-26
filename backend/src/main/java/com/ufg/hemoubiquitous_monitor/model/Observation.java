package com.ufg.hemoubiquitous_monitor.model;

import jakarta.persistence.*;
import org.hl7.fhir.r4.model.Quantity;

import java.util.Date;

@Entity
@Table(name = "observation")
public class Observation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // se o ID for auto-incremento
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "display", length = 255)
    private String display;
//
//    @Transient
//    private Quantity quantity;

    @Column(name = "haemoglobin_g_dl")
    private Double haemoglobinInGramsPerLitre;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public Observation() {

    }

    public Boolean getHasAnaemia() {
        return hasAnaemia;
    }

    public void setHasAnaemia(Boolean hasAnaemia) {
        this.hasAnaemia = hasAnaemia;
    }

    @Column(name = "has_anaemia")
    private Boolean hasAnaemia;

    // Getters e setters

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
//
//    public Quantity getQuantity() {
//        return quantity;
//    }
//
//    public void setQuantity(Quantity quantity) {
//        this.quantity = quantity;
//    }

    public Double getHaemoglobinInGramsPerLitre() {
        return haemoglobinInGramsPerLitre;
    }

    public void setHaemoglobinInGramsPerLitre(Double haemoglobinInGramsPerLitre) {
        this.haemoglobinInGramsPerLitre = haemoglobinInGramsPerLitre;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Observation(String code, String display, Double haemoglobinInGramsPerLitre, Date createdAt, Boolean hasAnaemia) {
        this.code = code;
        this.display = display;
        this.haemoglobinInGramsPerLitre = haemoglobinInGramsPerLitre;
        this.createdAt = createdAt;
        this.hasAnaemia = hasAnaemia;
    }
}
