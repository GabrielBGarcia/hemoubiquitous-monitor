package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.config.AnalyticsProperties;
import com.ufg.hemoubiquitous_monitor.model.AnaemiaGeoAggregate;
import com.ufg.hemoubiquitous_monitor.model.dto.AnalyticsRunRequest;
import com.ufg.hemoubiquitous_monitor.model.dto.GeoAnaemiaAggregateDto;
import com.ufg.hemoubiquitous_monitor.repository.AnaemiaGeoAggregateRepository;
import com.ufg.hemoubiquitous_monitor.repository.ObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnaemiaAnalyticsServiceTest {

    @Mock private ObservationRepository observationRepository;
    @Mock private AnaemiaGeoAggregateRepository aggregateRepository;
    @Mock private AnalyticsProperties analyticsProperties;

    @InjectMocks private AnaemiaAnalyticsService analyticsService;

    @Test
    public void getOrCompute_returnsDtoFromPersistedAggregates() {
        // prepare persisted summary
        AnaemiaGeoAggregate summary = new AnaemiaGeoAggregate();
        summary.setAreaType("admin-area");
        summary.setTotal(100L); summary.setAnaemic(20L);
        summary.setPrevalence(0.2);
        summary.setBaselinePrevalence(0.1);
        summary.setDeltaPercent(1.0);
        summary.setSeverity("major");

        // bucket row
        AnaemiaGeoAggregate bucket = new AnaemiaGeoAggregate();
        bucket.setBucketStart(Date.from(Instant.now().minus(Duration.ofHours(1))));
        bucket.setTotal(50L); bucket.setAnaemic(10L); bucket.setPrevalence(0.2);

        when(aggregateRepository.findFirstByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNullAndLoinc(anyString(), any(Date.class), any(Date.class), anyString()))
                .thenReturn(summary);
        when(aggregateRepository.findByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNotNullAndLoincOrderByBucketStartAsc(anyString(), any(Date.class), any(Date.class), anyString()))
                .thenReturn(Arrays.asList(bucket));

        AnalyticsRunRequest req = new AnalyticsRunRequest();
        req.loinc = "718-7"; // default

        GeoAnaemiaAggregateDto dto = analyticsService.getOrCompute(req);
        assertThat(dto).isNotNull();
        assertThat(dto.metrics).isNotNull();
        assertThat(dto.metrics.total).isEqualTo(100L);
        assertThat(dto.timeSeries).hasSize(1);
        assertThat(dto.timeSeries.get(0).total).isEqualTo(50L);
    }

    @Test
    public void cleanupByLevels_callsRepositoryForEachLevelAndReturnsCount() {
        when(analyticsProperties.getRetentionDetailedDays()).thenReturn(7);
        when(analyticsProperties.getRetentionDailyDays()).thenReturn(90);
        when(analyticsProperties.getRetentionMonthlyDays()).thenReturn(730);
        when(aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore(eq("detailed"), any(Date.class))).thenReturn(5L);
        when(aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore(eq("daily"), any(Date.class))).thenReturn(3L);
        when(aggregateRepository.deleteByAggregationLevelAndGeneratedAtBefore(eq("monthly"), any(Date.class))).thenReturn(1L);

        Map<String, Long> deleted = analyticsService.cleanupByLevels();
        assertThat(deleted).containsEntry("detailed", 5L);
        assertThat(deleted).containsEntry("daily", 3L);
        assertThat(deleted).containsEntry("monthly", 1L);
    }
}
