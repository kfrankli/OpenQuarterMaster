package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.profiles.ExternalAuthTestProfile;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.rest.user.TokenCheckResponse;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginRequest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static io.restassured.RestAssured.given;

@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(Auth.class)
class AuthExternalTest extends RunningServerTest {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    TestUserService testUserService;
    @Inject
    JwtService jwtService;

    @Test
    public void testLogin() throws JsonProcessingException {
        User testUser = this.testUserService.getTestUser(false, false);

        UserLoginRequest ulr = new UserLoginRequest(testUser.getEmail(), testUser.getAttributes().get(TestUserService.TEST_PASSWORD_ATT_KEY), true);

        String errorMessage = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ulr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .extract().body().toString();

        log.info("Error Message: {}", errorMessage);
    }

    @Test
    public void testTokenCheck() throws JsonProcessingException {
        User testUser = this.testUserService.getExternalTestUser();

        UserLoginResponse token = this.jwtService.getUserJwt(testUser, false);

        log.info("Token from external: {}", token);

        TokenCheckResponse response = setupJwtCall(
                given(),
                token.getToken()
        )
                .contentType(ContentType.JSON)
                .when()
                .get("tokenCheck")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().as(TokenCheckResponse.class);

        log.info("Token Check Response: {}", response);
    }
}