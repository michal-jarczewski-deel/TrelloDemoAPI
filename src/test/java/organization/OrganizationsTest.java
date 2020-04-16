package organization;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationsTest extends BaseTest {

    private static String orgId;

    @Test
    public void createNewOrganizationWithAllData() {
        String displayName = "Test";
        String desc = "Long description of my organization";
        String name = "unique_organization";
        String website = "https://developer.atlassian.com/";

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", displayName)
                .queryParam("name", name)
                .queryParam("desc", desc)
                .queryParam("website", website)
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("displayName")).isEqualTo(displayName);
        assertThat(json.getString("name")).isEqualTo(name);
        assertThat(json.getString("desc")).isEqualTo(desc);
        assertThat(json.getString("website")).isEqualTo(website);

        orgId = json.get("id");

        given()
                .spec(reqSpec)
                .when()
                .delete(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200);
    }

    @Test
    public void getExistingOrgById() {
        Response createResponse = given()
                .spec(reqSpec)
                .queryParam("displayName", "TestOrg")
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonCreate = createResponse.jsonPath();
        orgId = jsonCreate.get("id");

        Response getResponse = given()
                .spec(reqSpec)
                .when()
                .get(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonGet = getResponse.jsonPath();

        assertThat(jsonGet.getString("id")).isEqualTo(orgId);
        assertThat(jsonGet.getString("displayName")).isEqualTo("TestOrg");

        given()
                .spec(reqSpec)
                .when()
                .delete(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200);
    }

    @Test
    public void getProperErrorMessageWhenOrgDoesNotExist() {
        orgId = "made5up5organization5ID9";

        Response response = given()
                .spec(reqSpec)
                .when()
                .get(ORGANIZATIONS + orgId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        assertThat(response.asString()).isEqualTo("model not found");
    }

    @Test
    public void updateOrganization() {
        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", "TestOrg")
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        orgId = json.get("id");


        Response responsePUT = given()
                .spec(reqSpec)
                .queryParam("displayName", "TestOrg2")
                .when()
                .put(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPUT = responsePUT.jsonPath();

        assertThat(jsonPUT.getString("id")).isEqualTo(orgId);
        assertThat(jsonPUT.getString("displayName")).isEqualTo("TestOrg2");

        given()
                .spec(reqSpec)
                .when()
                .delete(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200);
    }

    @Test
    public void cannotAddOrgWithoutName() {
        given()
                .spec(reqSpec)
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }
}