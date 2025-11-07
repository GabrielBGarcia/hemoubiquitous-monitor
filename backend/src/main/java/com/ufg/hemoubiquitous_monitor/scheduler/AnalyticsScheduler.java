package com.ufg.hemoubiquitous_monitor.scheduler;

import com.ufg.hemoubiquitous_monitor.model.dto.AnalyticsRunRequest;
import com.ufg.hemoubiquitous_monitor.service.AnaemiaAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Agendador automático das tarefas de análise periódica.
 * Executa automaticamente apenas se habilitado em application.properties.
 * 
 * Para habilitar, adicione no application.properties:
 * analytics.scheduler.enabled=true
 */
@Component
@ConditionalOnProperty(name = "analytics.scheduler.enabled", havingValue = "true")
public class AnalyticsScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsScheduler.class);
    
    @Autowired
    private AnaemiaAnalyticsService analyticsService;
    
    @Value("${analytics.scheduler.analysis-interval:60000}")
    private long analysisInterval;
    
    /**
     * Executa análise periodicamente (padrão: a cada 60 segundos)
     * Intervalo configurável em application.properties: analytics.scheduler.analysis-interval
     * NOTA: Desabilitado por padrão durante desenvolvimento para não poluir o banco.
     * Para habilitar, configure analytics.scheduler.enabled=true
     */
    @Transactional
    @Scheduled(fixedRateString = "${analytics.scheduler.analysis-interval:60000}")
    public void runPeriodicAnalysis() {
        logger.info(" [SCHEDULER] Iniciando análise periódica automática - {}", Instant.now());
        
        try {
            AnalyticsRunRequest request = new AnalyticsRunRequest();
            request.window = "PT1H";  // Apenas 1 hora ao invés de 24h
            request.bucket = "15m";   // Buckets de 15min (resultará em 4 registros)
            request.loinc = "718-7";
            request.state = "GO";     // Especificar estado ao invés de global
            
            analyticsService.runCycle(request);
            logger.info(" [SCHEDULER] Análise periódica concluída com sucesso");
        } catch (Exception e) {
            logger.error(" [SCHEDULER] Erro ao executar análise periódica: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Executa agregação diária todos os dias às 2h da manhã
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // Às 2h AM todo dia
    public void aggregateDaily() {
        logger.info(" [SCHEDULER] Iniciando agregação diária - {}", Instant.now());
        
        try {
            long saved = analyticsService.aggregateToLevel("daily");
            logger.info(" [SCHEDULER] Agregação diária concluída: {} registros salvos", saved);
        } catch (Exception e) {
            logger.error(" [SCHEDULER] Erro ao executar agregação diária: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Executa agregação mensal no dia 1 de cada mês às 3h da manhã
     */
    @Transactional
    @Scheduled(cron = "0 0 3 1 * *") // Dia 1 de cada mês às 3h AM
    public void aggregateMonthly() {
        logger.info(" [SCHEDULER] Iniciando agregação mensal - {}", Instant.now());
        
        try {
            long saved = analyticsService.aggregateToLevel("monthly");
            logger.info(" [SCHEDULER] Agregação mensal concluída: {} registros salvos", saved);
        } catch (Exception e) {
            logger.error(" [SCHEDULER] Erro ao executar agregação mensal: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Executa limpeza de dados antigos todos os dias às 3h da manhã
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *") // Às 3h AM todo dia
    public void cleanupOldData() {
        logger.info(" [SCHEDULER] Iniciando limpeza de dados antigos - {}", Instant.now());
        
        try {
            Map<String, Long> deleted = analyticsService.cleanupByLevels();
            logger.info(" [SCHEDULER] Limpeza concluída: {}", deleted);
        } catch (Exception e) {
            logger.error(" [SCHEDULER] Erro ao executar limpeza: {}", e.getMessage(), e);
        }
    }
}
