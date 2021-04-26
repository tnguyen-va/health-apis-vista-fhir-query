package gov.va.api.health.vistafhirquery.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.sentinel.AccessTokens;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VistaConnectivityIT {

  private static String token;

  static Stream<Arguments> connected() {
    return Stream.of(
        arguments("tampa", "1011537977V693883"),
        arguments("also tampa", "1017283148V813263"),
        arguments("also also tampa", "1011537977V693883"));
  }

  @BeforeAll
  static void acquireToken() {
    token = AccessTokens.get().forSystemScopes("system/Observation.read");
  }

  @ParameterizedTest
  @MethodSource
  void connected(String intendedSite, String icn) {
    var sd = SystemDefinitions.systemDefinition().r4();
    RequestSpecification request =
        RestAssured.given()
            .baseUri(sd.url())
            .port(sd.port())
            .relaxedHTTPSValidation()
            .headers(Map.of("Authorization", "Bearer " + token))
            .contentType("application/json")
            .accept("application/json");
    ExpectedResponse response =
        ExpectedResponse.of(
            request.request(Method.GET, sd.apiPath() + "Observation?patient={icn}", icn));
    var bundle = response.expect(200).expectValid(Observation.Bundle.class);
    assertThat(bundle.total()).isGreaterThan(0);
  }
}
