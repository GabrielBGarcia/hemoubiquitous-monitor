package com.ufg.hemoubiquitous_monitor.model;

public class EpidemicNotification {
    private Long casesNumber;
    private String region;
    private String percentage;

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getCasesNumber() {
        return casesNumber;
    }

    public void setCasesNumber(Long casesNumber) {
        this.casesNumber = casesNumber;
    }

    public EpidemicNotification(Long casesNumber, String region, String percentage) {
        this.casesNumber = casesNumber;
        this.region = region;
        this.percentage = percentage;
    }

    public EpidemicNotification() {

    }
}
