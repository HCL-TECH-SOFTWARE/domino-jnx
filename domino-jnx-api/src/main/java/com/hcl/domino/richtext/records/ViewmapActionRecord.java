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
import com.hcl.domino.richtext.structures.NOTELINK;
import com.hcl.domino.richtext.structures.WSIG;

@StructureDefinition(
  name = "VIEWMAP_ACTION_RECORD", 
  members = { 
    @StructureMember(name = "Header", type = WSIG.class), 
    @StructureMember(name = "bHighlightTouch", type = short.class, unsigned = true), 
    @StructureMember(name = "bHighlightCurrent", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineColor", type = short.class, unsigned = true), 
    @StructureMember(name = "HLFillColor", type = short.class, unsigned = true), 
    @StructureMember(name = "ClickAction", type = short.class, unsigned = true), 
    @StructureMember(name = "ActionStringLen", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineWidth", type = short.class, unsigned = true), 
    @StructureMember(name = "HLOutlineStyle", type = short.class, unsigned = true), 
    @StructureMember(name = "LinkInfo", type = NOTELINK.class), 
    @StructureMember(name = "ExtDataLen", type = short.class, unsigned = true), /* length of extended action data, e.g. compiled script */
    @StructureMember(name = "ActionDataDesignType", type = short.class, unsigned = true), /* this is the design type for the named folder or view named in the ActionString */
    @StructureMember(name = "spare", type = int[].class, length = 2), /* reserved for future use */
    /* Followed by the Action Name string */
})
public interface ViewmapActionRecord extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("bHighlightTouch")
  int getbHighlightTouch();

  @StructureGetter("bHighlightCurrent")
  int getbHighlightCurrent();

  @StructureGetter("HLOutlineColor")
  int getHLOutlineColor();

  @StructureGetter("HLFillColor")
  int getHLFillColor();

  @StructureGetter("ClickAction")
  int getClickAction();

  @StructureGetter("ActionStringLen")
  int getActionStringLen();

  @StructureGetter("HLOutlineWidth")
  int getHLOutlineWidth();

  @StructureGetter("HLOutlineStyle")
  int getHLOutlineStyle();

  @StructureGetter("LinkInfo")
  NOTELINK getLinkInfo();

  @StructureGetter("ExtDataLen")
  int getExtDataLen();

  @StructureGetter("ActionDataDesignType")
  int getActionDataDesignType();

  @StructureGetter("spare")
  int[] getspare();

  @StructureSetter("bHighlightTouch")
  ViewmapActionRecord setbHighlightTouch(int bHighlightTouch);

  @StructureSetter("bHighlightCurrent")
  ViewmapActionRecord setbHighlightCurrent(int bHighlightCurrent);

  @StructureSetter("HLOutlineColor")
  ViewmapActionRecord setHLOutlineColor(int hLOutlineColor);

  @StructureSetter("HLFillColor")
  ViewmapActionRecord setHLFillColor(int hLFillColor);

  @StructureSetter("ClickAction")
  ViewmapActionRecord setClickAction(int clickAction);

  @StructureSetter("ActionStringLen")
  ViewmapActionRecord setActionStringLen(int actionStringLen);

  @StructureSetter("HLOutlineWidth")
  ViewmapActionRecord setHLOutlineWidth(int hLOutlineWidth);

  @StructureSetter("HLOutlineStyle")
  ViewmapActionRecord setHLOutlineStyle(int hLOutlineStyle);

  @StructureSetter("ExtDataLen")
  ViewmapActionRecord setExtDataLen(int extDataLen);

  @StructureSetter("ActionDataDesignType")
  ViewmapActionRecord setActionDataDesignType(int actionDataDesignType);

  default String getActionName() {
    return StructureSupport.extractStringValue(
      this,
      0, // The total of all variable elements before this one
      this.getActionStringLen()  // the length of this element
    );
  }
  
}
