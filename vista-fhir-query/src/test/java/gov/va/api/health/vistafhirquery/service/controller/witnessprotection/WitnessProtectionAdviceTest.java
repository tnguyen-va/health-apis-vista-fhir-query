package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WitnessProtectionAdviceTest {

  @Mock IdentityService identityService;

  @Test
  public void bundleOfMixedResourcesIsProtected() {
    var f11 = FugaziOne.builder().id("private-f11").build();
    var f12 = FugaziOne.builder().id("private-f12").build();
    var f21 = FugaziTwo.builder().id("private-f21").build();
    var f22 = FugaziTwo.builder().id("private-f22").build();
    FugaziBundle bundle =
        new FugaziBundle(
            List.of(
                new FugaziEntry(f11),
                new FugaziEntry(f12),
                new FugaziEntry(f21),
                new FugaziEntry(f22)));

    when(identityService.register(any()))
        .thenReturn(
            List.of(
                registration("FugaziOne", "f11"),
                registration("FugaziOne", "f12"),
                registration("FugaziTwo", "f21"),
                registration("FugaziTwo", "f22")));

    wp().protect(bundle);
    assertThat(f11.id()).isEqualTo("public-f11");
    assertThat(f12.id()).isEqualTo("public-f12");
    assertThat(f21.id()).isEqualTo("public-f21");
    assertThat(f22.id()).isEqualTo("public-f22");
  }

  private Registration registration(String resource, String baseId) {
    return Registration.builder()
        .uuid("public-" + baseId)
        .resourceIdentities(
            List.of(
                ResourceIdentity.builder()
                    .system("VISTA")
                    .resource(resource)
                    .identifier("private-" + baseId)
                    .build()))
        .build();
  }

  @Test
  public void singleResourceIsProtected() {
    when(identityService.register(any())).thenReturn(List.of(registration("FugaziOne", "f1")));
    var f1 = FugaziOne.builder().id("private-f1").build();
    wp().protect(f1);
    assertThat(f1.id()).isEqualTo("public-f1");

    when(identityService.register(any())).thenReturn(List.of(registration("FugaziTwo", "f2")));
    var f2 = FugaziTwo.builder().id("private-f2").build();
    wp().protect(f2);
    assertThat(f2.id()).isEqualTo("public-f2");
  }

  @Test
  void unknownResourceTypeIsNotModified() {
    var f1 = FugaziOne.builder().id("private-f1").build();
    var noF1 =
        WitnessProtectionAdvice.builder()
            .identityService(identityService)
            .availableAgent(new FugaziTwoAgent())
            .build();
    noF1.protect(f1);
    assertThat(f1.id()).isEqualTo("private-f1");
  }

  @Test
  void unregisteredResourcesAreNotModified() {
    var f1 = FugaziOne.builder().id("private-f1").build();
    wp().protect(f1);
    assertThat(f1.id()).isEqualTo("private-f1");
  }

  private WitnessProtectionAdvice wp() {
    return WitnessProtectionAdvice.builder()
        .identityService(identityService)
        .availableAgent(new FugaziOneAgent())
        .availableAgent(new FugaziTwoAgent())
        .build();
  }

  static class FugaziBundle extends AbstractBundle<FugaziEntry> {
    FugaziBundle(List<FugaziEntry> e) {
      entry(e);
    }
  }

  static class FugaziEntry extends AbstractEntry<Resource> {
    FugaziEntry(Resource r) {
      resource(r);
    }
  }

  @Data
  @Builder
  static class FugaziOne implements Resource {
    String id;
    String implicitRules;
    String language;
    Meta meta;
  }

  static class FugaziOneAgent implements WitnessProtectionAgent<FugaziOne> {

    @Override
    public Stream<ProtectedReference> referencesOf(FugaziOne resource) {
      return Stream.of(ProtectedReference.forResource(resource, resource::id));
    }
  }

  @Data
  @Builder
  static class FugaziTwo implements Resource {
    String id;
    String implicitRules;
    String language;
    Meta meta;
  }

  static class FugaziTwoAgent implements WitnessProtectionAgent<FugaziTwo> {
    @Override
    public Stream<ProtectedReference> referencesOf(FugaziTwo resource) {
      return Stream.of(ProtectedReference.forResource(resource, resource::id));
    }
  }
}
