package com.flysafely.probando.search;

public class City {

    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Country country;

    public City(Country country, String id, Double latitude, Double longitude, String name) {
        this.country = country;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
