package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.*;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.regex.PatternSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FilemanDateTest {
  @Test
  void checkForNullValues() {
    assertThat(FilemanDate.from((String) null).instant()).isNull();
    assertThat(FilemanDate.from((ValueOnlyXmlAttribute) null).instant()).isNull();
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
    assertThatExceptionOfType(PatternSyntaxException.class)
        .isThrownBy(() -> FilemanDate.from(invalidString));
  }

  @Test
  void createFilemanDatefromInstant() {
    assertThat(FilemanDate.from(Instant.parse("1997-09-19T08:27:01Z")).instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromString() {
    assertThat(FilemanDate.from("2970919").instant())
        .isEqualTo(Instant.parse("1997-09-19T00:00:00Z"));
    assertThat(FilemanDate.from("2970919.08").instant())
        .isEqualTo(Instant.parse("1997-09-19T08:00:00Z"));
    assertThat(FilemanDate.from("2970919.0827").instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:00Z"));
    assertThat(FilemanDate.from("2970919.082701").instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromValueOnlyXmlAttribute() {
    assertThat(
            FilemanDate.from(ValueOnlyXmlAttribute.builder().value("2970919.082701").build())
                .instant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }
}
