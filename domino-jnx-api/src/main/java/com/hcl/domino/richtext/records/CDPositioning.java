package com.hcl.domino.richtext.records;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.LengthValue;

@StructureDefinition(
    name = "CDPOSITIONING",
    members = {
      @StructureMember(name = "Header", type = BSIG.class),
      @StructureMember(name = "Scheme", type = CDPositioning.Scheme.class),
      @StructureMember(name = "bReserved", type = byte.class),
      @StructureMember(name = "ZIndex", type = int.class),
      @StructureMember(name = "Top", type = LengthValue.class),
      @StructureMember(name = "Left", type = LengthValue.class),
      @StructureMember(name = "Bottom", type = LengthValue.class),
      @StructureMember(name = "Right", type = LengthValue.class),
      @StructureMember(name = "BrowserLeftOffset", type = double.class),
      @StructureMember(name = "BrowserRightOffset", type = double.class)
    }
  )
public interface CDPositioning extends RichTextRecord<BSIG> {
  
  enum Scheme implements INumberEnum<Byte> {
    STATIC((byte)RichTextConstants.CDPOSITIONING_SCHEME_STATIC),
    ABSOLUTE((byte)RichTextConstants.CDPOSITIONING_SCHEME_ABSOLUTE),
    RELATIVE((byte)RichTextConstants.CDPOSITIONING_SCHEME_RELATIVE),
    FIXED((byte)RichTextConstants.CDPOSITIONING_SCHEME_FIXED);
    private final byte value;
    private Scheme(byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Byte getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("Scheme")
  Scheme getScheme();
  
  @StructureSetter("Scheme")
  CDPositioning setScheme(Scheme scheme);
  
  @StructureGetter("Scheme")
  byte getSchemeRaw();
  
  @StructureSetter("Scheme")
  CDPositioning setSchemeRaw(byte scheme);
  
  @StructureGetter("ZIndex")
  int getZIndex();
  
  @StructureSetter("ZIndex")
  CDPositioning setZIndex(int zindex);
  
  @StructureGetter("Top")
  LengthValue getTop();
  
  @StructureGetter("Left")
  LengthValue getLeft();
  
  @StructureGetter("Bottom")
  LengthValue getBottom();
  
  @StructureGetter("Right")
  LengthValue getRight();
  
  @StructureGetter("BrowserLeftOffset")
  double getBrowserLeftOffset();
  
  @StructureSetter("BrowserLeftOffset")
  CDPositioning setBrowserLeftOffset(double browserLeftOffset);
  
  @StructureGetter("BrowserRightOffset")
  double getBrowserRightOffset();
  
  @StructureSetter("BrowserRightOffset")
  CDPositioning setBrowserRightOffset(double browserRightOffset);

}
