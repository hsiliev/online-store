FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./gradlew :shop:bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/shop/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
