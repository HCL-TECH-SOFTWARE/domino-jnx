/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

@StructureDefinition(name = "CDEMBEDDEDSCHEDCTLEXTRA", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "FixedPartLength", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDEmbeddedSchedulerControlExtra.Flag.class, bitfield = true),
    @StructureMember(name = "SchedHdrBkgndColor", type = ColorValue.class),
    @StructureMember(name = "SchedHdrFontColor", type = ColorValue.class),
    @StructureMember(name = "SchedBkgndColor", type = ColorValue.class),
    @StructureMember(name = "NameHdrBkgndColor", type = ColorValue.class),
    @StructureMember(name = "NameHdrFontColor", type = ColorValue.class),
    @StructureMember(name = "NameBkgndColor", type = ColorValue.class),
    @StructureMember(name = "NameMouseBkgndColor", type = ColorValue.class),
    @StructureMember(name = "NameSelectBkgndColor", type = ColorValue.class),
    @StructureMember(name = "NameFontColor", type = ColorValue.class),
    @StructureMember(name = "NameMouseFontColor", type = ColorValue.class),
    @StructureMember(name = "NameSelectFontColor", type = ColorValue.class),
    @StructureMember(name = "OptPeopleItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "ReqRoomsItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "OptRoomsItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "ReqResourcesItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "OptResourcesItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "IntervalStartDTItemFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "IntervalEndDTItemFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "SchedulerNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "PeopleTitleLength", type = short.class, unsigned = true),
    @StructureMember(name = "RoomsTitleLength", type = short.class, unsigned = true),
    @StructureMember(name = "ResourcesTitleLength", type = short.class, unsigned = true),
    @StructureMember(name = "IntervalChangeEventFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "ProfileColor", type = ColorValue.class),
    @StructureMember(name = "SchedDetailItemsFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "SuggBkgndColor", type = ColorValue.class),
    @StructureMember(name = "SuggMouseBkgndColor", type = ColorValue.class),
    @StructureMember(name = "SuggSelectBkgndColor", type = ColorValue.class),
    @StructureMember(name = "DetailDisplayFormFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "SuggestionsAvailEventFormulaLength", type = short.class, unsigned = true),
    @StructureMember(name = "wReserved", type = short[].class, length = 2),
    @StructureMember(name = "dwReserved", type = int[].class, length = 6)
})
public interface CDEmbeddedSchedulerControlExtra extends RichTextRecord<WSIG> {

  enum Flag implements INumberEnum<Integer> {
    PEOPLE_TITLE_FORMULA(RichTextConstants.EMBEDDEDSCHEDEXT_FLAG_PEOPLE_TITLE_FORMULA), 
    ROOMS_TITLE_FORMULA(RichTextConstants.EMBEDDEDSCHEDEXT_FLAG_ROOMS_TITLE_FORMULA),
    RESOURCES_TITLE_FORMULA(RichTextConstants.EMBEDDEDSCHEDEXT_FLAG_RESOURCES_TITLE_FORMULA),
    SUGG_COLORS_DEFINED(RichTextConstants.EMBEDDEDSCHEDEXT_FLAG_SUGG_COLORS_DEFINED);

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
  CDEmbeddedSchedulerControlExtra setFlags(Collection<Flag> flags);
  
  @StructureGetter("FixedPartLength")
  int getFixedPartLength();
  
  @StructureSetter("FixedPartLength")
  CDEmbeddedSchedulerControlExtra setFixedPartLength(int fixedPartLength);
  
  @StructureGetter("SchedHdrBkgndColor")
  ColorValue getSchedHdrBkgndColor();

  @StructureGetter("SchedHdrFontColor")
  ColorValue getSchedHdrFontColor();
  
  @StructureGetter("SchedBkgndColor")
  ColorValue getSchedBkgndColor();

  @StructureGetter("NameHdrBkgndColor")
  ColorValue getNameHdrBkgndColor();
  
  @StructureGetter("NameHdrFontColor")
  ColorValue getNameHdrFontColor();
  
  @StructureGetter("NameBkgndColor")
  ColorValue getNameBkgndColor();

  @StructureGetter("NameMouseBkgndColor")
  ColorValue getNameMouseBkgndColor();
  
  @StructureGetter("NameSelectBkgndColor")
  ColorValue getNameSelectBkgndColor();

  @StructureGetter("NameFontColor")
  ColorValue getNameFontColor();
  
  @StructureGetter("NameMouseFontColor")
  ColorValue getNameMouseFontColor();
  
  @StructureGetter("NameSelectFontColor")
  ColorValue getNameSelectFontColor();
  
  @StructureGetter("OptPeopleItemsFormulaLength")
  int getOptPeopleItemsFormulaLength();
  
  @StructureSetter("OptPeopleItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setOptPeopleItemsFormulaLength(int optPeopleItemsFormulaLength);
  
  @StructureGetter("ReqRoomsItemsFormulaLength")
  int getReqRoomsItemsFormulaLength();
  
  @StructureSetter("ReqRoomsItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setReqRoomsItemsFormulaLength(int reqRoomsItemsFormulaLength);
  
  @StructureGetter("OptRoomsItemsFormulaLength")
  int getOptRoomsItemsFormulaLength();
  
  @StructureSetter("OptRoomsItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setOptRoomsItemsFormulaLength(int optRoomsItemsFormulaLength);
  
  @StructureGetter("ReqResourcesItemsFormulaLength")
  int getReqResourcesItemsFormulaLength();
  
  @StructureSetter("ReqResourcesItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setReqResourcesItemsFormulaLength(int reqResourcesItemsFormulaLength);
  
  @StructureGetter("OptResourcesItemsFormulaLength")
  int getOptResourcesItemsFormulaLength();
  
  @StructureSetter("OptResourcesItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setOptResourcesItemsFormulaLength(int optResourcesItemsFormulaLength);
  
  @StructureGetter("IntervalStartDTItemFormulaLength")
  int getIntervalStartDTItemFormulaLength();
  
  @StructureSetter("IntervalStartDTItemFormulaLength")
  CDEmbeddedSchedulerControlExtra setIntervalStartDTItemFormulaLength(int intervalStartDTItemFormulaLength);
  
  @StructureGetter("IntervalEndDTItemFormulaLength")
  int getIntervalEndDTItemFormulaLength();
  
  @StructureSetter("IntervalEndDTItemFormulaLength")
  CDEmbeddedSchedulerControlExtra setIntervalEndDTItemFormulaLength(int intervalEndDTItemFormulaLength);
  
  @StructureGetter("SchedulerNameLength")
  int getSchedulerNameLength();
  
  @StructureSetter("SchedulerNameLength")
  CDEmbeddedSchedulerControlExtra setSchedulerNameLength(int schedulerNameLength);
  
  @StructureGetter("PeopleTitleLength")
  int getPeopleTitleLength();
  
  @StructureSetter("PeopleTitleLength")
  CDEmbeddedSchedulerControlExtra setPeopleTitleLength(int peopleTitleLength);
  
  @StructureGetter("RoomsTitleLength")
  int getRoomsTitleLength();
  
  @StructureSetter("RoomsTitleLength")
  CDEmbeddedSchedulerControlExtra setRoomsTitleLength(int roomsTitleLength);
  
  @StructureGetter("ResourcesTitleLength")
  int getResourcesTitleLength();
  
  @StructureSetter("ResourcesTitleLength")
  CDEmbeddedSchedulerControlExtra setResourcesTitleLength(int resourcesTitleLength);
  
  @StructureGetter("IntervalChangeEventFormulaLength")
  int getIntervalChangeEventFormulaLength();
  
  @StructureSetter("IntervalChangeEventFormulaLength")
  CDEmbeddedSchedulerControlExtra setIntervalChangeEventFormulaLength(int intervalChangeEventFormulaLength);
  
  @StructureGetter("ProfileColor")
  ColorValue getProfileColor();
  
  @StructureGetter("SchedDetailItemsFormulaLength")
  int getSchedDetailItemsFormulaLength();
  
  @StructureSetter("SchedDetailItemsFormulaLength")
  CDEmbeddedSchedulerControlExtra setSchedDetailItemsFormulaLength(int schedDetailItemsFormulaLength);
  
  @StructureGetter("SuggBkgndColor")
  ColorValue getSuggBkgndColor();
  
  @StructureGetter("SuggMouseBkgndColor")
  ColorValue getSuggMouseBkgndColor();
  
  @StructureGetter("SuggSelectBkgndColor")
  ColorValue getSuggSelectBkgndColor();
  
  @StructureGetter("DetailDisplayFormFormulaLength")
  int getDetailDisplayFormFormulaLength();
  
  @StructureSetter("DetailDisplayFormFormulaLength")
  CDEmbeddedSchedulerControlExtra setDetailDisplayFormFormulaLength(int detailDisplayFormFormulaLength);
  
  @StructureGetter("SuggestionsAvailEventFormulaLength")
  int getSuggestionsAvailEventFormulaLength();
  
  @StructureSetter("SuggestionsAvailEventFormulaLength")
  CDEmbeddedSchedulerControlExtra setSuggestionsAvailEventFormulaLength(int suggestionsAvailEventFormulaLength);
  
  /**
   * Retrieves the formula for OptPeopleItems if it is set
   *
   * @return an {@link Optional} describing the formula for OptPeopleItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getOptPeopleItemsFormula() {
    if (this.getOptPeopleItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = 0;
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getOptPeopleItemsFormulaLength()
      )
    );
  }

  default void setOptPeopleItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          0,
          this.getOptPeopleItemsFormulaLength(), 
          newFormula, 
          this::setOptPeopleItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for ReqRoomsItems if it is set
   *
   * @return an {@link Optional} describing the formula for ReqRoomsItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getReqRoomsItemsFormula() {
    if (this.getReqRoomsItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getReqRoomsItemsFormulaLength()
      )
    );
  }

  default void setReqRoomsItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength(),
          this.getReqRoomsItemsFormulaLength(), 
          newFormula, 
          this::setReqRoomsItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for OptRoomsItems if it is set
   *
   * @return an {@link Optional} describing the formula for OptRoomsItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getOptRoomsItemsFormula() {
    if (this.getOptRoomsItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getOptRoomsItemsFormulaLength()
      )
    );
  }

  default void setOptRoomsItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength(),
          this.getOptRoomsItemsFormulaLength(), 
          newFormula, 
          this::setOptRoomsItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for ReqResourcesItems if it is set
   *
   * @return an {@link Optional} describing the formula for ReqResourcesItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getReqResourcesItemsFormula() {
    if (this.getReqResourcesItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getReqResourcesItemsFormulaLength()
      )
    );
  }

  default void setReqResourcesItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength(),
          this.getReqResourcesItemsFormulaLength(), 
          newFormula, 
          this::setReqResourcesItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for OptResourcesItems if it is set
   *
   * @return an {@link Optional} describing the formula for OptResourcesItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getOptResourcesItemsFormula() {
    if (this.getOptResourcesItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getOptResourcesItemsFormulaLength()
      )
    );
  }

  default void setOptResourcesItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength(),
          this.getOptResourcesItemsFormulaLength(), 
          newFormula, 
          this::setOptResourcesItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for IntervalStartDTItem if it is set
   *
   * @return an {@link Optional} describing the formula for IntervalStartDTItem,
   *          or an optional one if no formula is set
   */
  default Optional<String> getIntervalStartDTItemFormula() {
    if (this.getIntervalStartDTItemFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getIntervalStartDTItemFormulaLength()
      )
    );
  }

  default void setIntervalStartDTItemFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength(),
          this.getIntervalStartDTItemFormulaLength(), 
          newFormula, 
          this::setIntervalStartDTItemFormulaLength);
  }
  
  /**
   * Retrieves the formula for IntervalEndDTItem if it is set
   *
   * @return an {@link Optional} describing the formula for IntervalEndDTItem,
   *          or an optional one if no formula is set
   */
  default Optional<String> getIntervalEndDTItemFormula() {
    if (this.getIntervalEndDTItemFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getIntervalEndDTItemFormulaLength()
      )
    );
  }

  default void setIntervalEndDTItemFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength() +
          this.getIntervalStartDTItemFormulaLength(),
          this.getIntervalEndDTItemFormulaLength(), 
          newFormula, 
          this::setIntervalEndDTItemFormulaLength);
  }

  default String getSchedulerName() {
    return StructureSupport.extractStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength(), 
        this.getSchedulerNameLength());
  }
  
  default CDEmbeddedSchedulerControlExtra setSchedulerName(String name) {
    return StructureSupport.writeStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength(), 
        this.getSchedulerNameLength(), 
        name, 
        this::setSchedulerNameLength);
  }
  
  default String getPeopleTitle() {
    return StructureSupport.extractStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength(),
        this.getPeopleTitleLength());
  }
  
  default CDEmbeddedSchedulerControlExtra setPeopleTitle(String name) {
    return StructureSupport.writeStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength(),
        this.getPeopleTitleLength(),
        name, 
        this::setPeopleTitleLength);
  }
  
  default String getRoomsTitle() {
    return StructureSupport.extractStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength(),
        this.getRoomsTitleLength());
  }
  
  default CDEmbeddedSchedulerControlExtra setRoomsTitle(String name) {
    return StructureSupport.writeStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength(),
        this.getRoomsTitleLength(),
        name, 
        this::setRoomsTitleLength);
  }
  
  default String getResourcesTitle() {
    return StructureSupport.extractStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength(),
        this.getResourcesTitleLength());
  }
  
  default CDEmbeddedSchedulerControlExtra setResourcesTitle(String name) {
    return StructureSupport.writeStringValue(this, 
        this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength(),
        this.getResourcesTitleLength(),
        name, 
        this::setResourcesTitleLength);
  }
  
  /**
   * Retrieves the formula for IntervalChangeEvent if it is set
   *
   * @return an {@link Optional} describing the formula for IntervalChangeEvent,
   *          or an optional one if no formula is set
   */
  default Optional<String> getIntervalChangeEventFormula() {
    if (this.getIntervalChangeEventFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength() +
        this.getResourcesTitleLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getIntervalChangeEventFormulaLength()
      )
    );
  }

  default void setIntervalChangeEventFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength() +
          this.getIntervalStartDTItemFormulaLength() +
          this.getIntervalEndDTItemFormulaLength() + 
          this.getSchedulerNameLength() +
          this.getPeopleTitleLength() +
          this.getRoomsTitleLength() +
          this.getResourcesTitleLength(),
          this.getIntervalChangeEventFormulaLength(),
          newFormula, 
          this::setIntervalChangeEventFormulaLength);
  }
  
  /**
   * Retrieves the formula for SchedDetailItems if it is set
   *
   * @return an {@link Optional} describing the formula for SchedDetailItems,
   *          or an optional one if no formula is set
   */
  default Optional<String> getSchedDetailItemsFormula() {
    if (this.getSchedDetailItemsFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength() +
        this.getResourcesTitleLength()+
        this.getIntervalChangeEventFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getSchedDetailItemsFormulaLength()
      )
    );
  }

  default void setSchedDetailItemsFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength() +
          this.getIntervalStartDTItemFormulaLength() +
          this.getIntervalEndDTItemFormulaLength() + 
          this.getSchedulerNameLength() +
          this.getPeopleTitleLength() +
          this.getRoomsTitleLength() +
          this.getResourcesTitleLength() +
          this.getIntervalChangeEventFormulaLength(),
          this.getSchedDetailItemsFormulaLength(),
          newFormula, 
          this::setSchedDetailItemsFormulaLength);
  }
  
  /**
   * Retrieves the formula for DetailDisplayForm if it is set
   *
   * @return an {@link Optional} describing the formula for DetailDisplayForm,
   *          or an optional one if no formula is set
   */
  default Optional<String> getDetailDisplayFormFormula() {
    if (this.getDetailDisplayFormFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength() +
        this.getResourcesTitleLength()+
        this.getIntervalChangeEventFormulaLength() +
        this.getSchedDetailItemsFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getDetailDisplayFormFormulaLength()
      )
    );
  }

  default void setDetailDisplayFormFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength() +
          this.getIntervalStartDTItemFormulaLength() +
          this.getIntervalEndDTItemFormulaLength() + 
          this.getSchedulerNameLength() +
          this.getPeopleTitleLength() +
          this.getRoomsTitleLength() +
          this.getResourcesTitleLength() +
          this.getIntervalChangeEventFormulaLength() +
          this.getSchedDetailItemsFormulaLength(),
          this.getDetailDisplayFormFormulaLength(),
          newFormula, 
          this::setDetailDisplayFormFormulaLength);
  }
  
  /**
   * Retrieves the formula for SuggestionsAvailEvent if it is set
   *
   * @return an {@link Optional} describing the formula for SuggestionsAvailEvent,
   *          or an optional one if no formula is set
   */
  default Optional<String> getSuggestionsAvailEventFormula() {
    if (this.getSuggestionsAvailEventFormulaLength() <= 0) {
      return Optional.empty();
    }
    int preLen = this.getOptPeopleItemsFormulaLength() +
        this.getReqRoomsItemsFormulaLength() +
        this.getOptRoomsItemsFormulaLength() +
        this.getReqResourcesItemsFormulaLength() +
        this.getOptResourcesItemsFormulaLength() +
        this.getIntervalStartDTItemFormulaLength() +
        this.getIntervalEndDTItemFormulaLength() + 
        this.getSchedulerNameLength() +
        this.getPeopleTitleLength() +
        this.getRoomsTitleLength() +
        this.getResourcesTitleLength()+
        this.getIntervalChangeEventFormulaLength() +
        this.getSchedDetailItemsFormulaLength() +
        this.getDetailDisplayFormFormulaLength();
    
    return Optional.of(
      StructureSupport.extractCompiledFormula(
        this,
        preLen,
        this.getSuggestionsAvailEventFormulaLength()
      )
    );
  }

  default void setSuggestionsAvailEventFormula(String newFormula) {
      StructureSupport.writeCompiledFormula(this,
          this.getOptPeopleItemsFormulaLength() +
          this.getReqRoomsItemsFormulaLength() +
          this.getOptRoomsItemsFormulaLength() +
          this.getReqResourcesItemsFormulaLength() +
          this.getOptResourcesItemsFormulaLength() +
          this.getIntervalStartDTItemFormulaLength() +
          this.getIntervalEndDTItemFormulaLength() + 
          this.getSchedulerNameLength() +
          this.getPeopleTitleLength() +
          this.getRoomsTitleLength() +
          this.getResourcesTitleLength() +
          this.getIntervalChangeEventFormulaLength() +
          this.getSchedDetailItemsFormulaLength() +
          this.getDetailDisplayFormFormulaLength(),
          this.getSuggestionsAvailEventFormulaLength(),
          newFormula, 
          this::setSuggestionsAvailEventFormulaLength);
  }
}
