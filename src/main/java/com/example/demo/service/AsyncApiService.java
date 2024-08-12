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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Service class for making API requests and handling responses.
 * Supports fetching and deserializing data from external APIs asynchronously.
 */
public class AsyncApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AsyncApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Asynchronously fetches data from the specified URL and deserializes it into the specified type.
     *
     * @param url           The URL to fetch data from
     * @param typeReference TypeReference for complex types
     * @param <T>           The type of object to deserialize into
     * @return A CompletableFuture that will contain the deserialized object of type T
     */
    public <T> CompletableFuture<T> fetchDataAsync(String url, TypeReference<T> typeReference) {
        Function<String, T> deserializer = body -> {
            try {
                return objectMapper.readValue(body, typeReference);
            } catch (JsonProcessingException e) {
                throw new ApiException("Error parsing API response", e);
            }
        };
        return fetchDataFromApiAsync(url, deserializer);
    }

    /**
     * Asynchronously fetches data from the specified URL and deserializes it into the specified type.
     *
     * @param url   The URL to fetch data from
     * @param clazz The class type to deserialize into
     * @param <T>   The type of object to deserialize into
     * @return A CompletableFuture that will contain the deserialized object of type T
     */
    public <T> CompletableFuture<T> fetchDataAsync(String url, Class<T> clazz) {
        Function<String, T> deserializer = body -> {
            try {
                return objectMapper.readValue(body, clazz);
            } catch (JsonProcessingException e) {
                throw new ApiException("Error parsing API response", e);
            }
        };
        return fetchDataFromApiAsync(url, deserializer);
    }

    /**
     * Asynchronously fetches data from the specified API endpoint and deserializes it using the provided deserializer function.
     *
     * @param <T>           The type of the data to be returned.
     * @param url           The URL of the API endpoint to fetch data from.
     * @param deserializer  A function that takes a JSON string and converts it to an instance of type T.
     * @return A CompletableFuture that will contain the deserialized object of type T.
     * @throws ApiException If there is an error in the API request, such as an invalid URL,
     *                      an unsuccessful HTTP status code, or a network issue.
     */
    private <T> CompletableFuture<T> fetchDataFromApiAsync(String url, Function<String, T> deserializer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URI(url);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return deserializer.apply(response.body());
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
        });
    }
}

