package com.ufg.hemoubiquitous_monitor.model;

import jakarta.persistence.*;

import java.util.Date;

/**
 * Armazena agregações espaço-temporais para consumo rápido pelo app.
 * Conceitos aplicados:
 * - windowFrom/windowTo: janela de tempo clínica (baseada em issuedAt) analisada.
 * - areaType/areaKey: identificação lógica da área (ex.: admin-area "city:GO:Goiânia").
 * - bucketStart: início do intervalo da série temporal; linhas de resumo podem deixar null.
 * - prevalence: proporção de casos anêmicos (= anaemic / total).
 * - baselinePrevalence e deltaPercent: comparação com janela anterior para detectar aumento anormal.
 * - severity: classificação (none/minor/major/critical) baseada em limiares (thresholds) configuráveis.
 */
@Entity
@Table(
        name = "anaemia_geo_aggregate",
        indexes = {
                @Index(name = "idx_agg_areaKey_window", columnList = "area_key, window_from, window_to"),
                @Index(name = "idx_agg_bucket", columnList = "area_key, bucket_start"),
                @Index(name = "idx_agg_generatedAt", columnList = "generated_at")
        }
)
public class AnaemiaGeoAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificação da área analisada
    @Column(name = "area_type", length = 30, nullable = false)
    private String areaType; // "admin-area", "point-radius", "bbox", "geohash"

    @Column(name = "area_key", length = 255, nullable = false)
    private String areaKey; // ex.: "city:GO:Goiania" ou "gh6:6gkzq3" ou "pr:-16.68:-49.26:10"

    // Parametrização espacial (opcional conforme areaType)
    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lon")
    private Double centerLon;

    @Column(name = "radius_km")
    private Double radiusKm;

    // Janela temporal analisada
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "window_from", nullable = false)
    private Date windowFrom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "window_to", nullable = false)
    private Date windowTo;

    // Série temporal por bucket (linhas com bucketStart != null)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "bucket_start")
    private Date bucketStart;

    // Métricas principais
    @Column(name = "total")
    private Long total;

    @Column(name = "anaemic")
    private Long anaemic;

    @Column(name = "prevalence")
    private Double prevalence;

    // Comparação com baseline
    @Column(name = "baseline_prevalence")
    private Double baselinePrevalence;

    @Column(name = "delta_percent")
    private Double deltaPercent; // (prevalence - baseline) / baseline

    @Column(name = "severity", length = 15)
    private String severity; // none | minor | major | critical

    @Column(name = "loinc", length = 20)
    private String loinc; // default: 718-7

    // Nível de agregação para retenção diferenciada
    @Column(name = "aggregation_level", length = 20, nullable = false)
    private String aggregationLevel = "detailed"; // detailed | daily | monthly

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "generated_at", nullable = false)
    private Date generatedAt = new Date();

    public AnaemiaGeoAggregate() {}

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public String getAreaKey() { return areaKey; }
    public void setAreaKey(String areaKey) { this.areaKey = areaKey; }
    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }
    public Double getCenterLon() { return centerLon; }
    public void setCenterLon(Double centerLon) { this.centerLon = centerLon; }
    public Double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(Double radiusKm) { this.radiusKm = radiusKm; }
    public Date getWindowFrom() { return windowFrom; }
    public void setWindowFrom(Date windowFrom) { this.windowFrom = windowFrom; }
    public Date getWindowTo() { return windowTo; }
    public void setWindowTo(Date windowTo) { this.windowTo = windowTo; }
    public Date getBucketStart() { return bucketStart; }
    public void setBucketStart(Date bucketStart) { this.bucketStart = bucketStart; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getAnaemic() { return anaemic; }
    public void setAnaemic(Long anaemic) { this.anaemic = anaemic; }
    public Double getPrevalence() { return prevalence; }
    public void setPrevalence(Double prevalence) { this.prevalence = prevalence; }
    public Double getBaselinePrevalence() { return baselinePrevalence; }
    public void setBaselinePrevalence(Double baselinePrevalence) { this.baselinePrevalence = baselinePrevalence; }
    public Double getDeltaPercent() { return deltaPercent; }
    public void setDeltaPercent(Double deltaPercent) { this.deltaPercent = deltaPercent; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLoinc() { return loinc; }
    public void setLoinc(String loinc) { this.loinc = loinc; }
    public String getAggregationLevel() { return aggregationLevel; }
    public void setAggregationLevel(String aggregationLevel) { this.aggregationLevel = aggregationLevel; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date generatedAt) { this.generatedAt = generatedAt; }
}
