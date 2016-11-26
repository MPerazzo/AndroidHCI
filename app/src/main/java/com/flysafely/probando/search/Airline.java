package com.flysafely.probando.search;

public class Airline {

    String id;
    String name;
    String logo;

    public Airline(String id, String logo, String name) {
        this.id = id;
        this.logo = logo;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
