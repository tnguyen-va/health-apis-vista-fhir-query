package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.io.Serial;
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
  public static Instant from(ValueOnlyXmlAttribute valueOnlyXmlAttribute) {
    if (valueOnlyXmlAttribute == null) {
      return null;
    }
    return from(new Parser(valueOnlyXmlAttribute.value()).parse()).instant;
  }

  /** Static constructor for String input. */
  public static Instant from(String filemanDate) {
    if (filemanDate == null) {
      return null;
    }
    return from(new Parser(filemanDate).parse()).instant;
  }

  public String toString() {
    return instant.toString();
  }

  public static class BadFilemanDate extends RuntimeException {
    @Serial private static final long serialVersionUID = 2556943342836450618L;

    public BadFilemanDate(String message) {
      super(message);
    }
  }

  private static class Parser {
    final char[] value;
    int index;

    public Parser(String s) {
      value = s.toCharArray();
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
      final int year = next(3);
      final int month = nextIfAvailable(2, 1);
      final int day = nextIfAvailable(2, 1);
      requireCharIfAvailable('.');
      int hour = nextIfAvailable(2, 0);
      int minute = nextIfAvailable(2, 0);
      int second = nextIfAvailable(2, 0);
      requireNoMoreChars();
      index = 0;
      return ZonedDateTime.of(year + 1700, month, day, hour, minute, second, 0, ZoneId.of("UTC"))
          .toInstant();
    }

    private int readInt(int chars) {
      try {
        return Integer.parseInt(String.valueOf(value, index, chars));
      } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
        throw new BadFilemanDate("Cannot parse string into a date, has invalid character(s).");
      } finally {
        index += chars;
      }
    }

    private int remaining() {
      return value.length - index;
    }

    void require(int chars) {
      if (remaining() < chars) {
        throw new BadFilemanDate("Invalid date string: not enough characters.");
      }
    }

    void requireCharIfAvailable(char c) {
      if (remaining() == 0) {
        return;
      }
      try {
        if (value[index] != c) {
          throw new BadFilemanDate("Invalid date string: missing decimal.");
        }
      } finally {
        index++;
      }
    }

    void requireNoMoreChars() {
      if (remaining() > 0) {
        throw new BadFilemanDate("Invalid date string: there are extra characters.");
      }
    }
  }
}
