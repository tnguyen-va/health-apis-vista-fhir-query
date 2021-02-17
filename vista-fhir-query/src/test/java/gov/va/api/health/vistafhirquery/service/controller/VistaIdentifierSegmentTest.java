package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.ids.client.EncodingIdentityServiceClient;
import gov.va.api.health.ids.client.EncryptingIdEncoder;
import gov.va.api.health.ids.client.EncryptingIdEncoder.Codebook;
import gov.va.api.health.vistafhirquery.service.controller.VistaIdentifierSegment.PatientIdentifierType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class VistaIdentifierSegmentTest {
  @Test
  void invalidPatientIdentifierTypeThrowsIllegalArgument() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> VistaIdentifierSegment.PatientIdentifierType.fromAbbreviation('Z'));
  }

  @Test
  void packNumberFormat() {
    VistaIdentifierSegment vis =
        VistaIdentifierSegment.builder()
            .patientIdentifierType(PatientIdentifierType.NATIONAL_ICN)
            .patientIdentifier("1011537977V693883")
            .vistaSiteId("673")
            .vistaRecordId("32463")
            .build();
    String data = vis.pack();
    assertThat(data.charAt(0)).isEqualTo('n');
    assertThat(VistaIdentifierSegment.unpack(data)).isEqualTo(vis);
    EncodingIdentityServiceClient ids =
        EncodingIdentityServiceClient.builder()
            .delegate(null)
            .encoder(
                EncryptingIdEncoder.builder()
                    .password("secret")
                    .codebook(Codebook.builder().map(List.of()).build())
                    .build())
            .patientIdPattern("abc")
            .build();
    List<Registration> register =
        ids.register(
            List.of(
                ResourceIdentity.builder().system("V").resource("Ob").identifier(data).build()));
    log.warn(
        "{} -> {} -> {}",
        vis.toIdentifierSegment().length(),
        data.length(),
        register.get(0).uuid().length());
  }

  @ParameterizedTest
  @ValueSource(strings = {"x+123+abc", "+123+abc", "123", "123+abc", "123+abc+456+def"})
  void parseInvalidSegmentThrowsIllegalArgument(String segment) {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> VistaIdentifierSegment.unpack(segment));
  }

  @ParameterizedTest
  @EnumSource(value = VistaIdentifierSegment.PatientIdentifierType.class)
  void patientIdentifierTypeRoundTrip(VistaIdentifierSegment.PatientIdentifierType value) {
    var shortened = value.abbreviation();
    var fullLength = VistaIdentifierSegment.PatientIdentifierType.fromAbbreviation(shortened);
    assertThat(fullLength).isEqualTo(value);
  }

  @Test
  void toIdentiferSegment() {
    assertThat(
            VistaIdentifierSegment.builder()
                .patientIdentifierType(VistaIdentifierSegment.PatientIdentifierType.NATIONAL_ICN)
                .patientIdentifier("1011537977V693883")
                .vistaSiteId("673")
                .vistaRecordId("32463")
                .build()
                .toIdentifierSegment())
        .isEqualTo("N1011537977V693883+673+32463");
  }

  @Test
  void unpackStringFormat() {
    assertThat(VistaIdentifierSegment.unpack("sNicn+siteId+vistaId"))
        .isEqualTo(
            VistaIdentifierSegment.builder()
                .patientIdentifierType(VistaIdentifierSegment.PatientIdentifierType.NATIONAL_ICN)
                .patientIdentifier("icn")
                .vistaSiteId("siteId")
                .vistaRecordId("vistaId")
                .build());
  }
}
