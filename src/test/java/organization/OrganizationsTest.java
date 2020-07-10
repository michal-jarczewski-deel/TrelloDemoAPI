package organization;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import trello.Organization;

import java.util.stream.Stream;

import static common.SharedMethods.deleteResource;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationsTest extends BaseTest {

    private static String orgId;

    private static Stream<Arguments> createOrganizationData() {
        return Stream.of(
                Arguments.of("Test happy path", "Long description of my organization Long description of my organization", "unique_organization", "https://developer.atlassian.com/"),
                Arguments.of("Need to have at least three letters", "Short description", "abc", "http://localhost:5000"),
                Arguments.of("Special characters !@#$%^&*()", "desc", "can_have_numbers_too123", "https://google.com"),
                Arguments.of("Unique name test", "Random desc", "must_be_unique_organization_name", "http://www.cnn.com"));
    }

    @DisplayName("Create Organization with valid data")
    @ParameterizedTest(name = "Display name: {0}, desc: {1}, name: {2}, website: {3}")
    @MethodSource("createOrganizationData")
    @Test
    public void createNewOrganizationWithAllData(String displayName, String desc, String name, String website) {
        Organization org = new Organization();
        org.setDisplayName(displayName);
        org.setDesc(desc);
        org.setName(name);
        org.setWebsite(website);

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("desc", org.getDesc())
                .queryParam("name", org.getName())
                .queryParam("website", org.getWebsite())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();

        assertThat(json.getString("displayName")).isEqualTo(org.getDisplayName());
        assertThat(json.getString("name")).isEqualTo(org.getName());
        assertThat(json.getString("desc")).isEqualTo(org.getDesc());
        assertThat(json.getString("website")).isEqualTo(org.getWebsite());

        orgId = json.get("id");

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void getExistingOrganizationById() {
        Organization org = new Organization();
        org.setDisplayName("TestOrg");

        Response createResponse = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
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
        assertThat(jsonGet.getString("displayName")).isEqualTo(org.getDisplayName());

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void getProperErrorMessageWhenOrganizationDoesNotExist() {
        orgId = "99";

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
        Organization org = new Organization();
        org.setDisplayName("TestOrg");

        Response response = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = response.jsonPath();
        orgId = json.get("id");

        org.setDisplayName("NewTestOrg");

        Response responsePUT = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .when()
                .put(ORGANIZATIONS + orgId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath jsonPUT = responsePUT.jsonPath();

        assertThat(jsonPUT.getString("id")).isEqualTo(orgId);
        assertThat(jsonPUT.getString("displayName")).isEqualTo(org.getDisplayName());

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void cannotAddOrganizationWithoutName() {
        given()
                .spec(reqSpec)
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotCreateOrganizationWithLessThan3Characters() {
        Organization org = new Organization();
        org.setDisplayName("Test");
        org.setName("aa");
        org.setWebsite("htttp://www.something.com");

        given()
                .spec(reqSpec)
                .queryParam("name", org.getName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotCreateOrganizationWithUpperCasedName() {
        Organization org = new Organization();
        org.setDisplayName("Test");
        org.setName("TEST_ORGANIZATION");

        given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("name", org.getName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotCreateOrganizationWithNotUniqueName() {
        Organization org = new Organization();
        org.setDisplayName("New Organization");
        org.setName("test_board");

        Response firstOrg = given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("name", org.getName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        JsonPath json = firstOrg.jsonPath();
        assertThat(json.getString("name")).isEqualTo(org.getName());

        orgId = json.getString("id");

        given()
                .spec(reqSpec)
                .queryParam("name", org.getName())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);

        deleteResource(ORGANIZATIONS + orgId);
    }

    @Test
    public void cannotCreateOrganizationWithWebsiteNotStartingFromHttpOrHttps() {
        Organization org = new Organization();
        org.setDisplayName("Test");
        org.setWebsite("htttttttt://something.com");

        given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("website", org.getWebsite())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);

        org.setWebsite("www.example.com");

        given()
                .spec(reqSpec)
                .queryParam("displayName", org.getDisplayName())
                .queryParam("website", org.getWebsite())
                .when()
                .post(ORGANIZATIONS)
                .then()
                .statusCode(400);
    }
}