package gov.va.api.health.vistafhirquery.service.controller.witnessprotection;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProtectedReference {
  private final String type;

  private final String id;

  private final Consumer<String> onUpdate;

  public static Optional<ProtectedReference> forReference(Reference reference) {
    if (isBlank(reference.reference())) {
      return Optional.empty();
    }
    String[] parts = reference.reference().split("/", -1);
    if (parts.length < 2) {
      return Optional.empty();
    }
    String resource = parts[parts.length - 2];
    String id = parts[parts.length - 1];
    if (isBlank(resource) || isBlank(id)) {
      return Optional.empty();
    }
    return Optional.of(
        ProtectedReference.builder()
            .type(resource)
            .id(id)
            .onUpdate(
                s -> {
                  parts[parts.length - 1] = s;
                  reference.reference(String.join("/", parts));
                })
            .build());
  }

  public static ProtectedReference forResource(Resource resource, Consumer<String> onUpdate) {
    return ProtectedReference.builder()
        .type(resource.getClass().getSimpleName())
        .id(resource.id())
        .onUpdate(onUpdate)
        .build();
  }

  public Optional<ResourceIdentity> asResourceIdentity() {
    return Optional.of(
        ResourceIdentity.builder().system("VISTA").resource(type).identifier(id).build());
  }

  public void updateId(Optional<String> id) {
    onUpdate.accept(id.orElse(null));
  }
}
