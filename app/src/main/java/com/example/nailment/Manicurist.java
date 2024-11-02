package com.example.nailment;

import java.io.Serializable;

public class Manicurist implements Serializable {
    private String name;
    private String services;
    private String location;
    private int imageResource;

    public Manicurist(String name, String services, String location, int imageResource) {
        this.name = name;
        this.services = services;
        this.location = location;
        this.imageResource = imageResource;
    }

    public String getName() { return name; }
    public String getServices() { return services; }
    public String getLocation() { return location; }
    public int getImageResource() { return imageResource; }
}
