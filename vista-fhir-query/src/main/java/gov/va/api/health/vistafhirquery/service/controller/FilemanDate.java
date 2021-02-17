package gov.va.api.health.vistafhirquery.service.controller;

import gov.va.api.lighthouse.vistalink.models.ValueOnlyXmlAttribute;
import java.time.Instant;

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

  Instant parse(String filemanDate) {
    String year = filemanDate.substring(0, 3);
    year = String.valueOf(Integer.parseInt(year) + 1700);
    String month = filemanDate.substring(3, 5);
    String day = filemanDate.substring(5, 7);
    String hour = filemanDate.substring(8, 10);
    String minute = filemanDate.substring(10, 12);
    String second = filemanDate.substring(12, 14);

    return Instant.parse(
        year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + "Z");
  }

  public String toString() {
    return instant.toString();
  }
}
