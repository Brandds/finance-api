FROM eclipse-temurin:21-jdk

WORKDIR /app

CMD ["./mvnw", "spring-boot:run"]