package com.onlinestore.it;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

@Testcontainers
public class DeficitIT {

    @Container
    public static ComposeContainer environment =
            new ComposeContainer(new File("../docker-compose.yml"))
                    .withExposedService("shop", 8082, Wait.forHttp("/actuator/health").forStatusCode(200))
                    .withExposedService("store", 8081, Wait.forHttp("/actuator/health").forStatusCode(200))
                    .withLocalCompose(true);

    private static String shopUrl;
    private static String storeUrl;

    @BeforeAll
    static void setup() {
        String shopHost = environment.getServiceHost("shop", 8082);
        Integer shopPort = environment.getServicePort("shop", 8082);
        shopUrl = "http://" + shopHost + ":" + shopPort;

        String storeHost = environment.getServiceHost("store", 8081);
        Integer storePort = environment.getServicePort("store", 8081);
        storeUrl = "http://" + storeHost + ":" + storePort;
    }

    @Test
    void testDeficitFlow() {
        // Stock 3 chocolates in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 1, "productName", "chocolate", "quantity", 3))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);

        // Stock 4 bananas in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 2, "productName", "banana", "quantity", 4))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);

        // Order 1 chocolate and 1 banana
        given()
            .contentType(ContentType.JSON)
            .body(List.of(
                Map.of("productId", 1, "quantity", 1),
                Map.of("productId", 2, "quantity", 1)
            ))
            .post(shopUrl + "/order")
            .then()
            .statusCode(202);

        // Check the stock is 2 chocolates and 3 bananas in the Store
        given()
            .get(storeUrl + "/stock")
            .then()
            .statusCode(200)
            .body("find { it.id == 1 }.name", is("chocolate"))
            .body("find { it.id == 1 }.quantity", is(2))
            .body("find { it.id == 2 }.name", is("banana"))
            .body("find { it.id == 2 }.quantity", is(3));

        // Order 10 chocolates
        given()
            .contentType(ContentType.JSON)
            .body(List.of(Map.of("productId", 1, "quantity", 10)))
            .post(shopUrl + "/order")
            .then()
            .statusCode(202);

        // Order 10 chocolates and 10 bananas
        given()
            .contentType(ContentType.JSON)
            .body(List.of(
                Map.of("productId", 1, "quantity", 10),
                Map.of("productId", 2, "quantity", 10)
            ))
            .post(shopUrl + "/order")
            .then()
            .statusCode(202);

        // Check that demand is 18 chocolates and 7 bananas
        given()
            .get(storeUrl + "/demand")
            .then()
            .statusCode(200)
            .body("find { it.productId == 1 }.quantityInDemand", is(18))
            .body("find { it.productId == 2 }.quantityInDemand", is(7));

        // Stock 10 chocolates in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 1, "quantity", 10))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);
        // Stock 5 bananas in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 2, "quantity", 5))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);

        // wait for event processing
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            given()
                .get(storeUrl + "/stock")
                .then()
                .statusCode(200)
                .body("find { it.id == 1 }.quantity", is(2))
                .body("find { it.id == 2 }.quantity", is(8))
        );

        // Check that demand is 8 chocolates and 2 bananas
        given()
            .get(storeUrl + "/demand")
            .then()
            .statusCode(200)
            .body("find { it.productId == 1 }.quantityInDemand", is(8))
            .body("find { it.productId == 2 }.quantityInDemand", is(2));

        // Stock 10 chocolates in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 1, "quantity", 10))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);
        // Stock 5 bananas in the Store
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("productId", 2, "quantity", 5))
            .post(storeUrl + "/stock")
            .then()
            .statusCode(200);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            given()
                .get(storeUrl + "/demand")
                .then()
                .statusCode(200)
                .body("$", hasSize(0))
        );

        // Check the stock is 2 chocolates and 3 bananas in the Store
        given()
            .get(storeUrl + "/stock")
            .then()
            .statusCode(200)
            .body("find { it.id == 1 }.quantity", is(2))
            .body("find { it.id == 2 }.quantity", is(3));
    }
}
