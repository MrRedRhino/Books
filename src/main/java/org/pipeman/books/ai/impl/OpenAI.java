package org.pipeman.books.ai.impl;

import io.javalin.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pipeman.books.utils.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAI {
    private static final URI API_URL = URI.create("https://api.openai.com/v1/chat/completions");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static String getCompletion(String input, String token) {
        JSONObject body = new JSONObject()
                .put("model", "gpt-3.5-turbo")
                .put("temperature", 0.7)
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", input.trim())
                ));

        HttpRequest request = HttpRequest.newBuilder(API_URL)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .method("GET", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = Utils.tryThis(() -> CLIENT.send(request, HttpResponse.BodyHandlers.ofString()));
        JSONObject responseBody = new JSONObject(response.body());

        JSONObject choice = responseBody.getJSONArray("choices").getJSONObject(0);
        String content = choice.getJSONObject("message").getString("content");

        return content.trim();
    }
}
