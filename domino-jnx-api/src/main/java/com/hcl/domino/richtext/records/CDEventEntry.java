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

import java.util.Optional;

import com.hcl.domino.design.action.EventId;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.32
 */
@StructureDefinition(
  name = "CDEVENTENTRY",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "wPlatform", type = CDEventEntry.Platform.class),
    @StructureMember(name = "wEventId", type = short.class),
    @StructureMember(name = "wActionType", type = CDEventEntry.ActionType.class),
    @StructureMember(name = "wReserved", type = short.class),
    @StructureMember(name = "dwReserved", type = int.class)
  }
)
public interface CDEventEntry extends RichTextRecord<WSIG> {
  enum Platform implements INumberEnum<Short> {
    CLIENT(RichTextConstants.PLATFORM_TYPE_CLIENT_ODS),
    WEB(RichTextConstants.PLATFORM_TYPE_WEB_ODS);

    private final short value;

    Platform(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }
  
  enum ActionType implements INumberEnum<Short> {
    FORMULA(RichTextConstants.ACTION_FORMULA),
    CANNED_ACTION(RichTextConstants.ACTION_CANNED_ACTION),
    LOTUS_SCRIPT(RichTextConstants.ACTION_LOTUS_SCRIPT),
    MISC(RichTextConstants.ACTION_MISC),
    COLLECTION_RULE(RichTextConstants.ACTION_COLLECTION_RULE),
    JAVA_FILE(RichTextConstants.ACTION_JAVA_FILE),
    JAVA(RichTextConstants.ACTION_JAVA),
    JAVASCRIPT(RichTextConstants.ACTION_JAVASCRIPT),
    JAVASCRIPT_COMMON(RichTextConstants.ACTION_JAVASCRIPT_COMMON),
    UNUSED(RichTextConstants.ACTION_UNUSED),
    SECTION_EDIT(RichTextConstants.ACTION_SECTION_EDIT),
    NULL(RichTextConstants.ACTION_NULL),
    PROPERTIES(RichTextConstants.ACTION_PROPERTIES),
    JSP(RichTextConstants.ACTION_JSP),
    HTML(RichTextConstants.ACTION_HTML),
    MAX(RichTextConstants.ACTION_MAX),
    OTHER(RichTextConstants.ACTION_OTHER),
    UNKNOWN(RichTextConstants.ACTION_UNKNOWN);

    private final short value;

    ActionType(final short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("wPlatform")
  Optional<Platform> getPlatform();
  
  /**
   * Retrieves the platform as a raw {@code short}.
   * 
   * @return the platform value as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wPlatform")
  short getPlatformRaw();
  
  @StructureSetter("wPlatform")
  CDEventEntry setPlatform(Platform platform);
  
  /**
   * Sets the platform value as a raw {@code short}.
   * 
   * @param platform the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wPlatform")
  CDEventEntry setPlatformRaw(short platform);
  
  @StructureGetter("wEventId")
  short getEventId();
  
  @StructureSetter("wEventId")
  CDEventEntry setEventId(short eventId);
  
  @StructureGetter("wActionType")
  Optional<ActionType> getActionType();
  
  /**
   * Retrieves the action type as a raw {@code short}.
   * 
   * @return the action type value as a {@code short}
   * @since 1.24.0
   */
  @StructureGetter("wPlatform")
  short getActionTypeRaw();
  
  @StructureSetter("wActionType")
  CDEventEntry setActionType(ActionType actionType);
  
  /**
   * Sets the action type value as a raw {@code short}.
   * 
   * @param actionType the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("wActionType")
  CDEventEntry setActionTypeRaw(short actionType);
  
  /**
   * Retrieves the HTML-type event for this action, if appropriate.
   * 
   * @return an {@link Optional} describing the {@link EventId} corresponding
   *         to the value of {@link #getEventId()}, or an empty one if it does
   *         not correspond to any
   */
  default Optional<EventId> getHtmlEventId() {
    return DominoEnumUtil.valueOf(EventId.class, getEventId());
  }
  
  @StructureSetter("wEventId")
  CDEventEntry setHtmlEventId(EventId id);
  
}
