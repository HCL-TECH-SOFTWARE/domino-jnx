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
import java.nio.charset.Charset;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDALTTEXT
 */
@StructureDefinition(name = "CDALTTEXT", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "wLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved1", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved2", type = int.class)
})
public interface CDAltText extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("wLength")
  int getLength();
  
  /**
   * Gets the text for alttext in the variable data portion of this record.
   * 
   * <p>
   * This method also sets the {@code wLength} property to the appropriate value.
   * </p>
   *
   * @return this record
   */
  default String getAltText() {
//    ByteBuffer buf = getVariableData();
//    int len = buf.remaining();
//    byte[] lmbcs = new byte[len];
//    buf.get(lmbcs);
//    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getLength()
        );
  }
  
  /**
   * Stores the text for alttext in the variable data portion of this record.
   * <p>
   * The buffer will be resized, if necessary, to hold the text value.
   * </p>
   * <p>
   * This method also sets the {@code wLength} property to the appropriate value.
   * </p>
   *
   * @param altText the caption text to set
   * @return this record
   */
  default CDAltText setAltText(final String altText) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getLength(),
        altText,
        this::setLength
      );
  }
  
  @StructureSetter("wLength")
  CDAltText setLength(int length);
}
