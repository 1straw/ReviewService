package se.reviewservice;

import se.reviewservice.openWeather.WeatherStockholm;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.reviewservice.openWeather.WeatherStockholm;

@SpringBootApplication
@EnableScheduling
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);


    }

}
