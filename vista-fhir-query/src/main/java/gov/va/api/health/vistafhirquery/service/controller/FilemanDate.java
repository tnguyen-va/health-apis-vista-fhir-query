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
  private static Pattern datePattern =
      Pattern.compile(
          "(?<year>[0-9]{3})(?<month>[0-9]{2})(?<day>[0-9]{2})"
              + "((\\.)(?<hour>([0-9]{2}))(?<minute>([0-9]{2}))?(?<second>([0-9]{2}))?)?$");

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
    var m = datePattern.matcher(filemanDate);
    if (!m.matches()) {
      throw new PatternSyntaxException(
          "Value provided is not a valid date.", datePattern.toString(), -1);
    }
    String year = m.group("year");
    String month = m.group("month");
    String day = m.group("day");
    String hour = m.group("hour");
    String minute = m.group("minute");
    String second = m.group("second");
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
