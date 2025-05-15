//package se.reviewservice.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KeystoreLogger {
//    private static final Logger logger = LoggerFactory.getLogger(KeystoreLogger.class);
//
//    @Value("${server.ssl.key-store}")
//    private String keyStorePath;
//
//    @PostConstruct
//    public void logKeyStorePath() {
//        logger.info("SSL keystore path: {}", keyStorePath);
//    }
//}
//
