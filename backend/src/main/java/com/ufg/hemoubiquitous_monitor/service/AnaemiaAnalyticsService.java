package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.model.AnaemiaGeoAggregate;
import com.ufg.hemoubiquitous_monitor.model.Observation;
import com.ufg.hemoubiquitous_monitor.model.PatientData;
import com.ufg.hemoubiquitous_monitor.model.dto.AnalyticsRunRequest;
import com.ufg.hemoubiquitous_monitor.model.dto.GeoAnaemiaAggregateDto;
import com.ufg.hemoubiquitous_monitor.repository.AnaemiaGeoAggregateRepository;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço de análise espaço-temporal:
 * - Filtra exames (issuedAt) por janela e área.
 * - Agrega por período e buckets.
 * - Calcula prevalência, baseline e severidade.
 * - Persiste em AnaemiaGeoAggregate para leitura rápida.
 * Observação: idempotente por (areaKey, windowFrom, windowTo, bucketStart).
 */

@Service
public class AnaemiaAnalyticsService {
    @Autowired private ObservationRepository observationRepository;
    @Autowired private AnaemiaGeoAggregateRepository aggregateRepository;
    @Autowired private NotificationService notificationService;
    @Autowired(required = false) private com.ufg.hemoubiquitous_monitor.config.AnalyticsProperties analyticsProperties;

