package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilemanDate {
  private final Instant instant;

  FilemanDate(Instant i) {
    this.instant = i;
  }

  FilemanDate(ValueOnlyXmlAttribute valueOnlyXmlAttribute) {
    this.instant = parse(valueOnlyXmlAttribute.value());
  }

  FilemanDate(String filemanDate) {
    this.instant = parse(filemanDate);
  }

  Instant asInstant() {
    return instant;
  }

  @SuppressWarnings("StringSplitter")
  Instant parse(String filemanDate) {
    String hour = "00";
    String minute = "00";
    String second = "00";
    if (filemanDate.contains(".")) {
      String[] splitDate = filemanDate.split("\\.");
      String tail = splitDate[1];
      log.info("TAIL: " + tail);
      if (tail.length() >= 2) {
        hour = tail.substring(0, 2);
        log.info("HOUR: " + hour);
      }
      if (tail.length() >= 4) {
        minute = tail.substring(2, 4);
      }
      if (tail.length() == 6) {
        second = tail.substring(4, 6);
      }
    }
    String year = filemanDate.substring(0, 3);
    year = String.valueOf(Integer.parseInt(year) + 1700);
    String month = filemanDate.substring(3, 5);
    String day = filemanDate.substring(5, 7);
    return Instant.parse(
        year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + "Z");
  }

  public String toString() {
    return instant.toString();
  }
}
