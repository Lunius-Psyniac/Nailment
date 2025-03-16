package com.example.nailment;

import java.io.Serializable;

public class Manicurist implements Serializable {
    private final String name;
    private final String services;
    private final String location;
    private final int imageResource;

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
