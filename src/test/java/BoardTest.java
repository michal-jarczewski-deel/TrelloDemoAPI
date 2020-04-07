import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardTest {
    private final String key = "5184597e2aaf90760735dc7f6fa2b827";
    private final String token = "ba39ca42102456119256c3bf0dfad17efb3a93c5bc114c910a43a1a3f256c5e9";

    @Test
    public void createNewBoard() {
        Response response = given()
                .queryParam("key", key)
                .queryParam("token", token)
                .queryParam("name", "First board created from Rest Assured")
                .contentType(ContentType.JSON)
                .when()
                .request(Method.POST, "https://api.trello.com/1/boards/")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertEquals("First board created from Rest Assured", json.get("name"));

        String boardId = json.get("id");

        given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.DELETE, "https://api.trello.com/1/boards/" + boardId)
                .then()
                .statusCode(200);
    }
}