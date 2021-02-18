package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import lombok.Value;

@Value
public class FilemanDate {
  private static Pattern datePatternWithTime =
      Pattern.compile(
          "(?<year>[0-9]{3})(?<month>[0-9]{2})(?<day>[0-9]{2})"
              + "(\\.)(?<hour>([0-9]{2}))(?<minute>([0-9]{2}))?(?<second>([0-9]{2}))?$");

  private static Pattern datePatternNoTime =
      Pattern.compile("(?<year>[0-9]{3})(?<month>[0-9]{2})(?<day>[0-9]{2})$");

  Instant instant;

  public FilemanDate(Instant i) {
    this.instant = i;
  }

  public static FilemanDate from(Instant i) {
    return new FilemanDate(i);
  }

  /** Static constructor for ValueOnlyXmlAttribute. */
  public static FilemanDate from(ValueOnlyXmlAttribute valueOnlyXmlAttribute) {
    if (valueOnlyXmlAttribute == null) {
      return new FilemanDate(null);
    }
    return new FilemanDate(parse(valueOnlyXmlAttribute.value()));
  }

  /** Static constructor for String input. */
  public static FilemanDate from(String filemanDate) {
    if (filemanDate == null) {
      return new FilemanDate(null);
    }
    return new FilemanDate(parse(filemanDate));
  }

  private static Instant parse(String filemanDate) {
    var m = datePatternWithTime.matcher(filemanDate);
    var m2 = datePatternNoTime.matcher(filemanDate);
    if (!m.matches() && !m2.matches()) {
      throw new PatternSyntaxException(
          "Value provided is not a valid date.", datePatternWithTime.toString(), -1);
    }
    String year = "00";
    String month = "00";
    String day = "00";
    String hour = null;
    String minute = null;
    String second = null;
    if (m.matches()) {
      year = m.group("year");
      month = m.group("month");
      day = m.group("day");
      hour = m.group("hour");
      minute = m.group("minute");
      second = m.group("second");
    }
    if (m2.matches()) {
      year = m2.group("year");
      month = m2.group("month");
      day = m2.group("day");
    }
    return ZonedDateTime.of(
            Integer.parseInt(year) + 1700,
            Integer.parseInt(month),
            Integer.parseInt(day),
            hour == null ? 0 : Integer.parseInt(hour),
            minute == null ? 0 : Integer.parseInt(minute),
            second == null ? 0 : Integer.parseInt(second),
            0,
            ZoneId.of("UTC"))
        .toInstant();
  }

  public String toString() {
    return instant.toString();
  }
}
