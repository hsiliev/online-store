FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./gradlew :store:bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/store/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
