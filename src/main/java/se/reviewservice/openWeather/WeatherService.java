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

        // Försök hämta API-nyckeln från .env
        Dotenv dotenv = Dotenv.load();
        String envApiKey = dotenv.get("OPEN_WEATHER_API_KEY");

        // Om .env saknar API-nyckeln, hämta den från systemets miljövariabler
        this.apiKey = envApiKey != null ? envApiKey : System.getenv("OPEN_WEATHER_API_KEY");

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

