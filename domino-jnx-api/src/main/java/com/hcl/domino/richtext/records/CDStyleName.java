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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * Rich text record of type CDSTYLENAME
 */
@StructureDefinition(name = "CDSTYLENAME", members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "Flags", type = CDStyleName.Flag.class, bitfield = true),
    @StructureMember(name = "PABID", type = short.class, unsigned = true),
    @StructureMember(name = "StyleName", type = byte[].class, length = RichTextConstants.MAX_STYLE_NAME)
})
public interface CDStyleName extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Integer> {
    FONTID(RichTextConstants.STYLE_FLAG_FONTID),
    INCYCLE(RichTextConstants.STYLE_FLAG_INCYCLE),
    PERMANENT(RichTextConstants.STYLE_FLAG_PERMANENT),
    MARGIN(RichTextConstants.STYLE_FLAG_MARGIN);
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
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDStyleName setFlags(Collection<Flag> flags);
  
  @StructureGetter("Flags")
  int getFlagsRaw();
  
  @StructureSetter("Flags")
  CDStyleName setFlagsRaw(int flags);
  
  @StructureGetter("PABID")
  int getPabId();
  
  @StructureSetter("PABID")
  CDStyleName setPabId(int id);
  
  @StructureGetter("StyleName")
  byte[] getStyleNameRaw();
  
  @StructureSetter("StyleName")
  CDStyleName setStyleName(byte[] name);
  
  default String getStyleName() {
    return StructureSupport.readLmbcsValue(getStyleNameRaw());
  }
  
  /**
   * Retrieves the font value for this style, if specified.
   * 
   * @return an {@link Optional} describing the corresponding {@link FontStyle}
   *         value, or an empty one of this does not have a font
   */
  default Optional<FontStyle> getFont() {
    if(!getFlags().contains(Flag.FONTID)) {
      return Optional.empty();
    }
    final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    
    return Optional.of(wrapper.wrapStructure(FontStyle.class, this.getVariableData()));
  }
  
  /**
   * Retrieves the User name value for this style, if specified.
   * 
   * @return an {@link Optional} returning the corresponding {@link String}
   *         value, or an empty one of this does not have a UserName
   */
  default Optional<String> getUserName() {
    if(!getFlags().contains(Flag.PERMANENT)) {
      return Optional.empty();
    }
    
    int preLen = 0;
    final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    //first read fontStyle
    Optional<FontStyle> fontStyle = this.getFont();
    if (fontStyle.isPresent()) {
      preLen += wrapper.sizeOf(FontStyle.class);
    }
    
    ByteBuffer data = this.getVariableData();
    data.position(preLen);
    //get userName length
    int nameLength = data.getShort();
    preLen += 2;
    return Optional.of(
        StructureSupport.extractStringValue(
          this,
          preLen,
          nameLength
        )
      );
  }
  
  /**
   * Sets the User name value for this style, if specified.
   * 
   * @param userName user name
   * @return returning the corresponding {@link CDStyleName}
   *         value
   */
  default CDStyleName setUserName(String userName) {
    //set Flag.PERMANENT for UserName
    Set<Flag> flags = this.getFlags();
    flags.add(Flag.PERMANENT);
    this.setFlags(flags);
    
    int preLen = 0;
    final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    //first read fontStyle
    Optional<FontStyle> fontStyle = this.getFont();
    if (fontStyle.isPresent()) {
      preLen += wrapper.sizeOf(FontStyle.class);
    }
    
    Number userNameLen = userName.length();
    ByteBuffer buf = this.getVariableData();
    
    //write userName length
    this.resizeVariableData(preLen+2+userNameLen.shortValue());
    buf = this.getVariableData();
    buf.position(preLen);
    buf.putShort(userNameLen.shortValue());
    
    //write User Name
    final byte[] lmbcs = userName == null ? new byte[0] : userName.getBytes(Charset.forName("LMBCS"));
    buf.position(preLen+2);
    buf.put(lmbcs);
    
    return this;
  }
  
  /**
   * Set the font value for this style, if specified.
   * 
   * @param  font FontStyle
   * @return returning the corresponding {@link CDStyleName}
   *         value
   */
  default CDStyleName setFont(FontStyle font) {
    //set Flag.FONTID for font
    Set<Flag> flags = this.getFlags();
    //if old data had font
    boolean hasFont = flags.contains(Flag.FONTID) ? true : false;
    if (!hasFont) {
      flags.add(Flag.FONTID);
      this.setFlags(flags);
    }
    
    ByteBuffer buf = this.getVariableData();
    final MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    final int fontStructSize = wrapper.sizeOf(FontStyle.class);
    final byte[] newData = new byte[fontStructSize];
    final ByteBuffer newbuf = ByteBuffer.wrap(newData).order(ByteOrder.nativeOrder());
    newbuf.put(font.getData());
    //move  newbuf position to 0 to copy data
    newbuf.position(0);
    
    if (hasFont) {
      //resize buffer if needed
      if (buf.limit() < newbuf.limit()) {
        this.resizeVariableData(fontStructSize);
        buf = this.getVariableData();
      }
      //just overwrite old font data
      buf.position(0);
      buf.put(newbuf);
    } else {
      //no previous font data append font data
      if (buf.hasRemaining()) {
        //username data exists
        int otherDataSize = buf.remaining();
        byte[] otherData =  new byte[otherDataSize];
        buf.get(otherData);
        this.resizeVariableData(otherDataSize + fontStructSize);
        buf = this.getVariableData();
        buf.put(newbuf);
        buf.put(otherData);
      } else {
        //no username data exists
        this.resizeVariableData(fontStructSize);
        buf = this.getVariableData();
        buf.put(newbuf);
      }
    }
    
    return this;
  }
}
