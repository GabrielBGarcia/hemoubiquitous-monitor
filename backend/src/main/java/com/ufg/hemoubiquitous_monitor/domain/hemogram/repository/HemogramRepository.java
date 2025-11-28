package com.ufg.hemoubiquitous_monitor.domain.hemogram.repository;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.HemogramAnalysis;

public interface HemogramRepository {
    HemogramAnalysis save(HemogramAnalysis analysis);
}
