package board;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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

        // Remove previously created Trello board
        given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.DELETE, "https://api.trello.com/1/boards/" + boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void createNewBoardsWithEmptyBoardName() {
        given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.POST, "https://api.trello.com/1/boards/")
                .then()
                .statusCode(400);
    }

    @Test
    public void createNewBoardWithoutDefaultLists() {
        Response response = given()
                .queryParam("key", key)
                .queryParam("token", token)
                .queryParam("name", "New Trello board without default lists")
                .queryParam("defaultLists", false)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.POST, "https://api.trello.com/1/boards/")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat((String) json.get("name")).isEqualTo("New Trello board without default lists");

        String boardId = json.get("id");

        // Send GET request to make sure there won't be any lists returned
        Response responseGet = given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.GET, "https://api.trello.com/1/boards/" + boardId + "/lists/")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> idList = jsonGet.getList("id");

        assertThat(idList).hasSize(0);

        // Remove previously created Trello board
        given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.DELETE, "https://api.trello.com/1/boards/" + boardId)
                .then()
                .statusCode(200);
    }

    @Test
    public void createNewBoardWithDefaultLists() {
        Response response = given()
                .queryParam("key", key)
                .queryParam("token", token)
                .queryParam("name", "New Trello board with default lists added")
                .queryParam("defaultLists", true)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.POST, "https://api.trello.com/1/boards/")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat((String) json.get("name")).isEqualTo("New Trello board with default lists added");

        String boardId = json.get("id");

        // Send GET request to make sure there are three lists added to the new board by default
        Response responseGet = given()
                .queryParam("key", key)
                .queryParam("token", token)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.GET, "https://api.trello.com/1/boards/" + boardId + "/lists/")
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> nameList = jsonGet.getList("name");

        assertThat(nameList).hasSize(3).containsExactly("To Do", "Doing", "Done");

        // Remove previously created Trello board
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