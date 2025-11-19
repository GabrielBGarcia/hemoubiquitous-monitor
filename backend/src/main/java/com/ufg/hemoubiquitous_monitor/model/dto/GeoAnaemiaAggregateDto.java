package com.ufg.hemoubiquitous_monitor.model.dto;

import java.util.Date;
import java.util.List;

/**
 * Agrega métricas do período e série temporal.
 * Mantém os conceitos de área, janela temporal (com baseline), thresholds usados e clusters.
 */

public class GeoAnaemiaAggregateDto {
    public Area area;
    public TimeWindow timeWindow;
    public Metrics metrics;
    public List<TimeBucket> timeSeries;
    public List<Cluster> clusters;
    public Breakdown breakdown;
    public Thresholds thresholdsUsed;
    public String loinc;
    public Date generatedAt;
    public String version = "1.0.0";

    public static class Area {
        public String type; // admin-area, point-radius, bbox, geohash
        public Center center; // quando aplicável
        public Double radiusKm; // quando aplicável
        public BBox bbox; // quando aplicável
        public String geohash; // quando aplicável
        public String city; // quando aplicável
        public String state; // quando aplicável
    }

    public static class Center { public Double lat; public Double lon; }
    public static class BBox { public Double minLat, minLon, maxLat, maxLon; }

    public static class TimeWindow {
        public Date from; public Date to; public String bucket;
        public Date baselineFrom; public Date baselineTo;
    }

    public static class Metrics {
        public Long total; public Long anaemic; public Double prevalence;
        public Double baselinePrevalence; public Double deltaPrevalence; public Double deltaPercent;
        public String severity; // none | minor | major | critical
        public Integer minSamples; public Integer minDistinctPatients;
    }

    public static class TimeBucket { public Date bucketStart; public Long total; public Long anaemic; public Double prevalence; }

    public static class Cluster {
        public Center centroid; public Double clusterRadiusKm;
        public Long cases; public Long anaemic; public Double prevalence;
        public String geohash; public String severity;
    }

    public static class Breakdown {
        public String type; // by-state, by-city
        public List<StateBreakdown> states;
        public List<CityBreakdown> cities;
    }

    public static class StateBreakdown {
        public String state;
        public Metrics metrics;
        public List<TimeBucket> timeSeries;
    }

    public static class CityBreakdown {
        public String city;
        public String state;
        public Metrics metrics;
        public List<TimeBucket> timeSeries;
    }

    public static class Thresholds {
        public Double thresholdPrevalence; public Double thresholdTrendDelta; public Integer clusterMinCases;
    }
}