    /** 
     * Executa um ciclo de análise espaço-temporal.
     * Chamado automaticamente pelo scheduler ou manualmente via endpoint.
     */
    public GeoAnaemiaAggregateDto runCycle(AnalyticsRunRequest req) {
        // 1) Determinar janela (from/to) caso só tenha window (ex.: PT24H)
        Instant now = Instant.now();
        Instant to = req.to != null ? req.to.toInstant() : now;
        Instant from = req.from != null ? req.from.toInstant() : to.minus(parseDuration(req.window != null ? req.window : "PT24H"));

        // 2) Baseline: janela anterior de mesma duração
        Duration dur = Duration.between(from, to);
        Instant baselineTo = from;
        Instant baselineFrom = baselineTo.minus(dur);

        // 3) Resolver areaKey e metadados da área
        String areaType = deriveAreaType(req);
        String areaKey = buildAreaKey(req);

        // 4) Buscar exames no período
        List<Observation> observations = observationRepository.findByIssuedAtBetween(Date.from(from), Date.from(to));
        List<Observation> inWindow = observations.stream()
            .filter(o -> matchArea(o.getPatientData(), req))
            .filter(o -> matchLoinc(o.getCode(), req.loinc))
            .collect(Collectors.toList());

        // Para baseline, consulta de janela anterior
        List<Observation> baselineCandidates = observationRepository.findByIssuedAtBetween(Date.from(baselineFrom), Date.from(baselineTo));
        List<Observation> inBaseline = baselineCandidates.stream()
            .filter(o -> matchArea(o.getPatientData(), req))
            .filter(o -> matchLoinc(o.getCode(), req.loinc))
            .collect(Collectors.toList());

        // 5) Cálculo simples de métricas agregadas do período
        long total = inWindow.size();
        long anaemic = inWindow.stream().filter(Observation::getHasAnaemia).count();
        Double prevalence = total > 0 ? (double) anaemic / (double) total : 0.0;

        long baselineTotal = inBaseline.size();
        long baselineAnaemic = inBaseline.stream().filter(Observation::getHasAnaemia).count();
        Double baselinePrevalence = baselineTotal > 0 ? (double) baselineAnaemic / (double) baselineTotal : 0.0;
        Double deltaPrevalence = prevalence - baselinePrevalence;
        Double deltaPercent = baselinePrevalence > 0 ? deltaPrevalence / baselinePrevalence : null;

        String severity = classifySeverity(prevalence, deltaPercent, req);

        // 6) Persistência idempotente do summary
        removeExisting(areaKey, Date.from(from), Date.from(to));
        AnaemiaGeoAggregate summary = new AnaemiaGeoAggregate();
        summary.setAreaType(areaType);
        summary.setAreaKey(areaKey);
        summary.setWindowFrom(Date.from(from));
        summary.setWindowTo(Date.from(to));
        // persistir metadados espaciais quando aplicável
        if ("point-radius".equals(areaType)) {
            summary.setAreaType("point-radius");
            summary.setCenterLat(req.lat);
            summary.setCenterLon(req.lon);
            summary.setRadiusKm(req.radiusKm);
        } else if ("bbox".equals(areaType)) {
            summary.setAreaType("bbox");
        } else {
            summary.setAreaType(areaType);
        }
        summary.setTotal(total);
        summary.setAnaemic(anaemic);
        summary.setPrevalence(prevalence);
        summary.setBaselinePrevalence(baselinePrevalence);
        summary.setDeltaPercent(deltaPercent);
        summary.setSeverity(severity);
        summary.setLoinc(req.loinc);
        aggregateRepository.save(summary);

        // 6.1) Série temporal (buckets)
        List<GeoAnaemiaAggregateDto.TimeBucket> series = buildBuckets(inWindow, from, to, req.bucket);
        for (GeoAnaemiaAggregateDto.TimeBucket tb : series) {
            AnaemiaGeoAggregate row = new AnaemiaGeoAggregate();
            row.setAreaType(areaType);
            row.setAreaKey(areaKey);
            row.setWindowFrom(Date.from(from));
            row.setWindowTo(Date.from(to));
            row.setBucketStart(tb.bucketStart);
            row.setTotal(tb.total);
            row.setAnaemic(tb.anaemic);
            row.setPrevalence(tb.prevalence);
            row.setLoinc(req.loinc);
            aggregateRepository.save(row);
        }

        // 7) Montar DTO de retorno
        GeoAnaemiaAggregateDto dto = new GeoAnaemiaAggregateDto();
        dto.area = new GeoAnaemiaAggregateDto.Area();
        dto.area.type = areaType;
        dto.area.city = req.city; dto.area.state = req.state;
        if ("point-radius".equals(areaType)) {
            dto.area.center = new GeoAnaemiaAggregateDto.Center();
            dto.area.center.lat = req.lat; dto.area.center.lon = req.lon;
            dto.area.radiusKm = req.radiusKm;
        }
        if ("bbox".equals(areaType)) {
            dto.area.bbox = new GeoAnaemiaAggregateDto.BBox();
            dto.area.bbox.minLat = req.minLat; dto.area.bbox.minLon = req.minLon;
            dto.area.bbox.maxLat = req.maxLat; dto.area.bbox.maxLon = req.maxLon;
        }
        dto.timeWindow = new GeoAnaemiaAggregateDto.TimeWindow();
        dto.timeWindow.from = Date.from(from); dto.timeWindow.to = Date.from(to); dto.timeWindow.bucket = req.bucket;
        dto.timeWindow.baselineFrom = Date.from(baselineFrom); dto.timeWindow.baselineTo = Date.from(baselineTo);
        dto.metrics = new GeoAnaemiaAggregateDto.Metrics();
        dto.metrics.total = total; dto.metrics.anaemic = anaemic; dto.metrics.prevalence = prevalence;
        dto.metrics.baselinePrevalence = baselinePrevalence; dto.metrics.deltaPrevalence = deltaPrevalence; dto.metrics.deltaPercent = deltaPercent;
        dto.metrics.severity = severity; dto.metrics.minSamples = req.minSamples; dto.metrics.minDistinctPatients = req.minDistinctPatients;
        dto.timeSeries = series;
    dto.clusters = detectClusters(inWindow, req);
        
        // Adicionar breakdown conforme o tipo de área
        if ("global".equals(areaType)) {
            // Breakdown por estado quando área é global
            dto.breakdown = buildStateBreakdown(inWindow, inBaseline, from, to, req);
        } else if ("admin-area".equals(areaType) && req.state != null && req.city == null) {
            // Breakdown por cidade quando área é um estado específico
            dto.breakdown = buildCityBreakdown(inWindow, inBaseline, from, to, req.state, req);
        }


        System.out.println("teste de nome de tópico: " + areaKey + areaType);

        String alertStateMessage = "";

        switch(severity){
            case "none":
                break;
            case "minor":
                alertStateMessage= "Os casos suspeitos de anemia estão aumentando";
                break;
            case "major":
                alertStateMessage= "Os casos suspeitos de anemia estão elevados";
                break;
            case "critical":
                alertStateMessage= "Os casos suspeitos de anemia estão em estado crítico";
                break;
        }

        if(req.city != null && req.state != null)
        notificationService.sendToTopic(
                "anaemia-alerts"+ "-" + req.state + "-" + req.city,
                alertStateMessage, "teste"
//                String.format("Prevalência de %.1f%% detectada em %s", prevalence * 100)
        );
        else if(req.state != null)
            notificationService.sendToTopic(
                    "anaemia-alerts"+ "-" + req.state,
                    alertStateMessage, "teste"
//                    String.format("Prevalência de %.1f%% detectada em %s", prevalence * 100)
            );

        GeoAnaemiaAggregateDto.Thresholds th = new GeoAnaemiaAggregateDto.Thresholds();
        th.thresholdPrevalence = req.thresholdPrevalence; th.thresholdTrendDelta = req.thresholdTrendDelta; th.clusterMinCases = req.clusterMinCases;
        dto.thresholdsUsed = th;
        dto.loinc = req.loinc; dto.generatedAt = new Date();        if ("major".equals(severity)) {
            try {
                Map<String, String> data = new HashMap<>();
                data.put("areaKey", areaKey);
                data.put("areaType", areaType);
                data.put("prevalence", String.valueOf(prevalence));
                data.put("severity", severity);
                data.put("deltaPercent", deltaPercent != null ? String.valueOf(deltaPercent) : "0");
                data.put("total", String.valueOf(total));
                data.put("anaemic", String.valueOf(anaemic));

            } catch (Exception e) {
                System.err.println("Erro ao enviar notificação push: " + e.getMessage());
            }
        }

        return dto;
    }

