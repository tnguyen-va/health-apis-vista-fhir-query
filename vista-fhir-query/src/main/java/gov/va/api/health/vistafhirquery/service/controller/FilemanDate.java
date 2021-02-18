package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.PatternSyntaxException;
import lombok.Value;

@Value
public class FilemanDate {
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
      return null;
    }
    return new FilemanDate(parse(valueOnlyXmlAttribute.value()));
  }

  /** Static constructor for String input. */
  public static FilemanDate from(String filemanDate) {
    if (filemanDate == null) {
      return null;
    }
    return new FilemanDate(parse(filemanDate));
  }

  private static Integer getIntegerSubstring(String checkforInt, int index1, int index2) {
    String maybeInt = checkforInt.substring(index1, index2);
    if (!maybeInt.matches("[0-9]+")) {
      throw new PatternSyntaxException(
          "Index " + index1 + "-" + index2 + "of" + maybeInt + "is not a number", "[0-9]+", -1);
    }
    return Integer.parseInt(maybeInt);
  }

  private static Instant parse(String filemanDate) {
    int year;
    int month;
    int day;
    int hour = 0;
    int minute = 0;
    int second = 0;
    if (filemanDate.contains(".")) {
      String[] splitDate = filemanDate.split("\\.", -1);
      String head = splitDate[0];
      year = getIntegerSubstring(head, 0, 3) + 1700;
      month = getIntegerSubstring(head, 3, 5);
      day = getIntegerSubstring(head, 5, 7);
      String tail = splitDate[1];
      if (tail.length() >= 2) {
        hour = getIntegerSubstring(tail, 0, 2);
      }
      if (tail.length() >= 4) {
        minute = getIntegerSubstring(tail, 2, 4);
      }
      if (tail.length() == 6) {
        second = getIntegerSubstring(tail, 4, 6);
      }
    } else {
      year = getIntegerSubstring(filemanDate, 0, 3) + 1700;
      month = getIntegerSubstring(filemanDate, 3, 5);
      day = getIntegerSubstring(filemanDate, 5, 7);
    }
    return ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC"))
        .toInstant();
  }

  public String toString() {
    return instant.toString();
  }
}
