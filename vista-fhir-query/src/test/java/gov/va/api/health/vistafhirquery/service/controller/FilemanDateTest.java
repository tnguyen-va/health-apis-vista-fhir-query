package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.*;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class FilemanDateTest {
  private static Stream<Arguments> stringArguments() {
    return Stream.of(
        Arguments.arguments("2970919", ZoneId.of("UTC"), "1997-09-19T00:00:00Z"),
        Arguments.arguments("2970919.", ZoneId.of("UTC"), "1997-09-19T00:00:00Z"),
        Arguments.arguments("2970919.08", ZoneId.of("UTC"), "1997-09-19T08:00:00Z"),
        Arguments.arguments("2970919.0827", ZoneId.of("UTC"), "1997-09-19T08:27:00Z"),
        Arguments.arguments("2970919.082701", ZoneId.of("UTC"), "1997-09-19T08:27:01Z"),
        Arguments.arguments("2970919.082701", ZoneId.of("UTC"), "1997-09-19T08:27:01Z"),
        Arguments.arguments(
            "2970919.082701", ZoneId.of("America/New_York"), "1997-09-19T12:27:01Z"),
        Arguments.arguments("2970919.082701", ZoneId.of("America/Chicago"), "1997-09-19T13:27:01Z"),
        Arguments.arguments("2970919.082701", ZoneId.of("America/Denver"), "1997-09-19T14:27:01Z"),
        Arguments.arguments(
            "2970919.082701", ZoneId.of("America/Los_Angeles"), "1997-09-19T15:27:01Z"),
        Arguments.arguments(
            "2970919.082701", ZoneId.of("America/Anchorage"), "1997-09-19T16:27:01Z"));
  }

  @Test
  void checkForNullTimeZones() {
    assertThatExceptionOfType(FilemanDate.BadFilemanDate.class)
        .isThrownBy(() -> FilemanDate.from("2970919.082701", null));
  }

  @Test
  void checkForNullValues() {
    assertThat(FilemanDate.from((String) null, ZoneId.of("America/New_York"))).isNull();
    assertThat(FilemanDate.from((ValueOnlyXmlAttribute) null, null)).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2975019.082801",
        "2970940.082801",
        "2970919.302801",
        "2970919.087001",
        "2970919.087001",
        "2970919.082890"
      })
  void checkInvalidDates(String invalidDate) {
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from(invalidDate, ZoneId.of("UTC")));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "27909191.082801",
        "aa70919.082801",
        "297bb19.082801",
        "29709cc.082801",
        "2970919.aa2801",
        "2970919.08cc01",
        "2970919.0828bb"
      })
  void checkInvalidStringCannotBeParsed(String invalidString) {
    assertThatExceptionOfType(FilemanDate.BadFilemanDate.class)
        .isThrownBy(() -> FilemanDate.from(invalidString, ZoneId.of("UTC")));
  }

  @Test
  void createFilemanDatefromInstant() {
    assertThat(FilemanDate.from(Instant.parse("1997-09-19T08:27:01Z")).instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @ParameterizedTest
  @MethodSource("stringArguments")
  void createFilemanDatefromString(String fhirDate, ZoneId timeZone, String expected) {
    assertThat(FilemanDate.from(fhirDate, timeZone)).isEqualTo(Instant.parse(expected));
  }

  @Test
  void createFilemanDatefromValueOnlyXmlAttribute() {
    assertThat(
            FilemanDate.from(
                ValueOnlyXmlAttribute.builder().value("2970919.082701").build(), ZoneId.of("UTC")))
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }
}
