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
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

@StructureDefinition(name = "CDEMBEDDEDSCHEDCTL", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDEmbeddedSchedulerControl.Flag.class, bitfield = true),
    @StructureMember(name = "TargetFrameLength", type = short.class, unsigned = true),
    @StructureMember(name = "DisplayStartDTItemFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "HrsPerDayItemFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "ReqPeopleItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "BusyTimeColor", type = ColorValue.class),
    @StructureMember(name = "FreeTimeColor", type = ColorValue.class),
    @StructureMember(name = "NoDataColor", type = ColorValue.class),
    @StructureMember(name = "DataRestrictedColor", type = ColorValue.class),
    @StructureMember(name = "GridLineColor", type = ColorValue.class),
    @StructureMember(name = "NameColWidth", type = short.class, unsigned = true),
    @StructureMember(name = "PeopleLines", type = short.class, unsigned = true),
    @StructureMember(name = "RoomsLines", type = short.class, unsigned = true),
    @StructureMember(name = "ResourcesLines", type = short.class, unsigned = true),
    @StructureMember(name = "SpareWORD", type = short[].class, length = 5),
    @StructureMember(name = "SpareDWORD", type = int[].class, length = 13)
})
public interface CDEmbeddedSchedulerControl extends RichTextRecord<WSIG> {

  enum Flag implements INumberEnum<Integer> {
    USE_COLORS1(RichTextConstants.EMBEDDEDSCHED_FLAG_USE_COLORS1),
    NO_INITFROMITEMS(RichTextConstants.EMBEDDEDSCHED_FLAG_NO_INITFROMITEMS),
    NO_REFRESHFROMITEMS(RichTextConstants.EMBEDDEDSCHED_FLAG_NO_REFRESHFROMITEMS),
    ALLOW_FILTERING(RichTextConstants.EMBEDDEDSCHED_FLAG_ALLOW_FILTERING), 
    USE_COLORS2(RichTextConstants.EMBEDDEDSCHED_FLAG_USE_COLORS2),

    NO_SHOWLEGEND(RichTextConstants.EMBEDDEDSCHED_FLAG_NO_SHOWLEGEND),   
    SHOWINTERVALINDICATOR(RichTextConstants.EMBEDDEDSCHED_FLAG_SHOWINTERVALINDICATOR),  

    SHOW_TWISTIES(RichTextConstants.EMBEDDEDSCHED_FLAG_SHOW_TWISTIES),   
    ALLOW_EDIT_INPLACE(RichTextConstants.EMBEDDEDSCHED_FLAG_ALLOW_EDIT_INPLACE), 

    ATTENDEE_WIDTH_DEFINED(RichTextConstants.EMBEDDEDSCHED_FLAG_ATTENDEE_WIDTH_DEFINED),
    ATTENDEE_WIDTH_FIXED(RichTextConstants.EMBEDDEDSCHED_FLAG_ATTENDEE_WIDTH_FIXED),  

    PEOPLE_INVISIBLE(RichTextConstants.EMBEDDEDSCHED_FLAG_PEOPLE_INVISIBLE),
    ROOMS_VISIBLE(RichTextConstants.EMBEDDEDSCHED_FLAG_ROOMS_VISIBLE),   
    RESOURCES_VISIBLE(RichTextConstants.EMBEDDEDSCHED_FLAG_RESOURCES_VISIBLE),  

    PEOPLE_FIXEDHEIGHT(RichTextConstants.EMBEDDEDSCHED_FLAG_PEOPLE_FIXEDHEIGHT), 
    ROOMS_FIXEDHEIGHT(RichTextConstants.EMBEDDEDSCHED_FLAG_ROOMS_FIXEDHEIGHT),  
    RESOURCES_FIXEDHEIGHT(RichTextConstants.EMBEDDEDSCHED_FLAG_RESOURCES_FIXEDHEIGHT), 
    ATTENDEE_LINES_DEFINED(RichTextConstants.EMBEDDEDSCHED_FLAG_ATTENDEE_LINES_DEFINED),
    RTL_READING(RichTextConstants.EMBEDDEDSCHED_FLAG_RTL_READING),
    NO_LAUNCH(RichTextConstants.EMBEDDEDSCHED_FLAG_NO_LAUNCH),  
    SHOW_CHECKBOXES(RichTextConstants.EMBEDDEDSCHED_FLAG_SHOW_CHECKBOXES);
    
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
  
  @StructureSetter("Flags")
  CDEmbeddedSchedulerControl setFlags(Collection<Flag> flags);
  
  @StructureGetter("TargetFrameLength")
  int getTargetFrameLength();
  
  @StructureSetter("TargetFrameLength")
  CDEmbeddedSchedulerControl setTargetFrameLength(int targetFrameLength);
  
