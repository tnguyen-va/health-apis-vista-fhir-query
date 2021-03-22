package gov.va.api.health.vistafhirquery.service.controller.ping;

import gov.va.api.health.vistafhirquery.service.controller.VistalinkApiClient;
import gov.va.api.lighthouse.charon.api.RpcDetails;
import gov.va.api.lighthouse.charon.api.RpcInvocationResult;
import gov.va.api.lighthouse.charon.api.RpcResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/internal"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class InternalPingController {

  private final VistalinkApiClient vistalinkApiClient;

  /** Ping All VistA sites for a given patient. */
  @SneakyThrows
  @GetMapping(value = "/ping/{icn}")
  public List<RpcInvocationResult> ping(@PathVariable("icn") String icn) {
    RpcResponse response =
        vistalinkApiClient.requestForPatient(
            icn,
            RpcDetails.builder().name("XOBV TEST PING").context("XOBV VISTALINK TESTER").build());
    return response.results();
  }
}