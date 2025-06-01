package se.reviewservice.client;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeClient {

    private final WebClient webClient;

    public ClaudeClient(WebClient.Builder builder) {
//        Dotenv dotenv = Dotenv.load();
//        String apiKey = dotenv.get("ANTHROPIC_API_KEY");
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.load();
        } catch (Exception ignored) {
            // Dotenv kanske inte finns i containern
        }

        String apiKey = dotenv != null ? dotenv.get("ANTHROPIC_API_KEY") : null;

        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("ANTHROPIC_API_KEY");
        }


        this.webClient = builder
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    public String askClaude(String prompt) {
        Map<String, Object> body = Map.of(
                "model", "claude-3-haiku-20240307",
                "max_tokens", 4096,
                "messages", new Object[] {
                        Map.of("role", "user", "content", prompt)
                }
        );

        return webClient.post()
                .uri("/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> System.out.println("Claude response: " + response))
                .map(response -> {
                    var contentList = (List<Map<String, Object>>) response.get("content");
                    if (contentList != null && !contentList.isEmpty()) {
                        return (String) contentList.get(0).get("text");
                    }
                    return "No response";
                })
                .doOnError(error -> {
                    System.err.println("Claude API error:");
                    ((Throwable) error).printStackTrace();
                })
                .onErrorReturn("Error calling Claude API")
                .block();
    }
}