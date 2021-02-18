package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.*;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class FilemanDateTest {
  private static Stream<Arguments> stringArguments() {
    return Stream.of(
        Arguments.arguments("2970919", "1997-09-19T00:00:00Z"),
        Arguments.arguments("2970919.08", "1997-09-19T08:00:00Z"),
        Arguments.arguments("2970919.0827", "1997-09-19T08:27:00Z"),
        Arguments.arguments("2970919.082701", "1997-09-19T08:27:01Z"));
  }

  @Test
  void checkForNullValues() {
    assertThat(FilemanDate.from((String) null)).isNull();
    assertThat(FilemanDate.from((ValueOnlyXmlAttribute) null)).isNull();
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
        .isThrownBy(() -> FilemanDate.from(invalidDate));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "aa70919.082801",
        "297bb19.082801",
        "29709cc.082801",
        "2970919.aa2801",
        "2970919.08cc01",
        "2970919.0828bb"
      })
  void checkInvalidStringCannotBeParsed(String invalidString) {
    assertThatExceptionOfType(FilemanDate.BadFilemanDate.class)
        .isThrownBy(() -> FilemanDate.from(invalidString));
  }

  @Test
  void createFilemanDatefromInstant() {
    assertThat(FilemanDate.from(Instant.parse("1997-09-19T08:27:01Z")).instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @ParameterizedTest
  @MethodSource("stringArguments")
  void createFilemanDatefromString(String fhirDate, String expected) {
    assertThat(FilemanDate.from(fhirDate)).isEqualTo(Instant.parse(expected));
  }

  @Test
  void createFilemanDatefromValueOnlyXmlAttribute() {
    assertThat(FilemanDate.from(ValueOnlyXmlAttribute.builder().value("2970919.082701").build()))
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }
}