    /**
     * Busca agregados já persistidos para a janela e área informadas. Se não existirem,
     * executa a análise (runCycle) e retorna o resultado. Útil para o endpoint público.
     */

    public GeoAnaemiaAggregateDto getOrCompute(AnalyticsRunRequest req) {
        // Recalcular a janela de forma idêntica ao runCycle
        Instant now = Instant.now();
        Instant to = req.to != null ? req.to.toInstant() : now;
        Instant from = req.from != null ? req.from.toInstant() : to.minus(parseDuration(req.window != null ? req.window : "PT24H"));
        String areaKey = buildAreaKey(req);

        // Calcular baseline para preencher o DTO quando reutilizando agregados
        Duration dur = Duration.between(from, to);
        Instant baselineTo = from;
        Instant baselineFrom = baselineTo.minus(dur);

        // Tentar localizar summary e buckets já persistidos para este areaKey/janela/loinc
        Date df = Date.from(from), dt = Date.from(to);
        AnaemiaGeoAggregate summary = aggregateRepository.findFirstByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNullAndLoinc(areaKey, df, dt, req.loinc);
        List<AnaemiaGeoAggregate> buckets = aggregateRepository
                .findByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNotNullAndLoincOrderByBucketStartAsc(areaKey, df, dt, req.loinc);

        if (summary != null && buckets != null && !buckets.isEmpty()) {
            // Montar DTO a partir dos agregados salvos
            GeoAnaemiaAggregateDto dto = new GeoAnaemiaAggregateDto();
            dto.area = new GeoAnaemiaAggregateDto.Area();
            dto.area.type = summary.getAreaType();
            dto.area.city = req.city; dto.area.state = req.state;
            if ("point-radius".equals(dto.area.type)) {
                dto.area.center = new GeoAnaemiaAggregateDto.Center();
                dto.area.center.lat = summary.getCenterLat();
                dto.area.center.lon = summary.getCenterLon();
                dto.area.radiusKm = summary.getRadiusKm();
            }
            dto.timeWindow = new GeoAnaemiaAggregateDto.TimeWindow();
            dto.timeWindow.from = df; dto.timeWindow.to = dt; dto.timeWindow.bucket = req.bucket;
            dto.timeWindow.baselineFrom = Date.from(baselineFrom); dto.timeWindow.baselineTo = Date.from(baselineTo);

            dto.metrics = new GeoAnaemiaAggregateDto.Metrics();
            dto.metrics.total = summary.getTotal() != null ? summary.getTotal() : 0L;
            dto.metrics.anaemic = summary.getAnaemic() != null ? summary.getAnaemic() : 0L;
            dto.metrics.prevalence = summary.getPrevalence();
            dto.metrics.baselinePrevalence = summary.getBaselinePrevalence();
            if (summary.getPrevalence() != null && summary.getBaselinePrevalence() != null) {
                dto.metrics.deltaPrevalence = summary.getPrevalence() - summary.getBaselinePrevalence();
            }
            dto.metrics.deltaPercent = summary.getDeltaPercent();
            dto.metrics.severity = summary.getSeverity();
            dto.metrics.minSamples = req.minSamples; dto.metrics.minDistinctPatients = req.minDistinctPatients;

            List<GeoAnaemiaAggregateDto.TimeBucket> series = new ArrayList<>();
            for (AnaemiaGeoAggregate ag : buckets) {
                GeoAnaemiaAggregateDto.TimeBucket tb = new GeoAnaemiaAggregateDto.TimeBucket();
                tb.bucketStart = ag.getBucketStart();
                tb.total = ag.getTotal() != null ? ag.getTotal() : 0L;
                tb.anaemic = ag.getAnaemic() != null ? ag.getAnaemic() : 0L;
                tb.prevalence = ag.getPrevalence();
                series.add(tb);
            }
            dto.timeSeries = series;
            dto.clusters = Collections.emptyList(); // clusters não persistidos nesta versão

            // Recalcular breakdown quando reutilizar agregados
            List<Observation> observations = observationRepository.findByIssuedAtBetween(df, dt);
            List<Observation> inWindow = observations.stream()
                .filter(o -> matchArea(o.getPatientData(), req))
                .filter(o -> matchLoinc(o.getCode(), req.loinc))
                .collect(Collectors.toList());

            List<Observation> baselineCandidates = observationRepository.findByIssuedAtBetween(Date.from(baselineFrom), Date.from(baselineTo));
            List<Observation> inBaseline = baselineCandidates.stream()
                .filter(o -> matchArea(o.getPatientData(), req))
                .filter(o -> matchLoinc(o.getCode(), req.loinc))
                .collect(Collectors.toList());

            // Adicionar breakdown conforme o tipo de área
            if ("global".equals(summary.getAreaType())) {
                // Breakdown por estado quando área é global
                dto.breakdown = buildStateBreakdown(inWindow, inBaseline, from, to, req);
            } else if ("admin-area".equals(summary.getAreaType()) && req.state != null && req.city == null) {
                // Breakdown por cidade quando área é um estado específico
                dto.breakdown = buildCityBreakdown(inWindow, inBaseline, from, to, req.state, req);
            }

            GeoAnaemiaAggregateDto.Thresholds th = new GeoAnaemiaAggregateDto.Thresholds();
            th.thresholdPrevalence = req.thresholdPrevalence; th.thresholdTrendDelta = req.thresholdTrendDelta; th.clusterMinCases = req.clusterMinCases;
            dto.thresholdsUsed = th;
            dto.loinc = req.loinc; dto.generatedAt = new Date();
            return dto;
        }

        // Fallback: executar a análise
        return runCycle(req);
    }

