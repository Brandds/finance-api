FROM eclipse-temurin:21-jdk

RUN apt update && apt install -y git

WORKDIR /app

CMD ["./mvnw", "spring-boot:run"]