package org.example.boredless_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.boredless_backend.model.Activity;
import org.example.boredless_backend.model.ActivityType;
import org.example.boredless_backend.model.City;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final ChatModel chatModel;
    private final ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());
    private final ObjectMapper objectMapper;

    public ChatService(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public List<City> getTopCities(String country) {
        // Define the structured format for cities
        String format = """
                        Your response should be a JSON array of objects, with each object adhering to this structure:
                        {
                            "name": "City name",
                            "country": "Country name",
                            "description": "Brief description of the city"
                        }
                        """;

        // Create a dynamic prompt
        String template = """
                          List the top 5 cities to visit in {country}.
                          {format}
                          """;

        PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("country", country, "format", format));
        Prompt prompt = new Prompt(promptTemplate.createMessage());

        // Call the AI model and process the response
        Generation generation = chatModel.call(prompt).getResult();
        String rawResponse = generation.getOutput().getContent();
        System.out.println("Raw AI response: " + rawResponse);

        // Preprocess the response to extract the JSON part
        String jsonResponse = rawResponse;
        if (rawResponse.contains("```json")) {
            jsonResponse = rawResponse.substring(rawResponse.indexOf("```json") + 7, rawResponse.lastIndexOf("```")).trim();
        }

        // Convert the AI's response into a list of City objects
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<City>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + rawResponse); // Log original response for debugging
            throw new RuntimeException("Failed to get top cities for country: " + country, e);
        }
    }

    public List<Activity> getActivities(City city, List<ActivityType> activityTypes) {
        // Build a comma-separated list of activity types
        String activityTypeList = activityTypes.stream()
                .map(ActivityType::getDisplayName)
                .reduce((type1, type2) -> type1 + ", " + type2)
                .orElse("all types of activities");

        // Define the structured format for activities
        String format = """
        Your response should be a JSON array of objects, with each object adhering to this structure:
        {
            "title": "Activity name",
            "type": "Activity type",
            "bestTime": "Best time to do the activity",
            "cost": "Cost or 'Free'"
        }
        Each activity type should include 5 unique activities.
        """;

        // Create a dynamic prompt
        String template = """
        For the city of {cityName}, generate 5 unique activities for each of the following types: {activityTypes}.
        {format}
        """;

        PromptTemplate promptTemplate = new PromptTemplate(
                template,
                Map.of("cityName", city.getName(), "activityTypes", activityTypeList, "format", format)
        );

        Prompt prompt = new Prompt(promptTemplate.createMessage());

        // Call the AI model and process the response
        Generation generation = chatModel.call(prompt).getResult();
        String rawResponse = generation.getOutput().getContent();
        System.out.println("Raw AI response: " + rawResponse);

        // Trim backticks if present
        if (rawResponse.startsWith("```json")) {
            rawResponse = rawResponse.substring(7, rawResponse.length() - 3).trim();
        }

        // Convert the AI's response into a list of Activity objects
        try {
            return objectMapper.readValue(rawResponse, new TypeReference<List<Activity>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + rawResponse); // Debugging
            throw new RuntimeException("Failed to parse AI response into a list of Activity objects.", e);
        }
    }
}