    /**
     * Agregação hierárquica: converte dados detalhados em agregados diários/mensais.
     * Executado automaticamente pelo scheduler após período de acumulação.
     */

    public long aggregateToLevel(String targetLevel) {
        if (!"daily".equals(targetLevel) && !"monthly".equals(targetLevel)) {
            throw new IllegalArgumentException("targetLevel deve ser 'daily' ou 'monthly'");
        }
        
        Instant now = Instant.now();
        Date endDate = Date.from(now);
        Date startDate;
        
        if ("daily".equals(targetLevel)) {
            // Agregar últimas 24h de dados detalhados em 1 agregado diário
            startDate = Date.from(now.minus(Duration.ofDays(1)));
        } else {
            // Agregar último mês de dados diários em 1 agregado mensal
            startDate = Date.from(now.minus(Duration.ofDays(30)));
        }
        
        // Buscar agregados de nível inferior no período
        String sourceLevel = "daily".equals(targetLevel) ? "detailed" : "daily";
        List<AnaemiaGeoAggregate> sources = aggregateRepository
                .findByAggregationLevelAndWindowFromBetweenAndBucketStartIsNull(sourceLevel, startDate, endDate);
        
        // Agrupar por área e reagregar
        Map<String, List<AnaemiaGeoAggregate>> byArea = sources.stream()
                .collect(Collectors.groupingBy(AnaemiaGeoAggregate::getAreaKey));
        
        long saved = 0;
        for (Map.Entry<String, List<AnaemiaGeoAggregate>> entry : byArea.entrySet()) {
            List<AnaemiaGeoAggregate> group = entry.getValue();
            if (group.isEmpty()) continue;
            
            // Calcular métricas agregadas
            long totalSum = group.stream().mapToLong(a -> a.getTotal() != null ? a.getTotal() : 0).sum();
            long anaemicSum = group.stream().mapToLong(a -> a.getAnaemic() != null ? a.getAnaemic() : 0).sum();
            double prevalence = totalSum > 0 ? (double) anaemicSum / totalSum : 0.0;
            
            // Criar agregado de nível superior
            AnaemiaGeoAggregate higher = new AnaemiaGeoAggregate();
            higher.setAreaType(group.get(0).getAreaType());
            higher.setAreaKey(entry.getKey());
            higher.setWindowFrom(startDate);
            higher.setWindowTo(endDate);
            higher.setTotal(totalSum);
            higher.setAnaemic(anaemicSum);
            higher.setPrevalence(prevalence);
            higher.setLoinc(group.get(0).getLoinc());
            higher.setAggregationLevel(targetLevel);
            aggregateRepository.save(higher);
            saved++;
        }
        
        return saved;
    }

