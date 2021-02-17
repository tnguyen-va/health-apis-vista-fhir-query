package gov.va.api.health.vistafhirquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class FilemanDateTest {
  @Test
  void createFilemanDatefromInstant() {
    assertThat(new FilemanDate(Instant.parse("1997-09-19T08:27:01Z")).asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromString() {
    assertThat(new FilemanDate("2970919").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T00:00:00Z"));
    assertThat(new FilemanDate("2970919.08").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:00:00Z"));
    assertThat(new FilemanDate("2970919.0827").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:00Z"));
    assertThat(new FilemanDate("2970919.082701").asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }

  @Test
  void createFilemanDatefromValueOnlyXmlAttribute() {
    assertThat(
            new FilemanDate(ValueOnlyXmlAttribute.builder().value("2970919.082701").build())
                .asInstant())
        .isEqualTo(Instant.parse("1997-09-19T08:27:01Z"));
  }
}
