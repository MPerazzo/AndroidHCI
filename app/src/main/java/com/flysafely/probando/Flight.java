package com.flysafely.probando;


import com.flysafely.probando.search.Airline;
import com.flysafely.probando.search.Arrival;
import com.flysafely.probando.search.Departure;

public class Flight {

    private Integer id;

    private Integer number;

    private String status;

    private Airline airline;

    private Departure departure;

    private Arrival arrival;

    public Flight(Airline airline, Arrival arrival, Departure departure, Integer id, Integer number, String status) {
        this.airline = airline;
        this.arrival = arrival;
        this.departure = departure;
        this.id = id;
        this.number = number;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public Departure getDeparture() {
        return departure;
    }

    public void setDeparture(Departure departure) {
        this.departure = departure;
    }

    public Arrival getArrival() {
        return arrival;
    }

    public void setArrival(Arrival arrival) {
        this.arrival = arrival;
    }
}