    /**
     * Limpeza em níveis: remove agregados antigos respeitando retention por nível.
     */
    public Map<String, Long> cleanupByLevels() {
        Map<String, Long> deleted = new HashMap<>();
        
        if (analyticsProperties != null) {
            // Nível 1: Detailed (7 dias)
            Instant detailedCutoff = Instant.now().minus(Duration.ofDays(
                    analyticsProperties.getRetentionDetailedDays() != null ? analyticsProperties.getRetentionDetailedDays() : 7));
            Long det = aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore("detailed", Date.from(detailedCutoff));
            deleted.put("detailed", det != null ? det : 0L);
            
            // Nível 2: Daily (90 dias)
            Instant dailyCutoff = Instant.now().minus(Duration.ofDays(
                    analyticsProperties.getRetentionDailyDays() != null ? analyticsProperties.getRetentionDailyDays() : 90));
            Long dai = aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore("daily", Date.from(dailyCutoff));
            deleted.put("daily", dai != null ? dai : 0L);
            
            // Nível 3: Monthly (2 anos)
            Instant monthlyCutoff = Instant.now().minus(Duration.ofDays(
                    analyticsProperties.getRetentionMonthlyDays() != null ? analyticsProperties.getRetentionMonthlyDays() : 730));
            Long mon = aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore("monthly", Date.from(monthlyCutoff));
            deleted.put("monthly", mon != null ? mon : 0L);
        }
        
        return deleted;
    }

    private String deriveAreaType(AnalyticsRunRequest req) {
        if (req.city != null || req.state != null) return "admin-area";
        if (req.lat != null && req.lon != null && req.radiusKm != null) return "point-radius";
        if (req.minLat != null && req.minLon != null && req.maxLat != null && req.maxLon != null) return "bbox";
        if (req.geohash != null) return "geohash";
        return "global";
    }

    /**
     * Seleção espacial da área:
     * - admin-area: city/state
     * - point-radius: lat/lon + radiusKm (Haversine)
     * - bbox: minLat/minLon/maxLat/maxLon
     */
    private boolean matchArea(PatientData pd, AnalyticsRunRequest req) {
        if (req.city != null || req.state != null) {
            return matchAdminArea(pd, req.city, req.state);
        }
        if (req.lat != null && req.lon != null && req.radiusKm != null) {
            return matchPointRadius(pd, req.lat, req.lon, req.radiusKm);
        }
        if (req.minLat != null && req.minLon != null && req.maxLat != null && req.maxLon != null) {
            return matchBBox(pd, req.minLat, req.minLon, req.maxLat, req.maxLon);
        }
        // Sem área especificada → aceitar todos
        return true;
    }

    /** BBox simples em graus. */
    private boolean matchBBox(PatientData pd, double minLat, double minLon, double maxLat, double maxLon) {
        if (pd == null || pd.getLatitude() == null || pd.getLongitude() == null) return false;
        double lat = pd.getLatitude();
        double lon = pd.getLongitude();
        return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon;
    }

    /** Ponto+raio com Haversine em km. */
    private boolean matchPointRadius(PatientData pd, double centerLat, double centerLon, double radiusKm) {
        if (pd == null || pd.getLatitude() == null || pd.getLongitude() == null) return false;
        double d = haversineKm(centerLat, centerLon, pd.getLatitude(), pd.getLongitude());
        return d <= radiusKm;
    }

