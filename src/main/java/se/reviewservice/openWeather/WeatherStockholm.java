package se.reviewservice.openWeather;

import org.springframework.stereotype.Service;

@Service
public class WeatherStockholm {
    private final WeatherService weatherService;

    public WeatherStockholm(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void printStockholmWeather() {
        String lat = "59.3251172";
        String lon = "18.0710935";
        weatherService.getWeatherStockholm(lat, lon)
                .subscribe(weather -> {
                    System.out.println("Vädret i Stockholm: " + weather);
                });
    }
}
