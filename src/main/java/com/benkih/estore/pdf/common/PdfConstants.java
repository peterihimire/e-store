package com.benkih.estore.pdf.common;


import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import java.awt.Color;

public final class PdfConstants {

  private PdfConstants() {
  }

  public static final Color PRIMARY = new Color(53, 99, 233);

  public static final Color LIGHT_GRAY = new Color(240,240,240);

  public static final Color BORDER = new Color(220,220,220);

  public static final String COMPANY_NAME = "Benkih Store";

  public static final Font TITLE_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

  public static final Font HEADER_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

  public static final Font BODY_FONT =
      FontFactory.getFont(FontFactory.HELVETICA, 10);

  public static final Font LABEL_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

}
