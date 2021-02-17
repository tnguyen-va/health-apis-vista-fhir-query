package gov.va.api.health.vistafhirquery.service.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
public class VistaIdentifierSegment {
  @NonNull PatientIdentifierType patientIdentifierType;

  @NonNull String patientIdentifier;

  @NonNull String vistaSiteId;

  @NonNull String vistaRecordId;

  /** Parse a VistaIdentifier. */
  public static VistaIdentifierSegment unpack(String id) {
    return new Encoder().unpack(id);
  }

  /** Build a VistaIdentifier. */
  public String pack() {
    return new Encoder().pack(this);
  }

  /** Build a VistaIdentifier. */
  public String toIdentifierSegment() {
    return String.join(
        "+",
        patientIdentifierType().abbreviation() + patientIdentifier(),
        vistaSiteId(),
        vistaRecordId());
  }

  public Optional<Long> vistaRecordIdAsLong() {
    try {
      return Optional.of(Long.parseLong(vistaRecordId()));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public Optional<Integer> vistaSiteIdAsInteger() {
    try {
      return Optional.of(Integer.parseInt(vistaSiteId()));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  @RequiredArgsConstructor
  public enum PatientIdentifierType {
    /** A Patients DFN in VistA. */
    VISTA_PATIENT_FILE_ID('D'),
    /** A Patients ICN assigned by MPI and existing nationally. */
    NATIONAL_ICN('N'),
    /** An ICN assigned at a local VistA site. */
    LOCAL_VISTA_ICN('L');

    @Getter private final char abbreviation;

    /** Get an Enum value from an abbreviation. */
    public static PatientIdentifierType fromAbbreviation(char abbreviation) {
      switch (abbreviation) {
        case 'D':
          return VISTA_PATIENT_FILE_ID;
        case 'N':
          return NATIONAL_ICN;
        case 'L':
          return LOCAL_VISTA_ICN;
        default:
          throw new IllegalArgumentException(
              "PatientIdentifierType abbreviation in segment is invalid: " + abbreviation);
      }
    }
  }

  private static class Encoder {
    private final Map<Character, Format> formats;

    Encoder() {
      formats = new LinkedHashMap<>();
      formats.put('n', new NumericBufferFormat());
      /* StringFormat is the failsafe format, this should be last. */
      formats.put('s', new StringFormat());
    }

    /** Build a VistaIdentifier. */
    public String pack(VistaIdentifierSegment vis) {
      for (var entry : formats.entrySet()) {
        String value = entry.getValue().tryPack(vis);
        if (value != null) {
          System.out.println(
              "PACK: " + vis.toIdentifierSegment() + " with " + entry.getKey() + " as " + value);
          return entry.getKey() + value;
        }
      }
      throw new IllegalStateException(
          "VistaIdentifierSegment should be been encoded by StringFormat");
    }

    public VistaIdentifierSegment unpack(String data) {
      if (isBlank(data)) {
        throw new IllegalArgumentException("blank identifier");
      }
      char formatId = data.charAt(0);
      Format format = formats.get(formatId);
      if (format == null) {
        format = formats.get('s');
      }
      return format.unpack(data.substring(1));
    }

    private interface Format {

      String tryPack(VistaIdentifierSegment vis);

      VistaIdentifierSegment unpack(String data);
    }

    private static class NumericBufferFormat implements Format {

      private int requiredBufferSize() {
        /* identifier type */
        return Character.BYTES
            /* First part of ICN (ten) */
            + Long.BYTES
            /* Second part of ICN (six) */
            + Integer.BYTES
            /* Station number */
            + Integer.BYTES
            /* Record ID*/
            + Long.BYTES;
      }

      @Override
      public String tryPack(VistaIdentifierSegment vis) {
        Optional<TenVSix> tenVSix = TenVSix.parse(vis.patientIdentifier());
        if (tenVSix.isEmpty()) {
          return null;
        }
        Optional<Integer> stationNumber = vis.vistaSiteIdAsInteger();
        if (stationNumber.isEmpty()) {
          return null;
        }
        Optional<Long> record = vis.vistaRecordIdAsLong();
        if (record.isEmpty()) {
          return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(requiredBufferSize());
        buffer.putChar(vis.patientIdentifierType().abbreviation());
        buffer.putLong(tenVSix.get().ten());
        buffer.putInt(tenVSix.get().six());
        buffer.putInt(stationNumber.get());
        buffer.putLong(record.get());
        return new String(buffer.array(), StandardCharsets.ISO_8859_1);
      }

      @Override
      public VistaIdentifierSegment unpack(String data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes(StandardCharsets.ISO_8859_1));
        char typeAbbreviation = buffer.getChar();
        long ten = buffer.getLong();
        int six = buffer.getInt();
        int stationNumber = buffer.getInt();
        long record = buffer.getLong();
        return VistaIdentifierSegment.builder()
            .patientIdentifierType(PatientIdentifierType.fromAbbreviation(typeAbbreviation))
            .patientIdentifier(TenVSix.builder().ten(ten).six(six).build().asIcn())
            .vistaSiteId(Integer.toString(stationNumber))
            .vistaRecordId(Long.toString(record))
            .build();
      }
    }

    private static class StringFormat implements Format {
      @Override
      public String tryPack(VistaIdentifierSegment vis) {
        return vis.toIdentifierSegment();
      }

      @Override
      public VistaIdentifierSegment unpack(String data) {
        String[] segmentParts = data.split("\\+", -1);
        if (segmentParts.length != 3) {
          throw new IllegalArgumentException(
              "VistaIdentifierSegments are expected to have 3 parts "
                  + "(e.g. patientIdTypeAndId+vistaSiteId+vistaRecordId).");
        }
        if (segmentParts[0].length() < 2) {
          throw new IllegalArgumentException(
              "The first section of a VistaIdentifierSegment must contain "
                  + "a type and an identifier value.");
        }
        return VistaIdentifierSegment.builder()
            .patientIdentifierType(
                PatientIdentifierType.fromAbbreviation(segmentParts[0].charAt(0)))
            .patientIdentifier(segmentParts[0].substring(1))
            .vistaSiteId(segmentParts[1])
            .vistaRecordId(segmentParts[2])
            .build();
      }
    }
  }

  @Value
  @Builder
  private static class TenVSix {
    long ten;
    int six;

    static Optional<TenVSix> parse(String icn) {
      if (isBlank(icn)) {
        return Optional.empty();
      }
      try {
        /* Attempt to find national ICN in 10V6 format. */
        if (icn.length() == 10 + 1 + 6 && icn.charAt(10) == 'V') {
          return Optional.of(
              TenVSix.builder()
                  .ten(Long.parseLong(icn.substring(0, 10)))
                  .six(Integer.parseInt(icn.substring(11)))
                  .build());
        }
        /* Attempt to find all numeric lab-style ID. */
        return Optional.of(TenVSix.builder().ten(Long.parseLong(icn)).six(0).build());
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    }

    String asIcn() {
      if (six == 0) {
        return "" + ten;
      }
      return ten + "V" + six;
    }
  }
}
