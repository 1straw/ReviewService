package se.reviewservice.openWeather;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {
    private final WebClient webClient;
    private final String apiKey;


    public WeatherService(WebClient webClient) {
        this.webClient = webClient;

        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.load();
        } catch (Exception ignored) {
            // Dotenv kanske inte finns i containern
        }
        String apiKey = dotenv != null ? dotenv.get("OPEN_WEATHER_API_KEY") : null;
//        Dotenv dotenv = Dotenv.load();
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPEN_WEATHER_API_KEY");
        }

        // Om .env saknar API-nyckeln, hämta den från systemets miljövariabler
        this.apiKey = apiKey != null ? apiKey : System.getenv("OPEN_WEATHER_API_KEY");

    }

    public Mono<String> getWeatherStockholm(String lat, String lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .map(response -> {
                    if (response.getWeather() != null && !response.getWeather().isEmpty()) {
                        return response.getWeather().get(0).getDescription();
                    } else {
                        return "No weather data";
                    }
                });
    }
}