    /** Distância de Haversine (esférica) em quilômetros. */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // raio médio da Terra em km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    /**
     * Detecção simples de clusters: grade fixa (~1km) por arredondamento de lat/lon.
     * - Agrupa pontos por célula: lat/lon arredondados para 0.01 graus (~1.1km lat)
     * - Um cluster é uma célula com casos >= clusterMinCases.
     * - Calcula prevalência e centróide médios da célula.
     */
    private List<GeoAnaemiaAggregateDto.Cluster> detectClusters(List<Observation> inWindow, AnalyticsRunRequest req) {
        Map<String, List<Observation>> cells = new HashMap<>();
        for (Observation o : inWindow) {
            PatientData pd = o.getPatientData();
            if (pd == null || pd.getLatitude() == null || pd.getLongitude() == null) continue;
            double lat = pd.getLatitude();
            double lon = pd.getLongitude();
            // grade de ~1km: arredondar para 0.01 grau (lat ~1.11 km). Para lon varia com latitude, mas serve como aproximação.
            double keyLat = Math.round(lat * 100.0) / 100.0;
            double keyLon = Math.round(lon * 100.0) / 100.0;
            String key = keyLat + ":" + keyLon;
            List<Observation> bucket = cells.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                cells.put(key, bucket);
            }
            bucket.add(o);
        }

