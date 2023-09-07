package org.pipeman.books.ai.impl;

import io.javalin.http.ContentType;
import io.javalin.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.pipeman.books.Main;
import org.pipeman.books.utils.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class OpenAI {
    private static final URI API_URL = URI.create("https://api.openai.com/v1/chat/completions");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final JSONPointer RESPONSE_POINTER = new JSONPointer("/choices/0/message/content");

    public static String getCompletion(String input, String token) {
        input = input.replaceAll("(?<= )[A-z\\\\](?= )", "")
                .replaceAll("[@©ı]", "");

        JSONObject body = new JSONObject()
                .put("model", "gpt-3.5-turbo")
                .put("temperature", 0.7)
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", input.trim())
                ));

        HttpRequest request = HttpRequest.newBuilder(API_URL)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .header(Header.CONTENT_TYPE, ContentType.JSON)
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        try {
            Main.getUsageLimiter().reserve(1500);
            HttpResponse<String> response = Utils.tryThis(() -> CLIENT.send(request, BodyHandlers.ofString()));
            JSONObject responseBody = new JSONObject(response.body());

            int usedTokens = responseBody.getJSONObject("usage").getInt("total_tokens");
            Main.getUsageLimiter().use(usedTokens);

            String content = (String) responseBody.query(RESPONSE_POINTER);
            return content.trim();
        } finally {
            Main.getUsageLimiter().deReserve(1500);
        }
    }
}
