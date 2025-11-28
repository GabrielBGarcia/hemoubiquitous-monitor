package com.ufg.hemoubiquitous_monitor.domain.patient.model;

public class Address {
    private final String addressLine;
    private final String district;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;
    private final Coordinates coordinates;

    public Address(String addressLine, String district, String city, String state, 
                   String postalCode, String country, Coordinates coordinates) {
        this.addressLine = addressLine;
        this.district = district;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.coordinates = coordinates;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
}
