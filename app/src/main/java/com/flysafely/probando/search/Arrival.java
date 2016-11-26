package com.flysafely.probando.search;

public class Arrival {

    private Airport airport;

    private String scheduled_time;
    private String actual_time;

    private String scheduled_gate_time;
    private String actual_gate_time;

    private Integer gate_delay;

    private String estimate_runway_time;
    private String actual_runway_time;

    private Integer runway_delay;

    public Arrival(String actual_gate_time, String actual_runway_time, String actual_time, Airport airport, String estimate_runway_time, Integer gate_delay, Integer runway_delay, String scheduled_gate_time, String scheduled_time) {
        this.actual_gate_time = actual_gate_time;
        this.actual_runway_time = actual_runway_time;
        this.actual_time = actual_time;
        this.airport = airport;
        this.estimate_runway_time = estimate_runway_time;
        this.gate_delay = gate_delay;
        this.runway_delay = runway_delay;
        this.scheduled_gate_time = scheduled_gate_time;
        this.scheduled_time = scheduled_time;
    }

    public String getActual_gate_time() {
        return actual_gate_time;
    }

    public void setActual_gate_time(String actual_gate_time) {
        this.actual_gate_time = actual_gate_time;
    }

    public String getActual_runway_time() {
        return actual_runway_time;
    }

    public void setActual_runway_time(String actual_runway_time) {
        this.actual_runway_time = actual_runway_time;
    }

    public String getActual_time() {
        return actual_time;
    }

    public void setActual_time(String actual_time) {
        this.actual_time = actual_time;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public String getEstimate_runway_time() {
        return estimate_runway_time;
    }

    public void setEstimate_runway_time(String estimate_runway_time) {
        this.estimate_runway_time = estimate_runway_time;
    }

    public Integer getGate_delay() {
        return gate_delay;
    }

    public void setGate_delay(Integer gate_delay) {
        this.gate_delay = gate_delay;
    }

    public Integer getRunway_delay() {
        return runway_delay;
    }

    public void setRunway_delay(Integer runway_delay) {
        this.runway_delay = runway_delay;
    }

    public String getScheduled_gate_time() {
        return scheduled_gate_time;
    }

    public void setScheduled_gate_time(String scheduled_gate_time) {
        this.scheduled_gate_time = scheduled_gate_time;
    }

    public String getScheduled_time() {
        return scheduled_time;
    }

    public void setScheduled_time(String scheduled_time) {
        this.scheduled_time = scheduled_time;
    }
}
