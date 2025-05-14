package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClientConnecter {
    private final String baseUrl;
    private final String username;
    private final String password;
    private final HttpClient client;
    private final String params;

    public ClientConnecter(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.client = HttpClient.newHttpClient();
        this.params = "?username=" + username + "&password=" + password;
    }

    public List<SessionSummary> listSessions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/sessions/" + username + "?password=" + password))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return JsonUtil.GSON.fromJson(response.body(), new TypeToken<List<SessionSummary>>() {
        }.getType());
    }

    public GameState newGame(UUID sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + sessionId + "/reset" + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return GameState.fromJson(response.body());
    }

    public void finishGame(UUID sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + sessionId + "/finish" + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, BodyHandlers.ofString());
    }

    public GameState startGame() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/start" + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        System.out.println("Request: " + request);
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return GameState.fromJson(response.body());
    }

    public GameState placeBet(UUID sessionId, int amount) throws Exception {
        String uriString = baseUrl + "/" + sessionId + "/bet/" + amount + params;
        System.out.println("[ClientConnecter] Attempting to place bet. Request URI: " + uriString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriString))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("[ClientConnecter] Bet request sent. Response status code: " + response.statusCode());
            String responseBody = response.body();
            System.out.println("[ClientConnecter] Raw response body for bet: " + responseBody);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return GameState.fromJson(responseBody);
            } else {
                // Handle non-successful HTTP responses
                System.err.println("[ClientConnecter] Error placing bet. HTTP Status: " + response.statusCode()
                        + ", Body: " + responseBody);
                // Optionally, throw a more specific custom exception or handle as per
                // application needs
                throw new Exception("Server returned error: " + response.statusCode() + " - " + responseBody);
            }
        } catch (Exception e) {
            System.err.println("[ClientConnecter] Exception during placeBet: " + e.getMessage());
            e.printStackTrace(); // Ensure stack trace is printed from here
            throw e; // Re-throw the exception to be caught by the GUI layer if needed
        }
    }

    public GameState hit(UUID sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + sessionId + "/hit" + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return GameState.fromJson(response.body());
    }

    public GameState stand(UUID sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + sessionId + "/stand" + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return GameState.fromJson(response.body());
    }

    public GameState resumeSession(UUID sessionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/resume/" + sessionId + params))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        return new Gson().fromJson(response.body(), GameState.class);
    }
}
