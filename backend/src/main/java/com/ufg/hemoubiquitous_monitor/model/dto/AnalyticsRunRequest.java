package com.ufg.hemoubiquitous_monitor.model.dto;

import java.util.Date;

/**
 * Parâmetros para disparar um ciclo de análise.
 * Conceitos: window (janela atual) e baseline (janela anterior de mesma duração) são calculados a partir de now quando from/to não são informados.
 */
public class AnalyticsRunRequest {
    // Área (escolher um modo)
    public Double lat;
    public Double lon;
    public Double radiusKm;

    public Double minLat;
    public Double minLon;
    public Double maxLat;
    public Double maxLon;

    public String geohash;
    public Integer geohashPrecision;

    public String city;
    public String state; // UF

    // Tempo
    public String window; // ISO-8601 duration (ex.: PT24H) quando from/to não fornecidos
    public Date from; // opcional
    public Date to;   // opcional
    public String bucket; // ex.: 15m

    // Filtros e thresholds
    public String loinc = "718-7";
    public Integer minSamples = 10;
    public Integer minDistinctPatients = 5;
    public Double thresholdPrevalence = 0.15; // 15%
    public Double thresholdTrendDelta = 0.3;  // +30%
    public Integer clusterMinCases = 5;
}
