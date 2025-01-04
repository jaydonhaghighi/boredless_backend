package org.example.boredless_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.boredless_backend.model.Activity;
import org.example.boredless_backend.model.ActivityType;
import org.example.boredless_backend.model.City;
import org.example.boredless_backend.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/generate")
public class GenAIController {

    private final ChatService chatService;

    public GenAIController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/top-cities")
    public List<City> getTopCities(@RequestParam String country) {
        return chatService.getTopCities(country);
    }

    @PostMapping("/activities")
    public List<Activity> getActivities(@RequestBody Map<String, Object> payload) {
        System.out.println("Payload Received: " + payload); // Debug: Log payload

        // Parse city and activity types from payload
        City city = new ObjectMapper().convertValue(payload.get("city"), City.class);
        List<ActivityType> activityTypes = new ObjectMapper().convertValue(
                payload.get("activityTypes"), new TypeReference<List<ActivityType>>() {}
        );

        System.out.println("City Parsed: " + city); // Debug: Log parsed city
        System.out.println("Activity Types Parsed: " + activityTypes); // Debug: Log parsed activity types

        return chatService.getActivities(city, activityTypes);
    }
}