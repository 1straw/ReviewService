package se.reviewservice.client;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class ClaudeClient {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeClient.class);
    private final WebClient webClient;

    public ClaudeClient(WebClient.Builder builder) {
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.load();
        } catch (Exception e) {
            logger.warn("Could not load .env file, falling back to system environment.");
        }

        String apiKey = dotenv != null ? dotenv.get("ANTHROPIC_API_KEY") : System.getenv("ANTHROPIC_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Anthropic API key is missing!");
        }

        this.webClient = builder
                .baseUrl("https://api.anthropic.com/v1")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    public String askClaude(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "claude-3-haiku-20240307",
                    "max_tokens", 4096,
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    }
            );

            return webClient.post()
                    .uri("/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");
                        if (contentList != null && !contentList.isEmpty()) {
                            return (String) contentList.get(0).get("text");
                        }
                        logger.warn("Claude returned empty content list.");
                        return "No response";
                    })
                    .onErrorReturn("Error calling Claude API")
                    .block();

        } catch (Exception e) {
            logger.error("Exception while calling Claude API", e);
            return "Error calling Claude API";
        }
    }
}