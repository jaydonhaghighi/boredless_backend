package org.example.boredless_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // Title of the activity
    private String type;         // Type of activity
    private String bestTime;     // Best time to do the activity
    private String cost;         // Cost of the activity

    // Constructors
    public Activity() {
    }

    public Activity(String title, String type, String bestTime, String cost) {
        this.title = title;
        this.type = type;
        this.bestTime = bestTime;
        this.cost = cost;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBestTime() {
        return bestTime;
    }

    public void setBestTime(String bestTime) {
        this.bestTime = bestTime;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
