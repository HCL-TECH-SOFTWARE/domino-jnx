/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.frameset.FrameScrollStyle;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.CDFrameVariableData;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDFRAME",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDFrame.Flag.class, bitfield = true),
    @StructureMember(name = "DataFlags", type = CDFrame.DataFlag.class, bitfield = true),
    @StructureMember(name = "BorderEnable", type = byte.class),
    @StructureMember(name = "NoResize", type = byte.class),
    @StructureMember(name = "ScrollBarStyle", type = FrameScrollStyle.class),
    @StructureMember(name = "MarginWidth", type = short.class, unsigned = true),
    @StructureMember(name = "MarginHeight", type = short.class, unsigned = true),
    @StructureMember(name = "dwReserved", type = int.class),
    @StructureMember(name = "FrameNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved1", type = short.class),
    @StructureMember(name = "FrameTargetLength", type = short.class, unsigned = true),
    @StructureMember(name = "FrameBorderColor", type = ColorValue.class),
    @StructureMember(name = "wReserved", type = short.class, unsigned = true),
  }
)
public interface CDFrame extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /**  Set if BorderEnable is specified  */
    BorderEnable(DesignConstants.fFRBorderEnable),
    /**  Set if MarginWidth is specified  */
    MarginWidth(DesignConstants.fFRMarginWidth),
    /**  Set if MarginHeight is specified  */
    MarginHeight(DesignConstants.fFRMarginHeight),
    /**  Set if FrameBorderColor is specified  */
    FrameBorderColor(DesignConstants.fFRFrameBorderColor),
    /**  Set if ScrollBarStyle is specified  */
    Scrolling(DesignConstants.fFRScrolling),
    /**  Set if this frame has a notes only border (a border with caption text =&gt;"Caption Only") */
    NotesOnlyBorder(DesignConstants.fFRNotesOnlyBorder),
    /**  Set if this frame wants arrows shown in Notes (a border with arrows =&gt;"Arrows Only") */
    NotesOnlyArrows(DesignConstants.fFRNotesOnlyArrows),
    /**  Open value specified for Border caption is in percent. */
    NotesOpenPercent(DesignConstants.fFRNotesOpenPercent),
    /**  if set, set initial focus to this frame  */
    NotesInitialFocus(DesignConstants.fFRNotesInitialFocus),
    /**  Set if this frame caption reading order is Right-To-Left */
    NotesReadingOrder(DesignConstants.fFRNotesReadingOrder);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  enum DataFlag implements INumberEnum<Short> {
    NotesBorder(DesignConstants.fFRNotesBorder),
    NotesBorderFontAndColor(DesignConstants.fFRNotesBorderFontAndColor),
    NotesBorderCaption(DesignConstants.fFRNotesBorderCaption),
    NotesCaptionFontName(DesignConstants.fFRNotesCaptionFontName),
    /**  set this if frame has a sequence set other than the default 0  */
    Sequence(DesignConstants.fFRSequence);
    private final short value;
    private DataFlag(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  enum BorderAlignment implements INumberEnum<Short> {
    Top((short) 0),
    Bottom((short) 1),
    Left((short) 2),
    Right((short) 3);
    private final short value;
    private BorderAlignment(short value) {
      this.value = value;
    }
    
    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  enum TextAlignment implements INumberEnum<Short> {
    Left((short)0),
    Right((short)1),
    Center((short)2);
    private final short value;
    private TextAlignment(short value) {
      this.value = value;
    }
    
    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDFrame setFlags(Collection<Flag> flags);
  
  @StructureGetter("DataFlags")
  Set<DataFlag> getDataFlags();
  
  @StructureSetter("DataFlags")
  CDFrame setDataFlags(Collection<DataFlag> flags);
  
  @StructureGetter("BorderEnable")
  byte getBorderEnable();
  
  @StructureSetter("BorderEnable")
  CDFrame setBorderEnable(byte borderEnable);
  
  /**
   * Specifies the NORESIZE attribute for this Frame element
   * 
   * @return noresize flag (0 = false, 1=true)
   */
  @StructureGetter("NoResize")
  byte getNoResize();
  
  /**
   * Specifies the NORESIZE attribute for this Frame element
   * 
   * @param noResize noresize flag (0 = false, 1=true)
   * @return this frame
   */
  @StructureSetter("NoResize")
  CDFrame setNoResize(byte noResize);
  
  @StructureGetter("ScrollBarStyle")
  FrameScrollStyle getScrollBarStyle();
  
  @StructureSetter("ScrollBarStyle")
  CDFrame setScrollBarStyle(FrameScrollStyle style);
  
  @StructureGetter("MarginWidth")
  int getMarginWidth();
  
  @StructureSetter("MarginWidth")
  CDFrame setMarginWidth(int width);
  
  @StructureGetter("MarginHeight")
  int getMarginHeight();
  
  @StructureSetter("MarginHeight")
  CDFrame setMarginHeight(int height);

  @StructureGetter("FrameNameLength")
  int getFrameNameLength();
  
  @StructureSetter("FrameNameLength")
  CDFrame setFrameNameLength(int len);

  @StructureGetter("FrameTargetLength")
  int getFrameTargetLength();
  
  @StructureSetter("FrameTargetLength")
  CDFrame setFrameTargetLength(int len);
  
  @StructureGetter("FrameBorderColor")
  ColorValue getFrameBorderColor();
  
  default String getFrameName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getFrameNameLength()
    );
  }
  
  default CDFrame setFrameName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getFrameNameLength(),
      name,
      this::setFrameNameLength
    );
  }
  
  default String getFrameTarget() {
    return StructureSupport.extractStringValue(
      this,
      getFrameNameLength(),
      getFrameTargetLength()
    );
  }
  
  default CDFrame setFrameTarget(String target) {
    return StructureSupport.writeStringValue(
      this,
      getFrameNameLength(),
      getFrameTargetLength(),
      target,
      this::setFrameTargetLength
    );
  }
  
  default CDFrame writeVariableFrameData(CDFrameVariableData varData) {
	  //allocate a buffer that is large enough for the content
	  byte[] bufArr = new byte[5000];
	  ByteBuffer buf = ByteBuffer.wrap(bufArr).order(ByteOrder.nativeOrder());
	  
	  Set<DataFlag> dataFlags = getDataFlags();
	  
	  if (dataFlags.contains(DataFlag.NotesBorder)) {
		  //TODO find out if this can be enabled in Designer UI
	  }
	  
	  if (dataFlags.contains(DataFlag.NotesBorderFontAndColor)) {
		  //TODO find out if this can be disabled in Designer UI
	  }

	  if (dataFlags.contains(DataFlag.NotesBorderCaption)) {
		  Optional<String> captionFormula = varData.getCaptionFormula();
		  if (captionFormula.isPresent()) {
			  byte[] compiledCaptionFormula = FormulaCompiler.get().compile(captionFormula.get());
			  buf.putShort((short) (compiledCaptionFormula.length & 0xffff));
			  buf.put(compiledCaptionFormula);
		  }
		  else {
			  buf.putShort((short) 0);
		  }
		  
		  Optional<BorderAlignment> borderAlignment = varData.getBorderAlignment();
		  short borderAlignmentShort = borderAlignment.map((align) -> { return align.getValue(); }).orElse((short) 0);
		  buf.putShort(borderAlignmentShort);
		  
		  Optional<TextAlignment> textAlignment = varData.getTextAlignment();
      short textAlignmentShort = textAlignment.map((align) -> { return align.getValue(); }).orElse((short) 0);
		  buf.putShort((short) (textAlignmentShort & 0xffff));
		  
		  int open = varData.getOpen();
		  buf.putShort((short) (open & 0xffff));
	  }
	  
	  ColorValue backgroundColor = varData.getBackgroundColor().orElse(DesignColorsAndFonts.whiteColor());
	  buf.put(backgroundColor.getData());
	  
	  FontStyle fontId = varData.getFontStyle().orElse(DesignColorsAndFonts.defaultFont());
	  buf.put(fontId.getData());
	  
	  ColorValue textColor = varData.getTextColor().orElse(DesignColorsAndFonts.blackColor());
	  buf.put(textColor.getData());

	  if (dataFlags.contains(DataFlag.NotesCaptionFontName)) {
		  String fontName = varData.getFontName().get();
		  byte[] fontNameArr = fontName.getBytes(NativeItemCoder.get().getLmbcsCharset());
		  buf.putShort((short) (fontNameArr.length & 0xffff));
		  buf.put(fontNameArr);
	  }
	  
	  if (dataFlags.contains(DataFlag.Sequence)) {
		  int sequenceNo = varData.getSequenceNo();
		  buf.putShort((short) (sequenceNo & 0xffff));
	  }

	  int additionalVarDataLength = buf.position();
	  buf.limit(additionalVarDataLength).position(0);
	  
	  int frameNameLength = getFrameNameLength();
	  int frameTargetLength = getFrameTargetLength();
	  
	  resizeVariableData(frameNameLength + frameTargetLength + additionalVarDataLength);
	  ByteBuffer resizedVarDataBuffer = getVariableData();
	  resizedVarDataBuffer.position(frameNameLength + frameTargetLength);
	  
	  resizedVarDataBuffer.put(buf);
	  return this;
  }
  
  default Optional<String> getCaptionFormula() {
	  return readVariableFrameData().getCaptionFormula();
  }
  
  default CDFrame setCaptionFormula(String formula) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setCaptionFormula(formula);
	  writeVariableFrameData(varData);
	  return this;
  }
  
  /**
   * Returns the text alignment
   * 
   * @return alignment
   */
  default Optional<TextAlignment> getTextAlignment() {
	  return readVariableFrameData().getTextAlignment();
  }
  
	/**
	 * Sets the text alignment
	 * 
	 * @param align new alignment
	 * @return this record
	 */
  default CDFrame setTextAlignment(TextAlignment align) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setTextAlignment(align);
	  writeVariableFrameData(varData);
	  return this;
  }
  
  default Optional<BorderAlignment> getBorderAlignment() {
    return readVariableFrameData().getBorderAlignment();
  }
  
  default CDFrame setBorderAlignment(BorderAlignment align) {
    CDFrameVariableData varData = readVariableFrameData();
    varData.setBorderAlignment(align);
    writeVariableFrameData(varData);
    return this;
  }
  
  default int getOpen() {
	  return readVariableFrameData().getOpen();
  }
  
  default CDFrame setOpen(int open) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setOpen(open);
	  writeVariableFrameData(varData);
	  return this;
  }
  
  default Optional<ColorValue> getBackgroundColor() {
	  return readVariableFrameData().getBackgroundColor();
  }

  default CDFrame setBackgroundColor(ColorValue color) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setBackgroundColor(color);
	  writeVariableFrameData(varData);
	  return this;
  }

  default Optional<FontStyle> getFontStyle() {
	  return readVariableFrameData().getFontStyle();  
  }

  default CDFrame setFontStyle(FontStyle style) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setFontStyle(style);
	  writeVariableFrameData(varData);
	  return this;
  }

  default Optional<ColorValue> getTextColor() {
	  return readVariableFrameData().getTextColor();  
  }

  default CDFrame setTextColor(ColorValue color) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setTextColor(color);
	  writeVariableFrameData(varData);
	  return this;
  }

  default Optional<String> getFontName() {
	  return readVariableFrameData().getFontName();  
  }
  
  default CDFrame setFontName(String fontName) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setFontName(fontName);
	  writeVariableFrameData(varData);
	  return this;
  }
  
  default int getSequenceNo() {
	  return readVariableFrameData().getSequenceNo(); 
  }
  
  default CDFrame setSequenceNo(int seq) {
	  CDFrameVariableData varData = readVariableFrameData();
	  varData.setSequenceNo(seq);
	  writeVariableFrameData(varData);
	  return this;
  }
  
  default CDFrameVariableData readVariableFrameData() {
	  CDFrameVariableData varData = new CDFrameVariableData(this);
	  
	  final ByteBuffer buf = getVariableData();
	  
	  Set<DataFlag> dataFlags = getDataFlags();
	  
	  if (dataFlags.contains(DataFlag.NotesBorder)) {
		  //TODO find out if this can be enabled in Designer UI
	  }
	  
	  if (dataFlags.contains(DataFlag.NotesBorderFontAndColor)) {
		  //TODO find out if this can be disabled in Designer UI
	  }

	  if (dataFlags.contains(DataFlag.NotesBorderCaption)) {
		  int frameNameLength = getFrameNameLength();
		  int frameTargetLength = getFrameTargetLength();
		  int preLen = frameNameLength + frameTargetLength;
		  
		  //WORD with caption formula length
		  buf.position(preLen);
		  short captionFormulaLength = buf.getShort();
		  int captionFormulaLengthUnsigned = Short.toUnsignedInt(captionFormulaLength);
		  
		  if (captionFormulaLengthUnsigned > 0) {
			  byte[] captionFormulaArr = new byte[captionFormulaLengthUnsigned];
			  buf.get(captionFormulaArr);
			  String captionFormula = FormulaCompiler.get().decompile(captionFormulaArr);
			  varData.setCaptionFormula(captionFormula);
		  }
		  
		  short borderAlignmentShort = buf.getShort();
		  BorderAlignment borderAlignment = BorderAlignment.Top; // use 0x0000 by default
		  for (BorderAlignment currAlignment : BorderAlignment.values()) {
		    if (borderAlignmentShort == currAlignment.getValue()) {
		      borderAlignment = currAlignment;
		      break;
		    }
		  }
		  
		  varData.setBorderAlignment(borderAlignment);
		  
		  short textAlignShort = buf.getShort();
		  for (TextAlignment currAlign : TextAlignment.values()) {
		    if (textAlignShort == currAlign.getValue()) {
		      varData.setTextAlignment(currAlign);
		      break;
		    }
		  }
		  
		  short open = buf.getShort();
		  varData.setOpen(Short.toUnsignedInt(open));
	  }
	  
	  byte[] backgroundColorArr = new byte[6]; // WORD FLAGS + 4 byte for component
	  buf.get(backgroundColorArr);
	  ColorValue backgroundColor = MemoryStructureWrapperService.get().wrapStructure(ColorValue.class, ByteBuffer.wrap(backgroundColorArr));
	  varData.setBackgroundColor(backgroundColor);
	  
	  byte[] fontIdArr = new byte[4];
	  buf.get(fontIdArr);
	  FontStyle fontId = MemoryStructureWrapperService.get().wrapStructure(FontStyle.class, ByteBuffer.wrap(fontIdArr));
	  varData.setFontStyle(fontId);
	  
	  byte[] textColorArr = new byte[6]; // WORD FLAGS + 4 byte for component
	  buf.get(textColorArr);
	  ColorValue textColor = MemoryStructureWrapperService.get().wrapStructure(ColorValue.class, ByteBuffer.wrap(textColorArr));
	  varData.setTextColor(textColor);
	  
	  if (dataFlags.contains(DataFlag.NotesCaptionFontName)) {
		  short fontNameLength = buf.getShort();
		  int fontNameLengthUnsigned = Short.toUnsignedInt(fontNameLength);
		  byte[] fontNameArr = new byte[fontNameLengthUnsigned];
		  buf.get(fontNameArr);
		  String fontName = StructureSupport.readLmbcsValue(fontNameArr);
		  varData.setFontName(fontName);
	  }
	  
	  if (dataFlags.contains(DataFlag.Sequence)) {
		  short sequenceNo = buf.getShort();
		  varData.setSequenceNo(Short.toUnsignedInt(sequenceNo));
	  }
	  
	  return varData;
  }
}
