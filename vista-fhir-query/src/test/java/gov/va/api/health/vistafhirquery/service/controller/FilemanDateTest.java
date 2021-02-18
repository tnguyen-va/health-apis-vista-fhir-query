package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.*;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.Test;

public class FilemanDateTest {
  @Test
  void checkInvalidDates() {
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from("2975019.082801"));
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from("2970940.082801"));
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.302801"));
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.087001"));
    assertThatExceptionOfType(DateTimeException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.082890"));
  }

  @Test
  void checkInvalidStringCannotBeParsed() {
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("aa70919.082801"));
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("297bb19.082801"));
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("29709cc.082801"));
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.aa2801"));
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.08cc01"));
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from("2970919.0828bb"));
  }

  @Test
  void createFilemanDatefromInstant() {
    assertThat(FilemanDate.from(Instant.parse("1997-09-19T08:27:01Z")).asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromString() {
    assertThat(FilemanDate.from("2970919").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T00:00:00Z"));
    assertThat(FilemanDate.from("2970919.08").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:00:00Z"));
    assertThat(FilemanDate.from("2970919.0827").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:00Z"));
    assertThat(FilemanDate.from("2970919.082701").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromValueOnlyXmlAttribute() {
    assertThat(
            FilemanDate.from(ValueOnlyXmlAttribute.builder().value("2970919.082701").build())
                .asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }
}
