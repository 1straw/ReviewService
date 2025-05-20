package se.reviewservice.openWeather;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PrintWeatherService {
    private final WeatherService weatherService;

    public PrintWeatherService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // Körs en gång 2 sekunder efter start
    @Scheduled(initialDelay = 2000, fixedDelay = Long.MAX_VALUE)
    public void printWeatherStockholm() {
        String lat = "59.3251172";
        String lon = "18.0710935";
        weatherService.getWeatherStockholm(lat, lon)
                .subscribe(description -> System.out.println("Vädret i Stockholm: " + description));
    }
}