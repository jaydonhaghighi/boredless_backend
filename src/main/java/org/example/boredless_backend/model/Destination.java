package org.example.boredless_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Trip journey;

    private String country;

    @ManyToOne
    private City city;

    private int numOfDays;

    public Destination() {} // default constructor

    public Destination(String country, City city, int numOfDays) {
        this.country = country;
        this.city = city;
        this.numOfDays = numOfDays;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
