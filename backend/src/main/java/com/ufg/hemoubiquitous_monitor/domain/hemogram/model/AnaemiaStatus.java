package com.ufg.hemoubiquitous_monitor.domain.hemogram.model;

import java.time.Instant;

public class AnaemiaStatus {
    private final boolean hasAnaemia;
    private final Instant analyzedAt;

    public AnaemiaStatus(boolean hasAnaemia, Instant analyzedAt) {
        this.hasAnaemia = hasAnaemia;
        this.analyzedAt = analyzedAt;
    }

    public boolean hasAnaemia() {
        return hasAnaemia;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }
}
