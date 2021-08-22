package com.hcl.domino.richtext;

import com.hcl.domino.misc.INumberEnum;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.0.34
 */
public enum HotspotType implements INumberEnum<Short> {
  POPUP(RichTextConstants.HOTSPOTREC_TYPE_POPUP),
  HOTREGION(RichTextConstants.HOTSPOTREC_TYPE_HOTREGION),
  BUTTON(RichTextConstants.HOTSPOTREC_TYPE_BUTTON),
  FILE(RichTextConstants.HOTSPOTREC_TYPE_FILE),
  SECTION(RichTextConstants.HOTSPOTREC_TYPE_SECTION),
  ANY(RichTextConstants.HOTSPOTREC_TYPE_ANY),
  HOTLINK(RichTextConstants.HOTSPOTREC_TYPE_HOTLINK),
  BUNDLE(RichTextConstants.HOTSPOTREC_TYPE_BUNDLE),
  V4_SECTION(RichTextConstants.HOTSPOTREC_TYPE_V4_SECTION),
  SUBFORM(RichTextConstants.HOTSPOTREC_TYPE_SUBFORM),
  ACTIVEOBJECT(RichTextConstants.HOTSPOTREC_TYPE_ACTIVEOBJECT),
  OLERICHTEXT(RichTextConstants.HOTSPOTREC_TYPE_OLERICHTEXT),
  EMBEDDEDVIEW(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDVIEW),
  EMBEDDEDFPANE(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDFPANE),
  EMBEDDEDNAV(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDNAV),
  MOUSEOVER(RichTextConstants.HOTSPOTREC_TYPE_MOUSEOVER),
  FILEUPLOAD(RichTextConstants.HOTSPOTREC_TYPE_FILEUPLOAD),
  EMBEDDEDOUTLINE(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDOUTLINE),
  EMBEDDEDCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDCTL),
  EMBEDDEDCALENDARCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDCALENDARCTL),
  EMBEDDEDSCHEDCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDSCHEDCTL),
  RCLINK(RichTextConstants.HOTSPOTREC_TYPE_RCLINK),
  EMBEDDEDEDITCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDEDITCTL),
  CONTACTLISTCTL(RichTextConstants.HOTSPOTREC_TYPE_CONTACTLISTCTL);

  private final short value;

  HotspotType(final short value) {
    this.value = value;
  }

  @Override
  public long getLongValue() {
    return this.value;
  }

  @Override
  public Short getValue() {
    return this.value;
  }
}