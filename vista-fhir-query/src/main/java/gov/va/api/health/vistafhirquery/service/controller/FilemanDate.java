package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.PatternSyntaxException;

public class FilemanDate {
  private final Instant instant;

  public FilemanDate(Instant i) {
    this.instant = i;
  }

  public static FilemanDate from(Instant i) {
    return new FilemanDate(i);
  }

  public static FilemanDate from(ValueOnlyXmlAttribute valueOnlyXmlAttribute) {
    return new FilemanDate(parse(valueOnlyXmlAttribute.value()));
  }

  public static FilemanDate from(String filemanDate) {
    return new FilemanDate(parse(filemanDate));
  }

  private static Instant parse(String filemanDate) {
    String numberpattern = "[0-9]+";
    int hour = 0;
    int minute = 0;
    int second = 0;
    if (filemanDate.contains(".")) {
      String[] splitDate = filemanDate.split("\\.", -1);
      String tail = splitDate[1];
      if (tail.length() >= 2) {
        String maybeHour = tail.substring(0, 2);
        if (!maybeHour.matches(numberpattern)) {
          throw new PatternSyntaxException("Hour value cannot be parsed.", numberpattern, -1);
        }
        hour = Integer.parseInt(maybeHour);
      }
      if (tail.length() >= 4) {
        String maybeMinute = tail.substring(2, 4);
        if (!maybeMinute.matches(numberpattern)) {
          throw new PatternSyntaxException("Minute value cannot be parsed.", numberpattern, -1);
        }
        minute = Integer.parseInt(maybeMinute);
      }
      if (tail.length() == 6) {
        String maybeSecond = tail.substring(4, 6);
        if (!maybeSecond.matches(numberpattern)) {
          throw new PatternSyntaxException("Second value cannot be parsed.", numberpattern, -1);
        }
        second = Integer.parseInt(maybeSecond);
      }
    }
    int year = 0;
    int month = 0;
    int day = 0;
    if (filemanDate.length() >= 3) {
      String maybeYear = filemanDate.substring(0, 3);
      if (!maybeYear.matches(numberpattern)) {
        throw new PatternSyntaxException("Year value cannot be parsed.", numberpattern, -1);
      }
      year = Integer.parseInt(maybeYear) + 1700;
    }
    if (filemanDate.length() >= 5) {
      String maybeMonth = filemanDate.substring(3, 5);
      if (!maybeMonth.matches(numberpattern)) {
        throw new PatternSyntaxException("Month value cannot be parsed.", numberpattern, -1);
      }
      month = Integer.parseInt(maybeMonth);
    }
    if (filemanDate.length() >= 7) {
      String maybeDay = filemanDate.substring(5, 7);
      if (!maybeDay.matches(numberpattern)) {
        throw new PatternSyntaxException("Day value cannot be parsed.", numberpattern, -1);
      }
      day = Integer.parseInt(maybeDay);
    }
    return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"))
        .toInstant();
  }

  Instant asInstant() {
    return instant;
  }

  public String toString() {
    return instant.toString();
  }
}
