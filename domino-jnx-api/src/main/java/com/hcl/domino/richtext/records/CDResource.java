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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.DominoException;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.NOTELINK;
import com.hcl.domino.richtext.structures.OpaqueTimeDate;
import com.hcl.domino.richtext.structures.WSIG;
import com.hcl.domino.util.JNXStringUtil;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
@StructureDefinition(
  name = "CDRESOURCE",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDResource.Flag.class, bitfield = true),
    @StructureMember(name = "Type", type = CDResource.Type.class),
    @StructureMember(name = "ResourceClass", type = CDResource.ResourceClass.class),
    @StructureMember(name = "Length1", type = short.class, unsigned = true),
    @StructureMember(name = "ServerHintLength", type = short.class, unsigned = true),
    @StructureMember(name = "FileHintLength", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 8)
  }
)
public interface CDResource extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /** the type's data is a formula valid for _TYPE_URL and _TYPE_NAMEDELEMENT */
    FORMULA(RichTextConstants.CDRESOURCE_FLAGS_FORMULA),
    /**
     * the notelink variable length data contains the notelink itself not an index
     * into a $Links items
     */
    NOTELINKINLINE(RichTextConstants.CDRESOURCE_FLAGS_NOTELINKINLINE),
    /**
     * If specified, the link is to an absolute database or thing. Used to make a
     * hard link to a specific DB.
     */
    ABSOLUTE(RichTextConstants.CDRESOURCE_FLAGS_ABSOLUTE),
    /**
     * If specified, the server and file hint are filled in and should be attempted
     * before trying other copies.
     */
    USEHINTFIRST(RichTextConstants.CDRESOURCE_FLAGS_USEHINTFIRST),
    /**
     * the type's data is a canned image file (data/domino/icons/[*].gif) valid for
     * {@code _TYPE_URL && _CLASS_IMAGE} only
     */
    CANNEDIMAGE(RichTextConstants.CDRESOURCE_FLAGS_CANNEDIMAGE),
    /*	NOTE: _PRIVATE_DATABASE and _PRIVATE_DESKTOP are mutually exclusive. */
    /** the object is private in its database */
    PRIVATE_DATABASE(RichTextConstants.CDRESOURCE_FLAGS_PRIVATE_DATABASE),
    /** the object is private in the desktop database */
    PRIVATE_DESKTOP(RichTextConstants.CDRESOURCE_FLAGS_PRIVATE_DESKTOP),
    /**
     * the replica in the CD resource needs to be obtained via RLGetReplicaID to
     * handle special replica IDs like 'current' mail file.
     */
    REPLICA_WILDCARD(RichTextConstants.CDRESOURCE_FLAGS_REPLICA_WILDCARD),
    /** used with class view and folder to mean "Simple View" */
    SIMPLE(RichTextConstants.CDRESOURCE_FLAGS_SIMPLE),
    /** open this up in design mode */
    DESIGN_MODE(RichTextConstants.CDRESOURCE_FLAGS_DESIGN_MODE),
    /** open this up in preivew mode, if supported. Not saved to disk */
    PREVIEW(RichTextConstants.CDRESOURCE_FLAGS_PREVIEW),
    /** we will be doing a search after link opened. Not saved to disk */
    SEARCH(RichTextConstants.CDRESOURCE_FLAGS_SEARCH),

    /**
     * An UNID is added to the end of the hResource that means something to that
     * type - currently used in named element type
     */
    UNIDADDED(RichTextConstants.CDRESOURCE_FLAGS_UNIDADDED),
    /** document should be in edit mode */
    EDIT_MODE(RichTextConstants.CDRESOURCE_FLAGS_EDIT_MODE),

    /** reserved meaning for each resource link class */
    RESERVED1(RichTextConstants.CDRESOURCE_FLAGS_RESERVED1),
    /** reserved meaning for each resource link class */
    RESERVED2(RichTextConstants.CDRESOURCE_FLAGS_RESERVED2),
    /** reserved meaning for each resource link class */
    RESERVED3(RichTextConstants.CDRESOURCE_FLAGS_RESERVED3),
    /** reserved meaning for each resource link class */
    RESERVED4(RichTextConstants.CDRESOURCE_FLAGS_RESERVED4);

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

  enum ResourceClass implements INumberEnum<Short> {
    UNKNOWN(RichTextConstants.CDRESOURCE_CLASS_UNKNOWN),
    DOCUMENT(RichTextConstants.CDRESOURCE_CLASS_DOCUMENT),
    VIEW(RichTextConstants.CDRESOURCE_CLASS_VIEW),
    FORM(RichTextConstants.CDRESOURCE_CLASS_FORM),
    NAVIGATOR(RichTextConstants.CDRESOURCE_CLASS_NAVIGATOR),
    DATABASE(RichTextConstants.CDRESOURCE_CLASS_DATABASE),
    FRAMESET(RichTextConstants.CDRESOURCE_CLASS_FRAMESET),
    PAGE(RichTextConstants.CDRESOURCE_CLASS_PAGE),
    IMAGE(RichTextConstants.CDRESOURCE_CLASS_IMAGE),
    ICON(RichTextConstants.CDRESOURCE_CLASS_ICON),
    HELPABOUT(RichTextConstants.CDRESOURCE_CLASS_HELPABOUT),
    HELPUSING(RichTextConstants.CDRESOURCE_CLASS_HELPUSING),
    SERVER(RichTextConstants.CDRESOURCE_CLASS_SERVER),
    APPLET(RichTextConstants.CDRESOURCE_CLASS_APPLET),
    /** A compiled formula someplace */
    FORMULA(RichTextConstants.CDRESOURCE_CLASS_FORMULA),
    AGENT(RichTextConstants.CDRESOURCE_CLASS_AGENT),
    /** a file on disk (file:) */
    FILE(RichTextConstants.CDRESOURCE_CLASS_FILE),
    /** A file attached to a note */
    FILEATTACHMENT(RichTextConstants.CDRESOURCE_CLASS_FILEATTACHMENT),
    OLEEMBEDDING(RichTextConstants.CDRESOURCE_CLASS_OLEEMBEDDING),
    /** A shared image resource */
    SHAREDIMAGE(RichTextConstants.CDRESOURCE_CLASS_SHAREDIMAGE),
    FOLDER(RichTextConstants.CDRESOURCE_CLASS_FOLDER),
    /**
     * An old (4.6) or new style portfolio. Which gets incorporated into the
     * bookmark bar as a
     * tab, rather than getting opened as a database.
     */
    PORTFOLIO(RichTextConstants.CDRESOURCE_CLASS_PORTFOLIO),
    OUTLINE(RichTextConstants.CDRESOURCE_CLASS_OUTLINE),
    /** Obsolete - Use class _VIEW or _FOLDER and flag _SIMPLE */
    SIMPLEVIEW(RichTextConstants.CDRESOURCE_CLASS_SIMPLEVIEW),
    /** design link only */
    SUBFORM(RichTextConstants.CDRESOURCE_CLASS_SUBFORM),
    /** design link only */
    SHARED_FLD(RichTextConstants.CDRESOURCE_CLASS_SHARED_FLD),
    /** design link only */
    SCRIPTLIB(RichTextConstants.CDRESOURCE_CLASS_SCRIPTLIB),
    /** design link only */
    DBSCRIPT(RichTextConstants.CDRESOURCE_CLASS_DBSCRIPT),
    /** design link only */
    SHARED_ACTIONS(RichTextConstants.CDRESOURCE_CLASS_SHARED_ACTIONS),
    /** design link only */
    WEBSERVICE(RichTextConstants.CDRESOURCE_CLASS_WEBSERVICE),
    /** design link only */
    DATA_CONNECTION(RichTextConstants.CDRESOURCE_CLASS_DATA_CONNECTION),
    SHARED_APPLET(RichTextConstants.CDRESOURCE_CLASS_SHARED_APPLET),
    EMBEDDED_VIEW(RichTextConstants.CDRESOURCE_CLASS_EMBEDDED_VIEW),
    STYLE_SHEET(RichTextConstants.CDRESOURCE_CLASS_STYLE_SHEET),
    /** a file with the html flag, too */
    HTMLFILE(RichTextConstants.CDRESOURCE_CLASS_HTMLFILE),
    /** a file that's a JSP */
    JSP(RichTextConstants.CDRESOURCE_CLASS_JSP),
    SHAREDCOL(RichTextConstants.CDRESOURCE_CLASS_SHAREDCOL),
    DB2ACCESSVIEW(RichTextConstants.CDRESOURCE_CLASS_DB2ACCESSVIEW),
    /** LI 3925.04 */
    COMPAPP(RichTextConstants.CDRESOURCE_CLASS_COMPAPP),
    /** LI 3925.05 */
    COMPDEF(RichTextConstants.CDRESOURCE_CLASS_COMPDEF),
    /** LI 3261.05 */
    MAILSETTINGS(RichTextConstants.CDRESOURCE_CLASS_MAILSETTINGS),
    /** LI 3261.05 */
    CSSETTINGS(RichTextConstants.CDRESOURCE_CLASS_CSSETTINGS),
    /** LI 3261.05 */
    FORM_PREMIUM(RichTextConstants.CDRESOURCE_CLASS_FORM_PREMIUM),
    /** design link only */
    XSPPAGES(RichTextConstants.CDRESOURCE_CLASS_XSPPAGES),
    /** design link only */
    XSPCCS(RichTextConstants.CDRESOURCE_CLASS_XSPCCS),
    /** design link only */
    STYLEKITS(RichTextConstants.CDRESOURCE_CLASS_STYLEKITS),
    /** design link only */
    WSCONSUMERS(RichTextConstants.CDRESOURCE_CLASS_WSCONSUMERS),
    /** design link only */
    COMPONENT(RichTextConstants.CDRESOURCE_CLASS_COMPONENT),
    /** design link only */
    JAVAFILES(RichTextConstants.CDRESOURCE_CLASS_JAVAFILES),
    /** design link only */
    JAVAJARS(RichTextConstants.CDRESOURCE_CLASS_JAVAJARS),
    /** design link only */
    CUSTOMELTS(RichTextConstants.CDRESOURCE_CLASS_CUSTOMELTS);

    private final short value;

    ResourceClass(final short value) {
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

  enum Type implements INumberEnum<Short> {
    EMPTY(RichTextConstants.CDRESOURCE_TYPE_EMPTY),
    URL(RichTextConstants.CDRESOURCE_TYPE_URL),
    NOTELINK(RichTextConstants.CDRESOURCE_TYPE_NOTELINK),
    NAMEDELEMENT(RichTextConstants.CDRESOURCE_TYPE_NAMEDELEMENT),
    /** Currently not written to disk only used in RESOURCELINK */
    NOTEIDLINK(RichTextConstants.CDRESOURCE_TYPE_NOTEIDLINK),
    /**
     * This would be used in conjunction with the formula flag. The formula is
     * an @Command that would
     * perform some action, typically it would also switch to a Notes UI element.
     * This will be used to
     * reference the replicator page and other UI elements.
     */
    ACTION(RichTextConstants.CDRESOURCE_TYPE_ACTION),
    /** Currently not written to disk only used in RESOURCELINK */
    NAMEDITEMELEMENT(RichTextConstants.CDRESOURCE_TYPE_NAMEDITEMELEMENT);

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

  @Override
  @StructureGetter("Header")
  WSIG getHeader();

  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  CDResource setFlags(Collection<Flag> flags);
  
  @StructureGetter("Type")
  Type getResourceType();

  @StructureSetter("Type")
  CDResource setResourceType(Type type);

  @StructureGetter("ResourceClass")
  Optional<ResourceClass> getResourceClass();

  @StructureSetter("ResourceClass")
  CDResource setResourceClass(ResourceClass resClass);

  @StructureGetter("Length1")
  int getLength1();

  @StructureSetter("Length1")
  CDResource setLength1(int length1);

  @StructureGetter("ServerHintLength")
  int getServerHintLength();

  @StructureSetter("ServerHintLength")
  CDResource setServerHintLength(int length);

  @StructureGetter("FileHintLength")
  int getFileHintLength();

  @StructureSetter("FileHintLength")
  CDResource setFileHintLength(int length);

  default String getServerHint() {
    return StructureSupport.extractStringValue(
      this,
      0,
      this.getServerHintLength()
    );
  }

  default CDResource setServerHint(final String hint) {
    return StructureSupport.writeStringValue(
      this,
      0,
      this.getServerHintLength(),
      hint,
      this::setServerHintLength
    );
  }

  default String getFileHint() {
    return StructureSupport.extractStringValue(
      this,
      this.getServerHintLength(),
      this.getFileHintLength()
    );
  }

  default CDResource setFileHint(final String hint) {
    return StructureSupport.writeStringValue(
      this,
      this.getServerHintLength(),
      this.getFileHintLength(),
      hint,
      this::setFileHintLength
    );
  }

  /**
   * Retrieves the URL of the target resource.
   * <p>
   * Note: this only applies when {@link #getResourceType()} is {@link Type#URL}.
   * </p>
   *
   * @return an {@link Optional} describing the URL to the referenced value,
   *         or an empty one if this is not a URL element
   */
  default Optional<String> getUrl() {
    if (this.getResourceType() != Type.URL) {
      return Optional.empty();
    }
    return Optional.of(
      StructureSupport.extractStringValue(
        this,
        this.getServerHintLength() + this.getFileHintLength(),
        this.getLength1()
      )
    );
  }

  /**
   * Retrieves the link anchor name for the target resource.
   * <p>
   * Note: this only applies when {@link #getResourceType()} is
   * {@link Type#NOTELINK}.
   * </p>
   *
   * @return an {@link Optional} describing the anchor name, or
   *         an empty one if this is not a note-link element
   */
  default Optional<String> getLinkAnchorName() {
    if (this.getResourceType() != Type.NOTELINK) {
      return Optional.empty();
    }
    int preLen = this.getServerHintLength() + this.getFileHintLength();
    Set<Flag> flags = getFlags();
    if (flags.contains(Flag.NOTELINKINLINE)) {
      //skip NOTELINK structure
      preLen = MemoryStructureWrapperService.get().sizeOf(NOTELINK.class);
    }
    else {
      //skip WORD with LinkID
      preLen += 2;
    }
    
    return Optional.of(
      StructureSupport.extractStringValue(
        this,
        preLen,
        this.getLength1()
      )
    );
  }

  /**
   * Retrieves the name of the element referenced by this resource.
   * <p>
   * Note: this only applies when {@link #getResourceType()} is
   * {@link Type#NAMEDELEMENT}.
   * </p>
   *
   * @return an {@link Optional} describing the name of the referenced element,
   *         or an empty one if this is not a named element
   */
  default Optional<String> getNamedElement() {
    if (this.getResourceType() != Type.NAMEDELEMENT) {
      return Optional.empty();
    }
    if (this.getFlags().contains(Flag.FORMULA)) {
      return Optional.empty();
    }
    return Optional.of(
      StructureSupport.extractStringValue(
        this,
        this.getServerHintLength() + this.getFileHintLength() + 8, // 8 for replica ID
        this.getLength1()
      )
    );
  }

  /**
   * Retrieves the url of the element referenced by this resource.
   * <p>
   * Note: this only applies when {@link #getResourceType()} is
   * {@link Type#URL}.
   * </p>
   *
   * @return an {@link Optional} describing the url of the referenced element,
   *         or an empty one if this is not a url
   */
  default Optional<String> getResourceUrl() {
    if (this.getResourceType() != Type.URL) {
      return Optional.empty();
    }
    if (this.getFlags().contains(Flag.FORMULA)) {
      return Optional.empty();
    }
    return Optional.of(
      StructureSupport.extractStringValue(
        this,
        this.getServerHintLength() + this.getFileHintLength(),
        this.getLength1()
      )
    );
  }
  
  /**
   * Retrieves the formulas for this resource, when {@link #getFlags()} contains
   * {@link Flag#FORMULA}.
   * 
   * <p>The meaning of this formula depends on the value of
   * {@link #getResourceType()}:</p>
   * 
   * <ul>
   *   <li>For resources of type {@link Type#NAMEDELEMENT NAMEDELEMENT} or
   *       {@link Type#URL URL}, this value represents the formula for a string
   *       of the element name or URL. Resources of frames in framesets have two
   *       additional formulas for the resource type (e.g. "Page") and the database path.</li>
   *   <li>For resources of type {@link Type#ACTION ACTION}, this represents the
   *       executable script of the action.</li>
   * </ul>
   *
   * @return an {@link Optional} describing the formula for the name of the referenced
   *         element, or an optional one if this is not a formula-based element
   */
  default Optional<List<String>> getNamedElementFormulas() {
    if (!this.getFlags().contains(Flag.FORMULA)) {
      return Optional.empty();
    }
    int preLen = getServerHintLength() + getFileHintLength();
    if(getResourceType() == Type.NAMEDELEMENT) {
      // skip the replica ID
      preLen += 8;
    }
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    byte[] formulaData = new byte[getLength1()];
    varData.get(formulaData);
    
    //there are three formulas: for the name of the named element, its type
    //and its database path
    List<String> formulas = FormulaCompiler.get().decompileMulti(formulaData);
    
    return Optional.of(formulas);
  }

  /**
   * Sets the formula for a named element. 
   * 
   * @param formulas we expect three formulas: for the name of the named element (e.g. "mypage1"), for the resource type, e.g. "Page" and for the database path
   * @return this record
   */
  default CDResource setNamedElementFormulas(Collection<String> formulas) {
    Set<Flag> flags = getFlags();
    if (!flags.contains(Flag.FORMULA)) {
      //clear formula flag
      flags.add(Flag.FORMULA);
      setFlags(flags);
    }
    setResourceType(Type.NAMEDELEMENT);
    
    MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    int replicaIDLen = wrapper.sizeOf(OpaqueTimeDate.class);
    
    int preLen = this.getServerHintLength() + this.getFileHintLength();
    
    FormulaCompiler compiler = FormulaCompiler.get();
    
    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    for (String currFormula : formulas) {
      byte[] currFormulaArr = compiler.compile(currFormula);
      try {
        bOut.write(currFormulaArr);
      } catch (IOException e) {
        throw new DominoException("Error writing compiled formula to stream", e);
      }
    }

    resizeVariableData(preLen + replicaIDLen + bOut.size());
    
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    varData.put(new byte[8]);
    varData.put(bOut.toByteArray());
    
    setLength1(bOut.size());
    return this;
  }

  /**
   * Returns the database replica id of named elements
   * 
   * @return replica id
   */
  default Optional<String> getNamedElementReplicaId() {
    if (getResourceType() != Type.NAMEDELEMENT) {
      return Optional.empty();
    }
    
    int preLen = this.getServerHintLength() + this.getFileHintLength();
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    
    OpaqueTimeDate replicaID = MemoryStructureWrapperService.get().newStructure(OpaqueTimeDate.class, 0);
    
    byte[] replicaIDData = new byte[replicaID.getData().limit()];
    varData.get(replicaIDData);
    
    replicaID.getData().put(replicaIDData);
    return Optional.of(replicaID.toReplicaId());
  }
  
  /**
   * Sets the name of a named element and its database replica id
   * 
   * @param replicaIDStr or null/empty for local DB
   * @param name name of named element
   * @return this record
   */
  default CDResource setNamedElement(String replicaIDStr, String name) {
    Set<Flag> flags = getFlags();
    if (flags.contains(Flag.FORMULA)) {
      //clear formula flag
      flags.remove(Flag.FORMULA);
      setFlags(flags);
    }
    setResourceType(Type.NAMEDELEMENT);
    
    MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    OpaqueTimeDate replicaID = wrapper.newStructure(OpaqueTimeDate.class, 0);
    replicaID.setFromReplicaId(replicaIDStr);
    ByteBuffer replicaIDData = replicaID.getData();
    int replicaIDLen = replicaIDData.limit();
    
    int preLen = this.getServerHintLength() + this.getFileHintLength();
    
    byte[] nameLMBCS = name.getBytes(NativeItemCoder.get().getLmbcsCharset());

    resizeVariableData(preLen + replicaIDLen + nameLMBCS.length);
    
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    varData.put(replicaIDData);
    varData.put(nameLMBCS);
    
    setLength1(nameLMBCS.length);
    return this;
  }
  
  default CDResource setResourceUrl(String url) {
    setResourceType(Type.URL);
    
    Set<Flag> flags = getFlags();
    if (flags.contains(Flag.FORMULA)) {
      //clear formula flag
      flags.remove(Flag.FORMULA);
      setFlags(flags);
    }

    int preLen = this.getServerHintLength() + this.getFileHintLength();
    
    byte[] urlLMBCS = url.getBytes(NativeItemCoder.get().getLmbcsCharset());

    resizeVariableData(preLen + urlLMBCS.length);
    
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    varData.put(urlLMBCS);
    
    setLength1(urlLMBCS.length);
    return this;
  }
  
  default Optional<NOTELINK> getLink() {
    if (getResourceType()!=Type.NOTELINK) {
      return Optional.empty();
    }
    Set<Flag> flags = getFlags();
    if (!flags.contains(Flag.NOTELINKINLINE)) {
      return Optional.empty();
    }
    
    ByteBuffer varData = getVariableData();
    varData.position(this.getServerHintLength() + this.getFileHintLength());
    ByteBuffer linkData = varData.slice();
    MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    linkData.limit(wrapper.sizeOf(NOTELINK.class));
    
    return Optional.of(wrapper.wrapStructure(NOTELINK.class, linkData));
  }
  
  /**
   * Sets the resource type to {@link Type#NOTELINK}, enables the
   * flag {@link Flag#NOTELINKINLINE} and stores a {@link NOTELINK}
   * structure and optional link anchor name.
   * 
   * @param link link to set
   * @param linkAnchorName optional name of link anchor or null/empty string
   * @return resource
   */
  default CDResource setLink(NOTELINK link, String linkAnchorName) {
    setResourceType(Type.NOTELINK);
    
    Set<Flag> flags = getFlags();
    flags.add(Flag.NOTELINKINLINE);
    flags.remove(Flag.FORMULA);
    setFlags(flags);
    
    ByteBuffer linkData = link.getData();
    byte[] linkAnchorNameLMBCS = JNXStringUtil.isEmpty(linkAnchorName) ? new byte[0] : linkAnchorName.getBytes(NativeItemCoder.get().getLmbcsCharset());

    int preLen = this.getServerHintLength() + this.getFileHintLength();
    resizeVariableData(preLen + linkData.limit() + linkAnchorNameLMBCS.length);
    
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    varData.put(linkData);
    varData.put(linkAnchorNameLMBCS);
    
    setLength1(linkAnchorNameLMBCS.length);
    
    return this;
  }
  
  /**
   * Sets the resource type to {@link Type#NOTELINK}, disables the
   * flag {@link Flag#NOTELINKINLINE} and stores a LinkId pointing
   * to a $Links item in the document
   * 
   * @param linkId link id
   * @param linkAnchorName optional name of link anchor or null/empty string
   * @return resource
   */
  default CDResource setLinkId(int linkId, String linkAnchorName) {
    setResourceType(Type.NOTELINK);
    
    Set<Flag> flags = getFlags();
    flags.remove(Flag.NOTELINKINLINE);
    flags.remove(Flag.FORMULA);
    setFlags(flags);
    
    int preLen = this.getServerHintLength() + this.getFileHintLength();
    
    byte[] linkAnchorNameLMBCS = JNXStringUtil.isEmpty(linkAnchorName) ? new byte[0] : linkAnchorName.getBytes(NativeItemCoder.get().getLmbcsCharset());
    resize(preLen + 2 + linkAnchorNameLMBCS.length);
    
    ByteBuffer varData = getVariableData();
    varData.position(preLen);
    short linkIdShort = (short) (linkId & 0xffff);
    varData.putShort(linkIdShort);
    //append link anchor name
    varData.put(linkAnchorNameLMBCS);
    
    setLength1(linkAnchorNameLMBCS.length);
    return this;
  }
}
