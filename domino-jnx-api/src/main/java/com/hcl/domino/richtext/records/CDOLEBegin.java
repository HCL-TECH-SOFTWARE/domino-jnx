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
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.43
 */
@StructureDefinition(
  name = "CDOLEBEGIN",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Version", type = CDOLEBegin.Version.class),
    @StructureMember(name = "Flags", type = CDOLEBegin.Flag.class, bitfield = true),
    @StructureMember(name = "ClipFormat", type = CDOLEBegin.ClipFormat.class),
    @StructureMember(name = "AttachNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "ClassNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "TemplateNameLength", type = short.class, unsigned = true)
  }
)
public interface CDOLEBegin extends RichTextRecord<WSIG> {
  public enum Version implements INumberEnum<Short> {
    VERSION1(NotesConstants.NOTES_OLEVERSION1),
    VERSION2(NotesConstants.NOTES_OLEVERSION2);
    private final short value;
    private Version(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }

  public enum Flag implements INumberEnum<Integer> {
    /** The data is an OLE embedded OBJECT */
    OBJECT(NotesConstants.OLEREC_FLAG_OBJECT),
    /** The data is an OLE Link */
    LINK(NotesConstants.OLEREC_FLAG_LINK),
    /** If link, Link type == Automatic (hot) */
    AUTOLINK(NotesConstants.OLEREC_FLAG_AUTOLINK),
    /** If link, Link type == Manual (warm) */
    MANUALLINK(NotesConstants.OLEREC_FLAG_MANUALLINK),
    /** New object, just inserted */
    NEWOBJECT(NotesConstants.OLEREC_FLAG_NEWOBJECT),
    /** New object, just pasted */
    PASTED(NotesConstants.OLEREC_FLAG_PASTED),
    /** Object came from form and should be saved every time it changes in server */
    SAVEOBJWHENCHANGED(NotesConstants.OLEREC_FLAG_SAVEOBJWHENCHANGED),
    /** Object inherited from form, so don't visualize or object incapable of rendering itself. */
    NOVISUALIZE(NotesConstants.OLEREC_FLAG_NOVISUALIZE);
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

  public enum ClipFormat implements INumberEnum<Short> {
    /** CF_TEXT */
    TEXT(NotesConstants.DDEFORMAT_TEXT),
    /** CF_METAFILE or CF_METAFILEPICT */
    METAFILE(NotesConstants.DDEFORMAT_METAFILE),
    /** CF_BITMAP */
    BITMAP(NotesConstants.DDEFORMAT_BITMAP),
    /** Rich Text Format */
    RTF(NotesConstants.DDEFORMAT_RTF),
    /** OLE Ownerlink (never saved in CD_DDE or CD_OLE: used at run time) */
    OWNERLINK(NotesConstants.DDEFORMAT_OWNERLINK),
    /** OLE Objectlink (never saved in CD_DDE or CD_OLE: used at run time) */
    OBJECTLINK(NotesConstants.DDEFORMAT_OBJECTLINK),
    /** OLE Native (never saved in CD_DDE or CD_OLE: used at run time) */
    NATIVE(NotesConstants.DDEFORMAT_NATIVE),
    /** Program Icon for embedded object */
    ICON(NotesConstants.DDEFORMAT_ICON);
    private final short value;
    private ClipFormat(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("Version")
  Optional<Version> getVersion();

  @StructureGetter("Version")  
  short getVersionRaw();

  @StructureSetter("Version")
  CDOLEBegin setVersion(Version version);

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  CDOLEBegin setFlags(Collection<Flag> flags);
  
  @StructureGetter("ClipFormat")
  Optional<ClipFormat> getClipFormat();
  
  @StructureSetter("ClipFormat")
  CDOLEBegin setClipFormat(ClipFormat format);
  
  @StructureGetter("AttachNameLength")
  int getAttachmentNameLength();
  
  @StructureSetter("AttachNameLength")
  CDOLEBegin setAttachmentNameLength(int len);
  
  @StructureGetter("ClassNameLength")
  int getClassNameLength();
  
  @StructureSetter("ClassNameLength")
  CDOLEBegin setClassNameLength(int len);
  
  @StructureGetter("TemplateNameLength")
  int getTemplateNameLength();
  
  @StructureSetter("TemplateNameLength")
  CDOLEBegin setTemplateNameLength(int len);
  
  default String getAttachmentName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getAttachmentNameLength()
    );
  }
  
  default CDOLEBegin setAttachmentName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getAttachmentNameLength(),
      name,
      this::setAttachmentNameLength
    );
  }
  
  default String getClassName() {
    return StructureSupport.extractStringValue(
      this,
      getAttachmentNameLength(),
      getClassNameLength()
    );
  }
  
  default CDOLEBegin setClassName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getAttachmentNameLength(),
      getClassNameLength(),
      name,
      this::setClassNameLength
    );
  }
  
  default String getTemplateName() {
    return StructureSupport.extractStringValue(
      this,
      getAttachmentNameLength() + getClassNameLength(),
      getTemplateNameLength()
    );
  }
  
  default CDOLEBegin setTemplateName(String name) {
    return StructureSupport.writeStringValue(
      this,
      getAttachmentNameLength() + getClassNameLength(),
      getTemplateNameLength(),
      name,
      this::setTemplateNameLength
    );
  }
}
