package board;

import base.BaseTest;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static common.SharedMethods.deleteResource;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardTest extends BaseTest {

    @Test
    public void createNewBoard() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "First board created from Rest Assured")
                .when()
                .request(Method.POST, BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertEquals("First board created from Rest Assured", json.get("name"));

        String boardId = json.get("id");

        // Remove previously created Trello board
        deleteResource(BOARDS + boardId);
    }

    @Test
    public void createNewBoardsWithEmptyBoardName() {
        given()
                .spec(reqSpec)
                .when()
                .request(Method.POST, BOARDS)
                .then()
                .statusCode(400);
    }

    @Test
    public void createNewBoardWithoutDefaultLists() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "New Trello board without default lists")
                .queryParam("defaultLists", false)
                .when()
                .request(Method.POST, BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat((String) json.get("name")).isEqualTo("New Trello board without default lists");

        String boardId = json.get("id");

        // Send GET request to make sure there won't be any lists returned
        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .request(Method.GET, BOARDS + boardId + LISTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> idList = jsonGet.getList("id");

        assertThat(idList).hasSize(0);

        // Remove previously created Trello board
        deleteResource(BOARDS + boardId);
    }

    @Test
    public void createNewBoardWithDefaultLists() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("name", "New Trello board with default lists added")
                .queryParam("defaultLists", true)
                .when()
                .request(Method.POST, BOARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat((String) json.get("name")).isEqualTo("New Trello board with default lists added");

        String boardId = json.get("id");

        // Send GET request to make sure there are three lists added to the new board by default
        Response responseGet = given()
                .spec(reqSpec)
                .when()
                .request(Method.GET, BOARDS + boardId + LISTS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = responseGet.jsonPath();
        List<String> nameList = jsonGet.getList("name");

        assertThat(nameList).hasSize(3).containsExactly("To Do", "Doing", "Done");

        // Remove previously created Trello board
        deleteResource(BOARDS + boardId);
    }
}