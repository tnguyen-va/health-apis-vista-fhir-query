package gov.va.api.health.vistafhirquery.service.controller;

import static gov.va.api.lighthouse.charon.api.RpcResponse.Status.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.vistafhirquery.service.config.VistaApiConfig;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.RpcInvocationResult;
import gov.va.api.lighthouse.charon.api.RpcResponse;
import gov.va.api.lighthouse.charon.models.TypeSafeRpcRequest;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestVistaApiClientTest {
  RestTemplate rt = mock(RestTemplate.class);

  VistaApiConfig config =
      VistaApiConfig.builder()
          .url("http://fugazi.com/")
          .accessCode("ac")
          .verifyCode("vc")
          .clientKey("ck")
          .build();

  private RestVistaApiClient client() {
    return RestVistaApiClient.builder().config(config).restTemplate(rt).build();
  }

  void mockVistalink200Response() {
    when(rt.exchange(any(), eq(RpcResponse.class)))
        .thenReturn(
            ResponseEntity.status(200)
                .body(
                    RpcResponse.builder()
                        .status(RpcResponse.Status.OK)
                        .results(
                            List.of(
                                RpcInvocationResult.builder()
                                    .vista("1")
                                    .response("SUCCESS")
                                    .build()))
                        .build()));
  }

  void mockVistalink500Response() {
    when(rt.exchange(any(), eq(RpcResponse.class)))
        .thenReturn(
            ResponseEntity.status(500)
                .body(
                    RpcResponse.builder()
                        .status(FAILED)
                        .results(
                            List.of(
                                RpcInvocationResult.builder()
                                    .vista("1")
                                    .error(Optional.of("OOF"))
                                    .build()))
                        .build()));
  }

  @Test
  void requestForPatientWithVistalink200Response() {
    mockVistalink200Response();
    assertThat(client().requestForPatient("p1", FauxRpc.create()))
        .isEqualTo(
            RpcResponse.builder()
                .status(RpcResponse.Status.OK)
                .results(
                    List.of(RpcInvocationResult.builder().vista("1").response("SUCCESS").build()))
                .build());
  }

  @Test
  void requestForPatientWithVistalink500Response() {
    mockVistalink500Response();
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> client().requestForPatient("p1", FauxRpc.create()));
  }

  @Test
  void requestForVistaSiteWithVistalink200Response() {
    mockVistalink200Response();
    assertThat(client().requestForVistaSite("123", FauxRpc.create()))
        .isEqualTo(
            RpcResponse.builder()
                .status(RpcResponse.Status.OK)
                .results(
                    List.of(RpcInvocationResult.builder().vista("1").response("SUCCESS").build()))
                .build());
  }

  @Test
  void requestForVistaSiteWithVistalink500Response() {
    mockVistalink500Response();
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> client().requestForVistaSite("123", FauxRpc.create()));
  }

  @NoArgsConstructor(staticName = "create")
  static class FauxRpc implements TypeSafeRpcRequest {
    private Optional<String> context;

    @Override
    public RpcDetails asDetails() {
      return RpcDetails.builder().name("FAUX RPC").context("FAUX CONTEXT").build();
    }
  }
}
