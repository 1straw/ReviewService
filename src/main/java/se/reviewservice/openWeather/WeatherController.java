//package se.reviewservice.openWeather;
//
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//@CrossOrigin
//@RequestMapping("/api/weather")
//public class WeatherController {
//    private final WeatherService weatherService;
//
//    public WeatherController(WeatherService weatherService) {
//        this.weatherService = weatherService;
//    }
//
//    @GetMapping("/stockholm")
//    public Mono<String> getWeatherStockholm() {
//        String lat = "59.3251172";
//        String lon  = "18.0710935";
//        return weatherService.getWeatherStockholm(lat, lon);
//    }
//
//    @GetMapping("/goteborg")
//    public Mono<String> getWeatherGoteborg() {
//        String lat = "57.422578";
//        String lon  = "11.580443";
//        return weatherService.getWeatherStockholm(lat, lon);
//    }
//
//}
