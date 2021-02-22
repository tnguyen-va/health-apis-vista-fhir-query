package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.io.Serial;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
  public static FilemanDate from(ValueOnlyXmlAttribute valueOnlyXmlAttribute, ZoneId timeZone) {
    if (valueOnlyXmlAttribute == null) {
      return null;
    }
    return from(valueOnlyXmlAttribute.value(), timeZone);
  }

  /** Static constructor for String input. */
  public static FilemanDate from(String filemanDate, ZoneId timeZone) {
    if (filemanDate == null) {
      return null;
    }
    return from(new Parser(filemanDate, timeZone).parse());
  }

  /** Returns FileManDate formatted string for a given time zone. */
  public String fileManDate(ZoneId timeZone) {
    DecimalFormat formatter = new DecimalFormat("00");
    ZonedDateTime zdt = instant().atZone(timeZone);
    String year = String.valueOf(zdt.getYear() - 1700);
    String month = formatter.format(zdt.getMonthValue());
    String day = formatter.format(zdt.getDayOfMonth());
    String hour = formatter.format(zdt.getHour());
    String minute = formatter.format(zdt.getMinute());
    String second = formatter.format(zdt.getSecond());
    String fmd = year + month + day + "." + hour + minute + second;
    return fmd.contains(".") ? fmd.replaceAll("0*$", "").replaceAll("\\.$", "") : fmd;
  }

  public static class BadFilemanDate extends RuntimeException {
    @Serial private static final long serialVersionUID = 2556943342836450618L;

    public BadFilemanDate(String message) {
      super(message);
    }

    public BadFilemanDate(String reason, char[] value) {
      super("Invalid date string: " + String.valueOf(value) + ". " + reason);
    }
  }

  private static class Parser {
    final char[] value;

    ZoneId timeZone;

    int index;

    public Parser(String s, ZoneId tz) {
      value = s.toCharArray();
      timeZone = tz;
      index = 0;
    }

    private int next(int chars) {
      require(chars);
      return readInt(chars);
    }

    private int nextIfAvailable(int chars, int defaultValue) {
      return (remaining() == 0) ? defaultValue : readInt(chars);
    }

    public Instant parse() {
      requireTimeZone();
      final int year = next(3);
      final int month = next(2);
      final int day = next(2);
      requireCharIfAvailable('.');
      int hour = nextIfAvailable(2, 0);
      int minute = nextIfAvailable(2, 0);
      int second = nextIfAvailable(2, 0);
      requireNoMoreChars();
      index = 0;
      return ZonedDateTime.of(year + 1700, month, day, hour, minute, second, 0, timeZone)
          .toInstant();
    }

    private int readInt(int chars) {
      try {
        return Integer.parseInt(String.valueOf(value, index, chars));
      } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
        throw new BadFilemanDate("Contains invalid characters.", value);
      } finally {
        index += chars;
      }
    }

    private int remaining() {
      return value.length - index;
    }

    void require(int chars) {
      if (remaining() < chars) {
        throw new BadFilemanDate("Not enough characters.", value);
      }
    }

    void requireCharIfAvailable(char c) {
      if (remaining() == 0) {
        return;
      }
      try {
        if (value[index] != c) {
          throw new BadFilemanDate("Missing decimal.", value);
        }
      } finally {
        index++;
      }
    }

    void requireNoMoreChars() {
      if (remaining() > 0) {
        throw new BadFilemanDate("There are extra characters.", value);
      }
    }

    void requireTimeZone() {
      if (timeZone == null) {
        throw new BadFilemanDate("No time zone specified.");
      }
    }
  }
}
