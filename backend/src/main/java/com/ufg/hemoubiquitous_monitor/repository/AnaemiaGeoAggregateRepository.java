package com.ufg.hemoubiquitous_monitor.repository;

import com.ufg.hemoubiquitous_monitor.model.AnaemiaGeoAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface AnaemiaGeoAggregateRepository extends JpaRepository<AnaemiaGeoAggregate, Long> {
    List<AnaemiaGeoAggregate> findByAreaKeyAndWindowFromGreaterThanEqualAndWindowToLessThanEqual(String areaKey, Date windowFrom, Date windowTo);
    List<AnaemiaGeoAggregate> findByAreaKeyAndBucketStartBetween(String areaKey, Date from, Date to);

    // Summary row (bucketStart IS NULL) for exact window and LOINC
    AnaemiaGeoAggregate findFirstByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNullAndLoinc(
            String areaKey, Date windowFrom, Date windowTo, String loinc);

    // Bucket rows (bucketStart NOT NULL) for exact window and LOINC, ordered by time
    List<AnaemiaGeoAggregate> findByAreaKeyAndWindowFromAndWindowToAndBucketStartIsNotNullAndLoincOrderByBucketStartAsc(
            String areaKey, Date windowFrom, Date windowTo, String loinc);

    // Retenção por nível de agregação
    Long deleteByAggregationLevelAndGeneratedAtBefore(String level, Date before);
    
    // Buscar agregados detalhados para reagregar
    List<AnaemiaGeoAggregate> findByAggregationLevelAndWindowFromBetweenAndBucketStartIsNull(
            String level, Date fromStart, Date fromEnd);
}

