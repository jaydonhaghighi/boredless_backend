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

        // 1) Extract the valid JSON portion
        String jsonResponse = extractJsonFromCodeBlock(rawResponse);

        // 2) Parse the JSON into a list of City objects
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<City>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + rawResponse); // Log original response
            throw new RuntimeException("Failed to get top cities for country: " + country, e);
        }
    }


    /**
     * Updated method to request up to 5 top activities per ActivityType.
     */
    public List<Activity> getActivities(City city, List<ActivityType> activityTypes) {
        // Build a comma-separated list of activity types
        String activityTypeList = activityTypes.stream()
                .map(ActivityType::getDisplayName)
                .reduce((type1, type2) -> type1 + ", " + type2)
                .orElse("all types of activities");

        // Define the structured format for activities
        String format = """
            Your response should be a single JSON array of objects.
            Each object must follow this structure:
            {
                "title": "Activity name",
                "type": "Activity type",   // Must match one of the requested activity types
                "bestTime": "Best time to do the activity",
                "cost": "Cost or 'Free'"
            }
            """;

        // Instruct the AI to produce up to 5 top activities *for each* activity type
        String template = """
            For each of the following activity types in {cityName},
            list top 5 recommended activities (fewer if not available).
            Combine all activities for all types into a single JSON array.
            
            Activity Types: {activityTypes}
            {format}
            
            The 'type' field in each object must match the relevant activity type.
            """;

        PromptTemplate promptTemplate = new PromptTemplate(
                template,
                Map.of("cityName", city.getName(), "activityTypes", activityTypeList, "format", format)
        );

        Prompt prompt = new Prompt(promptTemplate.createMessage());

        Generation generation = chatModel.call(prompt).getResult();
        String rawResponse = generation.getOutput().getContent();
        System.out.println("Raw AI response: " + rawResponse);

        // 1) Extract the valid JSON portion
        String jsonResponse = extractJsonFromCodeBlock(rawResponse);

        // 2) Convert to a list of Activity objects
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<Activity>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + rawResponse);
            throw new RuntimeException("Failed to parse AI response into a list of Activity objects.", e);
        }
    }

    /**
     * Extracts valid JSON from an AI response that may contain triple-backtick code blocks (```json ... ```).
     * If no code block is found, returns the entire response trimmed.
     */
    private String extractJsonFromCodeBlock(String rawResponse) {
        String content = rawResponse.trim();

        // If the response includes ```json ... ```
        if (content.contains("```json")) {
            int startIndex = content.indexOf("```json") + "```json".length();
            int endIndex = content.lastIndexOf("```");

            if (startIndex < endIndex) {
                // Extract everything between '```json' and the final '```'
                content = content.substring(startIndex, endIndex).trim();
            }
        }
        // If it doesn't contain "```json", or indexes were invalid, fallback to returning the entire string (trimmed).
        return content;
    }

}
