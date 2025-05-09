FROM amazoncorretto:21.0.0
ENV APP_HOME=/app
WORKDIR $APP_HOME

LABEL version="1.0"
COPY pom.xml .
COPY target/ReviewService-0.0.1-SNAPSHOT.jar $APP_HOME/ReviewService.jar

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ReviewService.jar"]

