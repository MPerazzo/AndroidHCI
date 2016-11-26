package com.flysafely.probando.search;

public class Airport {

    private String id;
    private String description;
    private String time_zone;
    private Double latitude;
    private Double longitude;
    private City city;
    private String terminal;
    private String gate;
    private String baggage;

    public Airport (String description, String id){
        this.description=description;
        this.id=id;
    }

    public Airport(String baggage, City city, String description, String gate, String id, Double latitude, Double longitude, String terminal, String time_zone) {
        this.baggage = baggage;
        this.city = city;
        this.description = description;
        this.gate = gate;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.terminal = terminal;
        this.time_zone = time_zone;
    }

    public String getBaggage() {
        return baggage;
    }

    public void setBaggage(String baggage) {
        this.baggage = baggage;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
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

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getTime_zone() {
        return time_zone;
    }

    public void setTime_zone(String time_zone) {
        this.time_zone = time_zone;
    }

    @Override
    public String toString() {
        return id + " " + description;
    }
}
