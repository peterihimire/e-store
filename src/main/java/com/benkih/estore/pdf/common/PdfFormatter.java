package com.benkih.estore.pdf.common;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class PdfFormatter {
//  private PdfFormatter() {}

  public static String formatMoney(BigDecimal amount) {
    //    NumberFormat formatter =
    //        NumberFormat.getCurrencyInstance(Locale.US);
    //    return formatter.format(amount);
    if (amount == null) {
      return "₦0.00";
    }
    return "₦" + NumberFormat
        .getNumberInstance(Locale.US)
        .format(amount);
  }

  public static String formatDate(Instant date) {
    if (date == null) {
      return "";
    }

    return DateTimeFormatter
        .ofPattern("dd MMM yyyy hh:mm a")
        .withZone(ZoneId.of("Africa/Lagos"))
        .format(date);
//    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"));
  }
}
