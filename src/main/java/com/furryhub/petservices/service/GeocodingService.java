package com.furryhub.petservices.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final String apiKey;

    //Constructor
    public GeocodingService(RestTemplateBuilder builder,
                            @Value("${google.api.key:}") String apiKey) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.apiKey = apiKey != null && !apiKey.isEmpty() ? apiKey : null;
    }

    public Optional<Map<String, Double>> geocode(String address) {

        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        if (apiKey == null || apiKey.isEmpty()) {
            // Return empty if no API key is configured
            return Optional.empty();
        }

        try {
            // URL-encodes the address so spaces and special characters are safe in a query parameter
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);

            //Builds the full Google Geocoding API URL using String.format. We insert the encoded address and the API key.
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    encodedAddress,
                    apiKey
            );

            //Execute a GET request to the built URL and map the JSON response to Jackson’s JsonNode
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }


            //get the full response body and move inside "results"
            JsonNode results = response.getBody().path("results");

            //check if result is array and have size>0
            if (results.isArray() && results.size() > 0) {

                //get the oth index of result and move inside "geometry" and then inside "Location" where long lat is stored
                JsonNode location = results.get(0).path("geometry").path("location");

                //get the lat - lat is inside path
                double lat = location.path("lat").asDouble();

                //get the long - long is inside path
                double lng = location.path("lng").asDouble();

                //store the long/lat in the Map and return
                Map<String, Double> coords = new HashMap<>();
                coords.put("lat", lat);
                coords.put("lon", lng);
                return Optional.of(coords);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    /*
    SAMPLE RESPONSE
    {
  "results": [
    {
      "formatted_address": "Connaught Place, New Delhi, Delhi 110001, India",
      "geometry": {
        "location": {
          "lat": 28.6314513,
          "lng": 77.216667
        },
        "location_type": "APPROXIMATE",
        "viewport": {
          "northeast": { "lat": 28.6328, "lng": 77.2178 },
          "southwest": { "lat": 28.6301, "lng": 77.2155 }
        }
      },
      "place_id": "ChIJN1t_tDeuEmsRUsoyG83frY4",
      "types": ["locality", "political"]
    }
  ],
  "status": "OK"
}
    * */
}