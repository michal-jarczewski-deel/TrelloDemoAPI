package common;

import base.BaseTest;

import static io.restassured.RestAssured.given;

public class SharedMethods extends BaseTest {

    public static void cleanUp(String URL) {
        given()
                .spec(reqSpec)
                .when()
                .delete(URL)
                .then()
                .statusCode(200);
    }
}