package common;

import base.BaseTest;

import static io.restassured.RestAssured.given;

public class SharedMethods extends BaseTest {

    public static void deleteResource(String URL) {
        given()
                .spec(reqSpec)
                .when()
                .delete(URL)
                .then()
                .statusCode(200);
    }
}