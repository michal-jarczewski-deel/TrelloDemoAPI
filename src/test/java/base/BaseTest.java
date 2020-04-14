package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {
    protected static final String BASE_URL = "https://api.trello.com/1";

    protected static final String BOARDS = "/boards/";
    protected static final String LISTS = "/lists/";
    protected static final String CARDS = "/cards/";
    protected static final String KEY = "5184597e2aaf90760735dc7f6fa2b827";
    protected static final String TOKEN = "ba39ca42102456119256c3bf0dfad17efb3a93c5bc114c910a43a1a3f256c5e9";

    protected static RequestSpecBuilder reqSpecBuilder;
    protected static RequestSpecification reqSpec;

    @BeforeAll
    public static void beforeAll() {
        RestAssured.baseURI = BASE_URL;

        reqSpecBuilder = new RequestSpecBuilder();
        reqSpecBuilder.addQueryParam("key", KEY);
        reqSpecBuilder.addQueryParam("token", TOKEN);
        reqSpecBuilder.setContentType(ContentType.JSON);

        reqSpec = reqSpecBuilder.build();
    }
}