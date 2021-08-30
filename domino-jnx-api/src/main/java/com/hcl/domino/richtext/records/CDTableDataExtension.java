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

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDTABLEDATAEXTENSION",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "dwColumnSizeToFitBits1", type = int.class),
    @StructureMember(name = "dwColumnSizeToFitBits2", type = int.class),
    @StructureMember(name = "wEqualSizeTabsWidthX", type = short.class),
    @StructureMember(name = "wTabsIndentWidthX", type = short.class),
    @StructureMember(name = "wAvailable3", type = short.class),
    @StructureMember(name = "wAvailable4", type = short.class),
    @StructureMember(name = "dwAvailable5", type = int.class),
    @StructureMember(name = "dwAvailable6", type = int.class),
    @StructureMember(name = "dwAvailable7", type = int.class),
    @StructureMember(name = "dwAvailable8", type = int.class),
    @StructureMember(name = "dwAvailable9", type = int.class),
    @StructureMember(name = "wcTabLabelFont", type = short.class, unsigned = true),
    @StructureMember(name = "wAvailableLength11", type = short.class, unsigned = true),
    @StructureMember(name = "wAvailableLength12", type = short.class, unsigned = true),
    @StructureMember(name = "wExtension2Length", type = short.class, unsigned = true)
  }
)
public interface CDTableDataExtension extends RichTextRecord<WSIG> {
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("dwColumnSizeToFitBits1")
  int getColumnSizeToFitBits1();
  
  @StructureSetter("dwColumnSizeToFitBits1")
  CDTableDataExtension setColumnSizeToFitBits1(int bits);
  
  @StructureGetter("dwColumnSizeToFitBits2")
  int getColumnSizeToFitBits2();
  
  @StructureSetter("dwColumnSizeToFitBits2")
  CDTableDataExtension setColumnSizeToFitBits2(int bits);
  
  @StructureGetter("wEqualSizeTabsWidthX")
  short getEqualSizeTabsWidth();
  
  @StructureSetter("wEqualSizeTabsWidthX")
  CDTableDataExtension setEqualSizeTabsWidthX(short width);
  
  @StructureGetter("wTabsIndentWidthX")
  short getTabsIndentWidth();
  
  @StructureSetter("wTabsIndentWidthX")
  CDTableDataExtension setTabsIndentWidthX(short width);
  
  @StructureGetter("wcTabLabelFont")
  int getTabLabelFontLength();
  
  @StructureSetter("wcTabLabelFont")
  CDTableDataExtension setTabLabelFontLength(int len);
  
  @StructureGetter("wAvailableLength11")
  int getAvailableLength11();
  
  @StructureSetter("wAvailableLength11")
  CDTableDataExtension setAvailableLength11(int len);
  
  @StructureGetter("wAvailableLength12")
  int getAvailableLength12();
  
  @StructureSetter("wAvailableLength12")
  CDTableDataExtension setAvailableLength12(int len);
  
  @StructureGetter("wExtension2Length")
  int getExtension2Length();
  
  @StructureSetter("wExtension2Length")
  CDTableDataExtension setExtension2Length(int len);
  
  // TODO
  /* The wcTabLabelFont variable data defined below is actually 
   *   a FONTID followed by a DWORD followed by a COLOR_VALUE.
   *   Some Notes6 pre-release versions only wrote the FONTID/DWORD combination 
   *   without the trailing COLOR_VALUE, so need to verify that 
   *   wcTabLabelFont is at least large enough to hold FONTID, DWORD, and COLOR_VALUE 
   *   before trying to read off the COLOR_VALUE.  SDK consumers should
   *   ignore the value of the DWORD when reading this structure and
   *   write the DWORD value as 0 when writing this structure
   */
}
