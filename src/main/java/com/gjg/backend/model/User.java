package com.gjg.backend.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class User {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(unique = true, name = "display_name")
    private String displayName;
    private String country;
    private double points;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String display_name) {
        this.displayName = display_name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
}
