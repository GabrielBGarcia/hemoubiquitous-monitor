package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.model.dto.AnalyticsRunRequest;
import com.ufg.hemoubiquitous_monitor.model.dto.GeoAnaemiaAggregateDto;
import com.ufg.hemoubiquitous_monitor.service.AnaemiaAnalyticsService;
import com.ufg.hemoubiquitous_monitor.config.AnalyticsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoints para:
 * - Disparo manual da análise (também executada automaticamente pelo scheduler).
 * - Consulta pública dos agregados para o aplicativo.
 */
@RestController
@RequestMapping
public class AnalyticsController {
    @Autowired private AnaemiaAnalyticsService analyticsService;
    @Autowired private AnalyticsProperties analyticsProperties;

    /**
     * POST /internal/analytics/run
     * Dispara um ciclo de análise manualmente.
     * Nota: Este endpoint é executado automaticamente a cada 60s pelo scheduler interno.
     * Idempotência: a mesma combinação de parâmetros deve resultar nos mesmos agregados.
     */
    @PostMapping("/internal/analytics/run")
    public ResponseEntity<GeoAnaemiaAggregateDto> run(@RequestBody AnalyticsRunRequest request) {
        GeoAnaemiaAggregateDto dto = analyticsService.runCycle(request);
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/analytics/anaemia/geo
     * Retorna os dados agregados para a área/tempo solicitados.
     */
    @GetMapping("/api/analytics/anaemia/geo")
    public ResponseEntity<GeoAnaemiaAggregateDto> get(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String window,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String loinc
    ) {
        AnalyticsRunRequest req = new AnalyticsRunRequest();
        req.city = city; req.state = state;
        req.window = (window != null ? window : analyticsProperties.getWindowDefault());
        req.bucket = (bucket != null ? bucket : analyticsProperties.getBucketDefault());
        req.loinc = (loinc != null ? loinc : analyticsProperties.getLoincDefault());
        req.thresholdPrevalence = analyticsProperties.getThresholdPrevalence();
        req.thresholdTrendDelta = analyticsProperties.getThresholdTrendDelta();
        req.clusterMinCases = analyticsProperties.getClusterMinCases();
        req.minSamples = analyticsProperties.getMinSamples();
        req.minDistinctPatients = analyticsProperties.getMinDistinctPatients();
        GeoAnaemiaAggregateDto dto = analyticsService.getOrCompute(req);
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /internal/analytics/cleanup-tiered
     * Remove agregados antigos respeitando retention por nível (detailed/daily/monthly).
     * Nota: Este endpoint é executado automaticamente todo dia às 3h AM pelo scheduler interno.
     */
    @PostMapping("/internal/analytics/cleanup-tiered")
    public ResponseEntity<Map<String, Object>> cleanupTiered() {
        Map<String, Long> deleted = analyticsService.cleanupByLevels();
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", deleted);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /internal/analytics/aggregate-daily
     * Agrega dados detalhados (últimas 24h) em agregados diários.
     * Nota: Este endpoint é executado automaticamente todo dia às 2h AM pelo scheduler interno.
     */
    @PostMapping("/internal/analytics/aggregate-daily")
    public ResponseEntity<Map<String, Object>> aggregateDaily() {
        long saved = analyticsService.aggregateToLevel("daily");
        Map<String, Object> response = new HashMap<>();
        response.put("level", "daily");
        response.put("aggregatesSaved", saved);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /internal/analytics/aggregate-monthly
     * Agrega dados diários (último mês) em agregados mensais.
     * Nota: Este endpoint é executado automaticamente no dia 1 de cada mês às 3h AM pelo scheduler interno.
     */
    @PostMapping("/internal/analytics/aggregate-monthly")
    public ResponseEntity<Map<String, Object>> aggregateMonthly() {
        long saved = analyticsService.aggregateToLevel("monthly");
        Map<String, Object> response = new HashMap<>();
        response.put("level", "monthly");
        response.put("aggregatesSaved", saved);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}

