FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew build -x test --no-daemon

FROM openjdk:21

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar tgparsingbot.jar


ENTRYPOINT ["java", "-jar", "tgparsingbot.jar"]