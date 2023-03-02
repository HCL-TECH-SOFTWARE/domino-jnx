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

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.HotspotType;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ActiveObject;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
@StructureDefinition(name = "CDHOTSPOTBEGIN", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Type", type = HotspotType.class),
    @StructureMember(name = "Flags", type = CDHotspotBegin.Flag.class, bitfield = true),
    @StructureMember(name = "DataLength", type = short.class, unsigned = true)
})
public interface CDHotspotBegin extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    BEGIN(RichTextConstants.HOTSPOTREC_RUNFLAG_BEGIN),
    END(RichTextConstants.HOTSPOTREC_RUNFLAG_END),
    BOX(RichTextConstants.HOTSPOTREC_RUNFLAG_BOX),
    NOBORDER(RichTextConstants.HOTSPOTREC_RUNFLAG_NOBORDER),
    FORMULA(RichTextConstants.HOTSPOTREC_RUNFLAG_FORMULA), /*	Popup is a formula, not text. */
    MOVIE(RichTextConstants.HOTSPOTREC_RUNFLAG_MOVIE), /*	File is a QuickTime movie. */
    IGNORE(RichTextConstants.HOTSPOTREC_RUNFLAG_IGNORE), /*	Run is for backward compatibility
                                                         (i.e. ignore the run)
                                                         */
    ACTION(RichTextConstants.HOTSPOTREC_RUNFLAG_ACTION), /*	Hot region executes a canned action	*/
    SCRIPT(RichTextConstants.HOTSPOTREC_RUNFLAG_SCRIPT), /*	Hot region executes a script.	*/
    INOTES(RichTextConstants.HOTSPOTREC_RUNFLAG_INOTES),
    ISMAP(RichTextConstants.HOTSPOTREC_RUNFLAG_ISMAP),
    INOTES_AUTO(RichTextConstants.HOTSPOTREC_RUNFLAG_INOTES_AUTO),
    ISMAP_INPUT(RichTextConstants.HOTSPOTREC_RUNFLAG_ISMAP_INPUT),

    SIGNED(RichTextConstants.HOTSPOTREC_RUNFLAG_SIGNED),
    ANCHOR(RichTextConstants.HOTSPOTREC_RUNFLAG_ANCHOR),
    COMPUTED(RichTextConstants.HOTSPOTREC_RUNFLAG_COMPUTED), /*	Used in conjunction
                                                             with computed hotspots.
                                                             */
    TEMPLATE(RichTextConstants.HOTSPOTREC_RUNFLAG_TEMPLATE), /*	used in conjunction
                                                             with embedded navigator
                                                             panes.
                                                             */
    HIGHLIGHT(RichTextConstants.HOTSPOTREC_RUNFLAG_HIGHLIGHT),
    EXTACTION(RichTextConstants.HOTSPOTREC_RUNFLAG_EXTACTION), /*  Hot region executes an extended action */
    NAMEDELEM(RichTextConstants.HOTSPOTREC_RUNFLAG_NAMEDELEM), /*	Hot link to a named element */

    /*	Allow R6 dual action type buttons, e.g. client LotusScript, web JS */
    WEBJAVASCRIPT(RichTextConstants.HOTSPOTREC_RUNFLAG_WEBJAVASCRIPT);

    private final int value;

    Flag(final int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Integer getValue() {
      return this.value;
    }
  }

  @StructureGetter("DataLength")
  int getDataLength();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Type")
  Optional<HotspotType> getHotspotType();
  
  /**
   * Gets the raw value of the {@code Type} component of this structure.
   * 
   * @return the {@code Type} component as a {@code short}
   * @since 1.23.0
   */
  @StructureGetter("Type")
  short getHotspotTypeRaw();

  /**
   * Retrieves the subform name or formula for a {@link HotspotType#SUBFORM SUBFORM}-type
   * hotspot.
   *
   * @return an {@link Optional} describing the string name or formula, or an empty
   *         one if that does not apply
   * @throws IllegalStateException if the type is not {@link HotspotType#SUBFORM}
   */
  default Optional<String> getSubformValue() {
    Optional<HotspotType> type = this.getHotspotType();
    if (!type.isPresent() || type.get() != HotspotType.SUBFORM) {
      return Optional.empty();
    }
    if (this.getFlags().contains(Flag.FORMULA)) {
      return Optional.of(
        StructureSupport.extractCompiledFormula(
          this,
          0,
          this.getDataLength()
        )
      );
    } else {
      return Optional.of(
        StructureSupport.extractStringValue(
          this,
          0,
          this.getDataLength() - 1 // null terminated
        )
      );
    }
  }

  /**
   * Returns the display file name for a {@link HotspotType#FILE FILE}-type hotspot.
   * 
   * @return an {@link Optional} describing the display file name, or an empty
   *         one if this is not applicable
   */
  default Optional<String> getDisplayFileName() {
    Optional<HotspotType> type = this.getHotspotType();
    if(!type.isPresent() || type.get() != HotspotType.FILE) {
      return Optional.empty();
    }
    
    final ByteBuffer buf = this.getVariableData().duplicate();
    int pos = buf.position();
    int nullIndex = pos;
    try {
      while (buf.hasRemaining()) {
        if (buf.get() == 0) {
          nullIndex = buf.position() - 1;
          break;
        }
      }
    } finally {
      buf.position(pos);
    }
    // Now find the next one
    pos = ++nullIndex;
    buf.position(nullIndex);
    try {
      while (buf.hasRemaining()) {
        if (buf.get() == 0) {
          nullIndex = buf.position() - 1;
          break;
        }
      }
    } finally {
      buf.position(pos);
    }

    final byte[] lmbcs = new byte[nullIndex - pos];
    buf.get(lmbcs);
    return Optional.of(new String(lmbcs, NativeItemCoder.get().getLmbcsCharset()));
  }

  /**
   * Returns the unique file name for a {@link HotspotType#FILE FILE}-type hotspot.
   * 
   * @return an {@link Optional} describing the unique file name, or an empty
   *         one if this is not applicable
   */
  default Optional<String> getUniqueFileName() {
    Optional<HotspotType> type = this.getHotspotType();
    if(!type.isPresent() || type.get() != HotspotType.FILE) {
      return Optional.empty();
    }
    
    final ByteBuffer buf = this.getVariableData();
    final int pos = buf.position();
    int nullIndex = pos;
    try {
      while (buf.hasRemaining()) {
        final byte val = buf.get();
        if (val == 0) {
          nullIndex = buf.position() - 1;
          break;
        }
      }
    } finally {
      buf.position(pos);
    }

    final byte[] lmbcs = new byte[nullIndex - pos];
    buf.get(lmbcs);
    return Optional.of(new String(lmbcs, NativeItemCoder.get().getLmbcsCharset()));
  }
  
  /**
   * Returns the formula for this hotspot, if the {@link Flag#FORMULA} flag is
   * set.
   * 
   * @return an {@link Optional} describing the hotspot formula, or an empty
   *         one if this is not applicable
   * @since 1.1.2
   */
  default Optional<String> getFormula() {
    if(!getFlags().contains(Flag.FORMULA)) {
      return Optional.empty();
    }
    
    return Optional.of(StructureSupport.extractCompiledFormula(
      this,
      0,
      getDataLength()
    ));
  }
  
  /**
   * Returns the LotusScript for this hotspot, if the {@link Flag#SCRIPT} flag is
   * set.
   * 
   * @return an {@link Optional} describing the hotspot LotusScript, or an empty
   *         one if this is not applicable
   * @since 1.1.2
   */
  default Optional<String> getScript() {
    if(!getFlags().contains(Flag.SCRIPT)) {
      return Optional.empty();
    }
    
    return Optional.of(StructureSupport.extractStringValue(
      this,
      0,
      getDataLength()
    ));
  }
  
  /**
   * Returns the Simple Actions for this hotspot, if the {@link Flag#ACTION} flag is
   * set.
   * 
   * @return an {@link Optional} describing the Simple Action CD records, or an empty
   *         one if this is not applicable
   * @since 1.1.2
   */
  default Optional<List<RichTextRecord<?>>> getActions() {
    if(!getFlags().contains(Flag.ACTION)) {
      return Optional.empty();
    }
    
    ByteBuffer data = getVariableData();
    return Optional.of(NativeItemCoder.get().readMemoryRecords(data, RecordType.Area.TYPE_ACTION));
  }

  @StructureSetter("DataLength")
  CDHotspotBegin setDataLength(int dataLength);

  /**
   * Sets the variable data portion of this structure to the provided string
   * values. The structure must
   * have been allocated with enough space to hold the LMBCS-encoded versions of
   * both names and null
   * terminators for each.
   * <p>
   * This is for use when the hotspot type is {@link HotspotType#FILE}.
   * </p>
   * <p>
   * This method also sets the {@code DataLength} property to the appropriate
   * value.
   * </p>
   *
   * @param uniqueFileName  the internal unique name of the file attachment
   * @param displayFileName the display name of the file attachment
   * @return this record
   */
  default CDHotspotBegin setFileNames(final String uniqueFileName, final String displayFileName) {
    final byte[] uniqueFileNameAttachment = uniqueFileName.getBytes(NativeItemCoder.get().getLmbcsCharset());
    final byte[] fileNameToDisplayMem = displayFileName.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setFileNamesRaw(uniqueFileNameAttachment, fileNameToDisplayMem, true);

    return this;
  }

  /**
   * Sets the variable data portion of this structure to the provided
   * LMBCS-encoded string values. The
   * structure must have been allocated with enough space to hold both and, if
   * specified, the added
   * null values.
   * <p>
   * This is for use when the hotspot type is {@link HotspotType#FILE}.
   * </p>
   *
   * @param uniqueFileName  the internal unique name of the file attachment
   * @param displayFileName the display name of the file attachment
   * @param appendNulls     whether this method should append null bytes after
   *                        each name
   * @return this record
   */
  default CDHotspotBegin setFileNamesRaw(final byte[] uniqueFileName, final byte[] displayFileName, final boolean appendNulls) {
    final int len = uniqueFileName.length + displayFileName.length + (appendNulls ? 2 : 0);
    this.resizeVariableData(len);
    this.setDataLength(len);

    final ByteBuffer buf = this.getVariableData();
    buf.put(uniqueFileName);
    if (appendNulls) {
      buf.put((byte) 0);
    }
    buf.put(displayFileName);
    if (appendNulls) {
      buf.put((byte) 0);
    }
    return this;
  }

  @StructureSetter("Flags")
  CDHotspotBegin setFlags(Collection<Flag> flags);

  @StructureSetter("Type")
  CDHotspotBegin setHotspotType(HotspotType type);
  
  /**
   * Sets the raw value of the {@code Type} component of this structure.
   * 
   * @param type the new {@code Type} component as a {@code short}
   * @return this structure
   * @since 1.23.0
   */
  @StructureSetter("Type")
  CDHotspotBegin setHotspotTypeRaw(short type);
  
  /**
   * Retrieves the {@link ActiveObject} associated with this hotspot, if its
   * type is {@link HotspotType#ACTIVEOBJECT}.
   * 
   * @return an {@link Optional} describing the associated {@link ActiveObject},
   *         or an empty one if this is not an active object
   * @since 1.0.44
   */
  default Optional<ActiveObject> getActiveObject() {
    Optional<HotspotType> type = this.getHotspotType();
    if(!type.isPresent() || type.get() != HotspotType.ACTIVEOBJECT) {
      return Optional.empty();
    }
    
    ByteBuffer buf = getVariableData();
    return Optional.of(MemoryStructureWrapperService.get().wrapStructure(ActiveObject.class, buf));
  }
}
