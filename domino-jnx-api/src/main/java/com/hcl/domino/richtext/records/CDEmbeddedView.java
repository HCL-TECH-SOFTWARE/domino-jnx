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
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * Rich text record of type CDEMBEDDEDVIEW
 */
@StructureDefinition(name = "CDEMBEDDEDVIEW", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEmbeddedView.Flag.class, bitfield = true),
    @StructureMember(name = "SpareFontID", type = FontStyle.class),
    @StructureMember(name = "RestrictFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "WebLines", type = short.class, unsigned = true),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "wSpare", type = short.class),
    @StructureMember(name = "Spare", type = int[].class, length = 3)
})
public interface CDEmbeddedView extends RichTextRecord<WSIG> {

  enum Flag implements INumberEnum<Integer> {
    TRANSPARENT(RichTextConstants.EMBEDDEDVIEW_FLAG_TRANSPARENT),
    USEAPPLET_INBROWSER(RichTextConstants.EMBEDDEDVIEW_FLAG_USEAPPLET_INBROWSER),
    USEAPPLET_VIEWPROP(RichTextConstants.EMBEDDEDVIEW_FLAG_USEAPPLET_VIEWPROP),
    USE_WEBLINES(RichTextConstants.EMBEDDEDVIEW_FLAG_USE_WEBLINES), 
    SIMPLE_VIEW_MOUSE_TRACK_ON(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_MOUSE_TRACK_ON),

    SIMPLE_VIEW_HEADER_OFF(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_HEADER_OFF),   
    SIMPLE_VIEW_SHOW_AS_WEB_LINK(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_AS_WEB_LINK),  

    SIMPLE_VIEW_SHOW_ACTION_BAR(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_ACTION_BAR),   
    SIMPLE_VIEW_SHOW_SELECTION_MARGIN(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_SELECTION_MARGIN), 

    SIMPLE_VIEW_SHOW_CURRENT_THREAD(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_CURRENT_THREAD),
    NOT_USE_WEBVIEW_DEFAULT(RichTextConstants.EMBEDDEDVIEW_FLAG_NOT_USE_WEBVIEW_DEFAULT),  

    SIMPLE_VIEW_SHOW_ONLY_SUMMARIZED(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_ONLY_SUMMARIZED),
    HASNAME(RichTextConstants.EMBEDDEDVIEW_FLAG_HASNAME),   
    SIMPLE_VIEW_SHOW_NEWMAIL_AT_TOP(RichTextConstants.EMBEDDEDVIEW_FLAG_SIMPLE_VIEW_SHOW_NEWMAIL_AT_TOP),  

    USEJSCTL_INBROWSER(RichTextConstants.EMBEDDEDVIEW_FLAG_USEJSCTL_INBROWSER), 
    USECUSTOMJS_INBROWSER(RichTextConstants.EMBEDDEDVIEW_FLAG_USECUSTOMJS_INBROWSER);
    
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
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureGetter("Flags")
  int getFlagsRaw();
  
  @StructureSetter("Flags")
  CDEmbeddedView setFlags(Collection<Flag> flags);
  
  @StructureSetter("Flags")
  CDEmbeddedView setFlagsRaw(int flags);
  
  @StructureGetter("SpareFontID")
  FontStyle getSpareFontID();
  
  default CDEmbeddedView getSpareFontID(FontStyle fonstStyle) {
    getSpareFontID().getData().put(fonstStyle.getData());
    return this;
  }
  
  @StructureGetter("RestrictFormulaLength")
  int getRestrictFormulaLength();
  
  @StructureSetter("RestrictFormulaLength")
  CDEmbeddedView setRestrictFormulaLength(int restrictFormulaLength);
  
  @StructureGetter("WebLines")
  int getWebLines();
  
  @StructureSetter("WebLines")
  CDEmbeddedView setWebLines(int webLines);
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDEmbeddedView setNameLength(int nameLength);
  
  /**
   * Retrieves the formula for RestrictFormula if RestrictFormulaLength is greater than 0
   *
   * @return an {@link Optional} describing the formula for RestrictFormula,
   *          or an optional one if no formula is set
   */
  default Optional<String> getRestrictFormula() {
    if (this.getRestrictFormulaLength() <= 0) {
      return Optional.empty();
    }
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        0,
        this.getRestrictFormulaLength()
      )
    );
  }

  default void setRestrictFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          0,
          this.getRestrictFormulaLength(), 
          newFormula, 
          this::setRestrictFormulaLength);
  }
}
