package e2e;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoveCardBetweenListsTest {
    private static final String BASE_URL = "https://api.trello.com/1";
    private static final String BOARDS = "/boards/";
    private static final String LISTS = "/lists/";
    private static final String CARDS = "/cards/";

    private static final String KEY = "5184597e2aaf90760735dc7f6fa2b827";
    private static final String TOKEN = "ba39ca42102456119256c3bf0dfad17efb3a93c5bc114c910a43a1a3f256c5e9";

    private static String boardId;
    private static String firstListId;
    private static String secondListId;
    private static String cardId;

    @Test
    @Order(1)
    public void createNewBoard() {
        Response response = given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("name", "My e2e board")
                .queryParam("defaultLists", false)
                .contentType(ContentType.JSON)
        .when()
                .request(Method.POST, BASE_URL+ BOARDS)
        .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("My e2e board");

        boardId = json.getString("id");
    }

    @Test
    @Order(2)
    public void createFirstList() {
        Response response = given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("name", "First list")
                .contentType(ContentType.JSON)
        .when()
                .request(Method.POST, BASE_URL + BOARDS + boardId + LISTS)
        .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("First list");

        firstListId = json.getString("id");
    }

    @Test
    @Order(3)
    public void createSecondList() {
        Response response = given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("name", "Second list")
                .contentType(ContentType.JSON)
        .when()
                .request(Method.POST, BASE_URL + BOARDS + boardId + LISTS)
        .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("Second list");

        secondListId = json.getString("id");
    }

    @Test
    @Order(4)
    public void createCardAndAddItToFirstList() {
        Response response = given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("name", "New card")
                .queryParam("idList", firstListId)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.POST, BASE_URL + CARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("name")).isEqualTo("New card");

        cardId = json.getString("id");
    }

    @Test
    @Order(5)
    public void moveCardToSecondList() {
        Response response = given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("name", "New card")
                .queryParam("idList", secondListId)
                .contentType(ContentType.JSON)
                .when()
                .request(Method.PUT, BASE_URL + CARDS + cardId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        assertThat(json.getString("idList")).isEqualTo(secondListId);
    }

    @Test
    @Order(6)
    public void deleteBoard() {
        given()
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .contentType(ContentType.JSON)
       .when()
                .request(Method.DELETE, BASE_URL + BOARDS + boardId)
       .then()
                .statusCode(200);
    }
}