        List<GeoAnaemiaAggregateDto.Cluster> out = new ArrayList<>();
        for (Map.Entry<String, List<Observation>> e : cells.entrySet()) {
            List<Observation> list = e.getValue();
            if (list.size() < (req.clusterMinCases != null ? req.clusterMinCases : 5)) continue;

            long total = list.size();
            long anaemic = list.stream().filter(Observation::getHasAnaemia).count();
            double prevalence = total > 0 ? (double) anaemic / (double) total : 0.0;
            double avgLat = list.stream().map(Observation::getPatientData).filter(Objects::nonNull).mapToDouble(PatientData::getLatitude).average().orElse(Double.NaN);
            double avgLon = list.stream().map(Observation::getPatientData).filter(Objects::nonNull).mapToDouble(PatientData::getLongitude).average().orElse(Double.NaN);

            GeoAnaemiaAggregateDto.Cluster c = new GeoAnaemiaAggregateDto.Cluster();
            c.centroid = new GeoAnaemiaAggregateDto.Center();
            c.centroid.lat = avgLat; c.centroid.lon = avgLon;
            c.clusterRadiusKm = 1.5; // aproximação para a célula de ~1km
            c.cases = total; c.anaemic = anaemic; c.prevalence = prevalence;
            c.severity = (prevalence >= req.thresholdPrevalence && total >= req.clusterMinCases) ? "critical" : "major";
            out.add(c);
        }
        return out;
    }

    /** Remove agregados existentes para a mesma área e janela (summary e buckets) antes de regravar. */
    private void removeExisting(String areaKey, Date from, Date to) {
        List<AnaemiaGeoAggregate> existing = aggregateRepository.findByAreaKeyAndWindowFromGreaterThanEqualAndWindowToLessThanEqual(areaKey, from, to);
        if (!existing.isEmpty()) {
            aggregateRepository.deleteAll(existing);
        }
    }

    /**
     * Monta série temporal por buckets de tempo, calculando total/anêmicos/prevalência por intervalo.
     * Conceitos aplicados: bucketização → subdividir a janela para desenhar tendência no app.
     */
    private List<GeoAnaemiaAggregateDto.TimeBucket> buildBuckets(List<Observation> inWindow, Instant from, Instant to, String bucketSpec) {
        Duration bucket = parseBucket(bucketSpec);
        List<GeoAnaemiaAggregateDto.TimeBucket> out = new ArrayList<>();
    for (Instant cursor = from; cursor.isBefore(to); cursor = cursor.plus(bucket)) {
        Instant bucketEnd = cursor.plus(bucket);
        final Instant start = cursor;
        final Instant end = bucketEnd;
        List<Observation> slice = inWindow.stream()
            .filter(o -> o.getIssuedAt() != null && !o.getIssuedAt().toInstant().isBefore(start) && o.getIssuedAt().toInstant().isBefore(end))
                    .collect(Collectors.toList());
        long total = slice.size();
            long anaemic = slice.stream().filter(Observation::getHasAnaemia).count();
            Double prev = total > 0 ? (double) anaemic / (double) total : 0.0;
            GeoAnaemiaAggregateDto.TimeBucket tb = new GeoAnaemiaAggregateDto.TimeBucket();
            tb.bucketStart = Date.from(cursor);
            tb.total = total; tb.anaemic = anaemic; tb.prevalence = prev;
            out.add(tb);
        }
        return out;
    }

    private Duration parseBucket(String spec) {
        if (spec == null) return Duration.ofMinutes(15);
        try {
            // formatos simples: "15m", "1h", "1d"
            if (spec.endsWith("m")) return Duration.ofMinutes(Long.parseLong(spec.replace("m", "")));
            if (spec.endsWith("h")) return Duration.ofHours(Long.parseLong(spec.replace("h", "")));
            if (spec.endsWith("d")) return Duration.ofDays(Long.parseLong(spec.replace("d", "")));
            // fallback ISO-8601
            return Duration.parse(spec);
        } catch (Exception e) {
            return Duration.ofMinutes(15);
        }
    }

    private boolean matchAdminArea(PatientData pd, String city, String state) {
        if (city == null && state == null) return true;
        if (pd == null) return false;
        boolean okCity = city == null || (pd.getCity() != null && pd.getCity().equalsIgnoreCase(city));
        boolean okState = state == null || (pd.getState() != null && pd.getState().equalsIgnoreCase(state));
        return okCity && okState;
    }

    private boolean matchLoinc(String code, String expectedLoinc) {
        return expectedLoinc == null || expectedLoinc.equals(code);
    }

    private String buildAreaKey(AnalyticsRunRequest req) {
        if (req.city != null || req.state != null) {
            return "city:" + (req.state != null ? req.state : "") + ":" + (req.city != null ? req.city : "");
        }
        if (req.geohash != null) {
            return "gh" + (req.geohashPrecision != null ? req.geohashPrecision : "") + ":" + req.geohash;
        }
        if (req.lat != null && req.lon != null && req.radiusKm != null) {
            return String.format(Locale.ROOT, "pr:%.5f:%.5f:%.0f", req.lat, req.lon, req.radiusKm);
        }
        if (req.minLat != null && req.minLon != null && req.maxLat != null && req.maxLon != null) {
            return String.format(Locale.ROOT, "bb:%.5f:%.5f:%.5f:%.5f", req.minLat, req.minLon, req.maxLat, req.maxLon);
        }
        return "global";
    }

    private Duration parseDuration(String iso) {
        try { return Duration.parse(iso); } catch (Exception e) { return Duration.ofHours(24); }
    }

    private String classifySeverity(Double prevalence, Double deltaPercent, AnalyticsRunRequest req) {
        boolean highPrev = prevalence != null && prevalence >= req.thresholdPrevalence;
        boolean rising = deltaPercent != null && deltaPercent >= req.thresholdTrendDelta;
        if (highPrev && rising) return "major";
        if (highPrev) return "minor";
        if (rising) return "minor";
        return "none";
    }

    /**
     * Gera breakdown por estado quando a área é global.
     * Agrupa observações por estado e calcula métricas + série temporal para cada um.
     */
    private GeoAnaemiaAggregateDto.Breakdown buildStateBreakdown(
            List<Observation> inWindow,
            List<Observation> inBaseline,
            Instant from,
            Instant to,
            AnalyticsRunRequest req) {
        
        // Agrupar observações por estado
        Map<String, List<Observation>> byState = inWindow.stream()
                .filter(o -> o.getPatientData() != null && o.getPatientData().getState() != null)
                .collect(Collectors.groupingBy(o -> o.getPatientData().getState()));

        // Agrupar baseline por estado
        Map<String, List<Observation>> baselineByState = inBaseline.stream()
                .filter(o -> o.getPatientData() != null && o.getPatientData().getState() != null)
                .collect(Collectors.groupingBy(o -> o.getPatientData().getState()));

        List<GeoAnaemiaAggregateDto.StateBreakdown> stateList = new ArrayList<>();

        for (Map.Entry<String, List<Observation>> entry : byState.entrySet()) {
            String state = entry.getKey();
            List<Observation> stateObs = entry.getValue();
            List<Observation> stateBaseline = baselineByState.getOrDefault(state, Collections.emptyList());

            // Calcular métricas do estado
            long total = stateObs.size();
            long anaemic = stateObs.stream().filter(Observation::getHasAnaemia).count();
            Double prevalence = total > 0 ? (double) anaemic / (double) total : 0.0;

            long baselineTotal = stateBaseline.size();
            long baselineAnaemic = stateBaseline.stream().filter(Observation::getHasAnaemia).count();
            Double baselinePrevalence = baselineTotal > 0 ? (double) baselineAnaemic / (double) baselineTotal : 0.0;
            Double deltaPrevalence = prevalence - baselinePrevalence;
            Double deltaPercent = baselinePrevalence > 0 ? deltaPrevalence / baselinePrevalence : null;

            String severity = classifySeverity(prevalence, deltaPercent, req);

            // Criar objeto StateBreakdown
            GeoAnaemiaAggregateDto.StateBreakdown sb = new GeoAnaemiaAggregateDto.StateBreakdown();
            sb.state = state;
            
            sb.metrics = new GeoAnaemiaAggregateDto.Metrics();
            sb.metrics.total = total;
            sb.metrics.anaemic = anaemic;
            sb.metrics.prevalence = prevalence;
            sb.metrics.baselinePrevalence = baselinePrevalence;
            sb.metrics.deltaPrevalence = deltaPrevalence;
            sb.metrics.deltaPercent = deltaPercent;
            sb.metrics.severity = severity;

            // Série temporal por estado
            sb.timeSeries = buildBuckets(stateObs, from, to, req.bucket);

            stateList.add(sb);
        }

        // Ordenar por total decrescente
        stateList.sort((a, b) -> Long.compare(b.metrics.total, a.metrics.total));

        GeoAnaemiaAggregateDto.Breakdown breakdown = new GeoAnaemiaAggregateDto.Breakdown();
        breakdown.type = "by-state";
        breakdown.states = stateList;
        
        return breakdown;
    }

    /**
     * Gera breakdown por cidade quando a área é um estado específico.
     * Agrupa observações por cidade e calcula métricas + série temporal para cada uma.
     */
    private GeoAnaemiaAggregateDto.Breakdown buildCityBreakdown(
            List<Observation> inWindow,
            List<Observation> inBaseline,
            Instant from,
            Instant to,
            String state,
            AnalyticsRunRequest req) {
        
        // Agrupar observações por cidade
        Map<String, List<Observation>> byCity = inWindow.stream()
                .filter(o -> o.getPatientData() != null && o.getPatientData().getCity() != null)
                .collect(Collectors.groupingBy(o -> o.getPatientData().getCity()));

        // Agrupar baseline por cidade
        Map<String, List<Observation>> baselineByCity = inBaseline.stream()
                .filter(o -> o.getPatientData() != null && o.getPatientData().getCity() != null)
                .collect(Collectors.groupingBy(o -> o.getPatientData().getCity()));

        List<GeoAnaemiaAggregateDto.CityBreakdown> cityList = new ArrayList<>();

        for (Map.Entry<String, List<Observation>> entry : byCity.entrySet()) {
            String city = entry.getKey();
            List<Observation> cityObs = entry.getValue();
            List<Observation> cityBaseline = baselineByCity.getOrDefault(city, Collections.emptyList());

            // Calcular métricas da cidade
            long total = cityObs.size();
            long anaemic = cityObs.stream().filter(Observation::getHasAnaemia).count();
            Double prevalence = total > 0 ? (double) anaemic / (double) total : 0.0;

            long baselineTotal = cityBaseline.size();
            long baselineAnaemic = cityBaseline.stream().filter(Observation::getHasAnaemia).count();
            Double baselinePrevalence = baselineTotal > 0 ? (double) baselineAnaemic / (double) baselineTotal : 0.0;
            Double deltaPrevalence = prevalence - baselinePrevalence;
            Double deltaPercent = baselinePrevalence > 0 ? deltaPrevalence / baselinePrevalence : null;

            String severity = classifySeverity(prevalence, deltaPercent, req);

            // Criar objeto CityBreakdown
            GeoAnaemiaAggregateDto.CityBreakdown cb = new GeoAnaemiaAggregateDto.CityBreakdown();
            cb.city = city;
            cb.state = state;
            
            cb.metrics = new GeoAnaemiaAggregateDto.Metrics();
            cb.metrics.total = total;
            cb.metrics.anaemic = anaemic;
            cb.metrics.prevalence = prevalence;
            cb.metrics.baselinePrevalence = baselinePrevalence;
            cb.metrics.deltaPrevalence = deltaPrevalence;
            cb.metrics.deltaPercent = deltaPercent;
            cb.metrics.severity = severity;

            // Série temporal por cidade
            cb.timeSeries = buildBuckets(cityObs, from, to, req.bucket);

            cityList.add(cb);
        }

        // Ordenar por total decrescente
        cityList.sort((a, b) -> Long.compare(b.metrics.total, a.metrics.total));

        GeoAnaemiaAggregateDto.Breakdown breakdown = new GeoAnaemiaAggregateDto.Breakdown();
        breakdown.type = "by-city";
        breakdown.cities = cityList;
        
        return breakdown;
    }
}
