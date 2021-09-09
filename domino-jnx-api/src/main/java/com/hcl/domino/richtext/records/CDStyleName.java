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
    @StructureMember(name = "StyleName", type = char[].class, length = RichTextConstants.MAX_STYLE_NAME)
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
  char[] getStyleName();
  
  @StructureSetter("StyleName")
  CDStyleName setStyleName(char[] name);
  
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
    Optional<FontStyle> fontStyle = getFont();
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
}
