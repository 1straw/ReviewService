//package se.reviewservice.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // Tillåt alla endpoints
//                .allowedOrigins("https://happyreview.org") // Tillåt förfrågningar från din domän
//                .allowedMethods("GET", "POST", "PUT", "DELETE") // Vilka HTTP-metoder som är tillåtna
//                .allowedHeaders("*")
//                .allowCredentials(true)
//        ; // Tillåt alla headers
//    }
//}