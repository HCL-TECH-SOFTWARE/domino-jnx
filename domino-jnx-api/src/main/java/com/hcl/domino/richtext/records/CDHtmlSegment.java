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

import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * CDHTMLSEGMENT
 * 
 * @author 
 * @since 1.0.46
 */

@StructureDefinition(
  name = "CDHTMLSEGMENT", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class),                        
    @StructureMember(name = "wHTMLLength", type = short.class, unsigned = true), 			/* Length of raw HTML */
                                                                                            /* HTML, the variable part of the struct... */
})
public interface CDHtmlSegment extends RichTextRecord<WSIG> {

  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("wHTMLLength")
  int getHTMLLength();

  @StructureSetter("wHTMLLength")
  CDHtmlSegment setHTMLLength(int wHTMLLength);

  /**
   * Gets the HTML in the variable data portion of this record.
   * 
   * <p>
   * This method also sets the {@code wHTMLLength} property to the appropriate value.
   * </p>
   *
   * @return this record
   */
  default String getHTML() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getHTMLLength()
        );
  }
  
  /**
   * Stores the HTML in the variable data portion of this record.
   * <p>
   * The buffer will be resized, if necessary, to hold the text value.
   * </p>
   * <p>
   * This method also sets the {@code wHTMLLength} property to the appropriate value.
   * </p>
   *
   * @param html to set
   * @return this record
   */
  default CDHtmlSegment setHTML(final String html) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getHTMLLength(),
        html,
        this::setHTMLLength
      );
  }
}
