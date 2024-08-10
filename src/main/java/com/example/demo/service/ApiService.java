package com.example.demo.service;

import com.example.demo.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

public class ApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public <T> T fetchData(String url, TypeReference<T> typeReference) {
        Function<String, T> deserializer = body -> {
            try {
                return objectMapper.readValue(body, typeReference);
            } catch (JsonProcessingException e) {
                throw new ApiException("Error parsing API response", e);
            }
        };
        return fetchDataFromApi(url, deserializer);
    }

    public <T> T fetchData(String url, Class<T> clazz) {
        Function<String, T> deserializer = body -> {
            try {
                return objectMapper.readValue(body, clazz);
            } catch (JsonProcessingException e) {
                throw new ApiException("Error parsing API response", e);
            }
        };
        return fetchDataFromApi(url, deserializer);
    }

    private <T> T fetchDataFromApi(String url, Function<String, T> deserializer) throws ApiException {
        try {
            URI uri = new URI(url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return  deserializer.apply(response.body());
            } else {
                throw new ApiException("Failed to fetch data. HTTP Status: " + response.statusCode());
            }
        } catch (URISyntaxException e) {
            throw new ApiException("Invalid URL: " + url, e);
        } catch (SSLHandshakeException e) {
            throw new ApiException("Seems site is unreachable, try to use VPN. ", e);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Error fetching data from API", e);
        }
    }
}
