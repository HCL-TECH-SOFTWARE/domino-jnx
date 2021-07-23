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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.2
 */
@StructureDefinition(name = "CDHOTSPOTBEGIN", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Type", type = CDHotspotBegin.Type.class),
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

  enum Type implements INumberEnum<Short> {
    POPUP(RichTextConstants.HOTSPOTREC_TYPE_POPUP),
    HOTREGION(RichTextConstants.HOTSPOTREC_TYPE_HOTREGION),
    BUTTON(RichTextConstants.HOTSPOTREC_TYPE_BUTTON),
    FILE(RichTextConstants.HOTSPOTREC_TYPE_FILE),
    SECTION(RichTextConstants.HOTSPOTREC_TYPE_SECTION),
    ANY(RichTextConstants.HOTSPOTREC_TYPE_ANY),
    HOTLINK(RichTextConstants.HOTSPOTREC_TYPE_HOTLINK),
    BUNDLE(RichTextConstants.HOTSPOTREC_TYPE_BUNDLE),
    V4_SECTION(RichTextConstants.HOTSPOTREC_TYPE_V4_SECTION),
    SUBFORM(RichTextConstants.HOTSPOTREC_TYPE_SUBFORM),
    ACTIVEOBJECT(RichTextConstants.HOTSPOTREC_TYPE_ACTIVEOBJECT),
    OLERICHTEXT(RichTextConstants.HOTSPOTREC_TYPE_OLERICHTEXT),
    EMBEDDEDVIEW(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDVIEW),
    EMBEDDEDFPANE(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDFPANE),
    EMBEDDEDNAV(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDNAV),
    MOUSEOVER(RichTextConstants.HOTSPOTREC_TYPE_MOUSEOVER),
    FILEUPLOAD(RichTextConstants.HOTSPOTREC_TYPE_FILEUPLOAD),
    EMBEDDEDOUTLINE(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDOUTLINE),
    EMBEDDEDCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDCTL),
    EMBEDDEDCALENDARCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDCALENDARCTL),
    EMBEDDEDSCHEDCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDSCHEDCTL),
    RCLINK(RichTextConstants.HOTSPOTREC_TYPE_RCLINK),
    EMBEDDEDEDITCTL(RichTextConstants.HOTSPOTREC_TYPE_EMBEDDEDEDITCTL),
    CONTACTLISTCTL(RichTextConstants.HOTSPOTREC_TYPE_CONTACTLISTCTL);

    private final short value;

    Type(final short value) {
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

  @StructureGetter("DataLength")
  int getDataLength();

  default String getDisplayFileName() {
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
    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
  }

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Type")
  Type getHotspotType();

  /**
   * Retrieves the subform name or formula for a {@link Type#SUBFORM SUBFORM}-type
   * hotspot.
   *
   * @return a string name or formula
   * @throws IllegalStateException if the type is not {@link Type#SUBFORM}
   */
  default String getSubformValue() {
    if (this.getHotspotType() != Type.SUBFORM) {
      throw new IllegalStateException(
          MessageFormat.format("Cannot retrieve the subform name for a hotspot of type {0}", this.getHotspotType()));
    }
    if (this.getFlags().contains(Flag.FORMULA)) {
      return StructureSupport.extractCompiledFormula(
          this,
          0,
          this.getDataLength());
    } else {
      return StructureSupport.extractStringValue(
          this,
          0,
          this.getDataLength() - 1 // null terminated
      );
    }
  }

  default String getUniqueFileName() {
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
    return new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
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
   * This is for use when the hotspot type is {@link Type#FILE}.
   * </p>
   * <p>
   * This method also sets the {@code DataLength} property to the appropriate
   * value.
   * </p>
   *
   * @param uniqueFileName  the internal unique name of the file attachment
   * @param displayFileName the display name of the file attachment
   * @param appendNulls     whether this method should append null bytes after
   *                        each name
   * @return this record
   */
  default CDHotspotBegin setFileNames(final String uniqueFileName, final String displayFileName) {
    final byte[] uniqueFileNameAttachment = uniqueFileName.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
    final byte[] fileNameToDisplayMem = displayFileName.getBytes(Charset.forName("LMBCS-native")); //$NON-NLS-1$
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
   * This is for use when the hotspot type is {@link Type#FILE}.
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
  CDHotspotBegin setHotspotType(Type type);
}
