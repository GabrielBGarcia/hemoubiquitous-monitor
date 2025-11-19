package com.ufg.hemoubiquitous_monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para a análise espaço-temporal.
 * Mantém defaults (janela, bucket) e thresholds (prevalência, tendência, cluster) ajustáveis por ambiente.
 */
@ConfigurationProperties(prefix = "analytics")
public class AnalyticsProperties {
    private String windowDefault = "PT24H"; // janela padrão (ISO-8601 duration)
    private String bucketDefault = "1h";  // granularidade padrão da série temporal
    private String loincDefault = "718-7"; // hemoglobina

    private Double thresholdPrevalence = 0.15; // 15%
    private Double thresholdTrendDelta = 0.3;  // +30%
    private Integer clusterMinCases = 5;
    private Integer minSamples = 10;
    private Integer minDistinctPatients = 5;
    
    // Retenção de agregados em níveis (dias)
    private Integer retentionDetailedDays = 7;    // Dados detalhados: 7 dias
    private Integer retentionDailyDays = 90;      // Agregados diários: 90 dias  
    private Integer retentionMonthlyDays = 730;   // Agregados mensais: 2 anos

    public String getWindowDefault() { return windowDefault; }
    public void setWindowDefault(String windowDefault) { this.windowDefault = windowDefault; }
    public String getBucketDefault() { return bucketDefault; }
    public void setBucketDefault(String bucketDefault) { this.bucketDefault = bucketDefault; }
    public String getLoincDefault() { return loincDefault; }
    public void setLoincDefault(String loincDefault) { this.loincDefault = loincDefault; }
    public Double getThresholdPrevalence() { return thresholdPrevalence; }
    public void setThresholdPrevalence(Double thresholdPrevalence) { this.thresholdPrevalence = thresholdPrevalence; }
    public Double getThresholdTrendDelta() { return thresholdTrendDelta; }
    public void setThresholdTrendDelta(Double thresholdTrendDelta) { this.thresholdTrendDelta = thresholdTrendDelta; }
    public Integer getClusterMinCases() { return clusterMinCases; }
    public void setClusterMinCases(Integer clusterMinCases) { this.clusterMinCases = clusterMinCases; }
    public Integer getMinSamples() { return minSamples; }
    public void setMinSamples(Integer minSamples) { this.minSamples = minSamples; }
    public Integer getMinDistinctPatients() { return minDistinctPatients; }
    public void setMinDistinctPatients(Integer minDistinctPatients) { this.minDistinctPatients = minDistinctPatients; }
    public Integer getRetentionDetailedDays() { return retentionDetailedDays; }
    public void setRetentionDetailedDays(Integer retentionDetailedDays) { this.retentionDetailedDays = retentionDetailedDays; }
    public Integer getRetentionDailyDays() { return retentionDailyDays; }
    public void setRetentionDailyDays(Integer retentionDailyDays) { this.retentionDailyDays = retentionDailyDays; }
    public Integer getRetentionMonthlyDays() { return retentionMonthlyDays; }
    public void setRetentionMonthlyDays(Integer retentionMonthlyDays) { this.retentionMonthlyDays = retentionMonthlyDays; }
}