  @StructureGetter("DisplayStartDTItemFormulaLength")
  int getDisplayStartDTItemFormulaLength();
  
  @StructureSetter("DisplayStartDTItemFormulaLength")
  CDEmbeddedSchedulerControl setDisplayStartDTItemFormulaLength(int displayStartDTItemFormulaLength);
  
  @StructureGetter("HrsPerDayItemFormulaLength")
  int getHrsPerDayItemFormulaLength();
  
  @StructureSetter("HrsPerDayItemFormulaLength")
  CDEmbeddedSchedulerControl setHrsPerDayItemFormulaLength(int hrsPerDayItemFormulaLength);
  
  @StructureGetter("ReqPeopleItemsFormulaLength")
  int getReqPeopleItemsFormulaLength();
  
  @StructureSetter("ReqPeopleItemsFormulaLength")
  CDEmbeddedSchedulerControl setReqPeopleItemsFormulaLength(int reqPeopleItemsFormulaLength);
  
  @StructureGetter("BusyTimeColor")
  ColorValue getBusyTimeColor();

  @StructureGetter("FreeTimeColor")
  ColorValue getFreeTimeColor();
  
  @StructureGetter("NoDataColor")
  ColorValue getNoDataColor();

  @StructureGetter("DataRestrictedColor")
  ColorValue getDataRestrictedColor();
  
  @StructureGetter("GridLineColor")
  ColorValue getGridLineColor();
  
  @StructureGetter("NameColWidth")
  int getNameColWidth();
  
  @StructureSetter("NameColWidth")
  CDEmbeddedSchedulerControl setNameColWidth(int nameColWidth);
  
  @StructureGetter("PeopleLines")
  int getPeopleLines();
  
  @StructureSetter("PeopleLines")
  CDEmbeddedSchedulerControl setPeopleLines(int peopleLines);
  
  @StructureGetter("RoomsLines")
  int getRoomsLines();
  
  @StructureSetter("RoomsLines")
  CDEmbeddedSchedulerControl setRoomsLines(int roomsLines);
  
  @StructureGetter("ResourcesLines")
  int getResourcesLines();
  
  @StructureSetter("ResourcesLines")
  CDEmbeddedSchedulerControl setResourcesLines(int resourcesLines);
  
  default String getTargetFrameName() {
    return StructureSupport.extractStringValue(this, 
        0, 
        this.getTargetFrameLength());
  }
  
  default CDEmbeddedSchedulerControl setTargetFrameName(String name) {
    return StructureSupport.writeStringValue(this, 
        0, 
        this.getTargetFrameLength(), 
        name, 
        this::setTargetFrameLength);
  }
  
  /**
   * Retrieves the formula for DisplayStartDTItem if it is set
   *
   * @return an {@link Optional} describing the formula for DisplayStartDTItem,
   *          or an optional one if no formula is set
   */
  default Optional<String> getDisplayStartDTItemFormula() {
    if (this.getDisplayStartDTItemFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getTargetFrameLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getDisplayStartDTItemFormulaLength()
      )
    );
  }

  default void setDisplayStartDTItemFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getTargetFrameLength(),
          this.getDisplayStartDTItemFormulaLength(), 
          newFormula, 
          this::setDisplayStartDTItemFormulaLength);
  }
  
  /**
   * Retrieves the formula for HrsPerDayItem if it is set
   *
   * @return an {@link Optional} describing the formula for HrsPerDayItem,
   *          or an optional one if no formula is set
   */
  default Optional<String> getHrsPerDayItemFormula() {
    if (this.getHrsPerDayItemFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getTargetFrameLength() +
        this.getDisplayStartDTItemFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getHrsPerDayItemFormulaLength()
      )
    );
  }

  default void setHrsPerDayItemFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getTargetFrameLength() + 
          this.getDisplayStartDTItemFormulaLength(),
          this.getHrsPerDayItemFormulaLength(), 
          newFormula, 
          this::setHrsPerDayItemFormulaLength);
  }
  
  /**
   * Retrieves the formula for RequiredPeopleItems if it is set
   *
   * @return an {@link Optional} describing the formula for RequiredPeopleItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getReqPeopleItemsFormula() {
    if (this.getReqPeopleItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getTargetFrameLength() +
        this.getDisplayStartDTItemFormulaLength() +
        this.getHrsPerDayItemFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getReqPeopleItemsFormulaLength()
      )
    );
  }

  default void setReqPeopleItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getTargetFrameLength() + 
          this.getDisplayStartDTItemFormulaLength() + 
          this.getHrsPerDayItemFormulaLength(),
          this.getReqPeopleItemsFormulaLength(), 
          newFormula, 
          this::setReqPeopleItemsFormulaLength);
  }
}
