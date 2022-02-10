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
package com.hcl.domino.dxl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.misc.CNativeEnum;

public interface DxlExporter {

  /** Specifies output charset */
  public enum DXLExportCharset implements CNativeEnum {
    /**
     * (default) "encoding =" attribute is set to utf8 and output charset is utf8
     */
    UTF8(0),
    /** "encoding =" attribute is set to utf16 and charset is utf16 */
    UTF16(1);

    private final int value;

    DXLExportCharset(final int value) {
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

  /** Specifies rule for exporting native MIME */
  public enum DXLMIMEOption implements CNativeEnum {
    /** (default) output native MIME within &lt;mime&gt; element in DXL */
    DXL(0),
    /** output MIME as uninterpretted (base64'ed) item data */
    ITEMDATA(1);

    private final int value;

    DXLMIMEOption(final int value) {
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

  /** Specifies rule for exporting rich text */
  public enum DXLRichTextOption implements CNativeEnum {
    /**
     * (default) output rich text as DXL with warning
     * comments if uninterpretable CD records
     */
    DXL(0),
    /** output rich text as uninterpretted (base64'ed) item data */
    ITEMDATA(1);

    private final int value;

    DXLRichTextOption(final int value) {
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

  /**
   * Specifies style of validation info emitted by exporter. Can override other
   * settings, eg - output doctype
   */
  public enum DXLValidationStyle implements CNativeEnum {
    NONE(0),
    DTD(1),
    XMLSCHEMA(2);

    private final int value;

    DXLValidationStyle(final int value) {
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

  /**
   * Export the ACL of the specified database in XML format.
   *
   * @param db  database to export
   * @param out result stream
   * @throws IOException in case of I/O errors
   */
  default void exportACL(final Database db, final OutputStream out) throws IOException {
    final OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    this.exportACL(db, w);
    w.flush();
  }

  /**
   * Export the ACL of the specified database in XML format.
   *
   * @param db  database to export
   * @param out result writer
   * @throws IOException in case of I/O errors
   */
  void exportACL(final Database db, final Writer out) throws IOException;

  /**
   * Export an entire database in XML format.
   *
   * @param db  database to export
   * @param out result stream
   * @throws IOException in case of I/O errors
   */
  default void exportDatabase(final Database db, final OutputStream out) throws IOException {
    final OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    this.exportDatabase(db, w);
    w.flush();
  }

  /**
   * Export an entire database in XML format.
   *
   * @param db  database to export
   * @param out result writer
   * @throws IOException in case of I/O errors
   */
  void exportDatabase(final Database db, final Writer out) throws IOException;

  /**
   * Exports an entire database into XML format and returns the string.
   * 
   * @param db the database to export
   * @return the exported DXL
   * @throws IOException in case of I/O errors
   * @since 1.0.34
   */
  default String exportDatabase(Database db) throws IOException {
    try(StringWriter w = new StringWriter()) {
      exportDatabase(db, w);
      w.flush();
      return w.toString();
    }
  }

  /**
   * Export a single document into XML format.
   *
   * @param doc document to export
   * @param out result stream
   * @throws IOException in case of I/O errors
   */
  default void exportDocument(final Document doc, final OutputStream out) throws IOException {
    final OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    this.exportDocument(doc, w);
    w.flush();
  }

  /**
   * Export a single document into XML format.
   *
   * @param doc document to export
   * @param out result writer
   * @throws IOException in case of I/O errors
   */
  void exportDocument(Document doc, Writer out) throws IOException;
  
  /**
   * Exports a single document into XML format and returns the string.
   * 
   * @param doc the document to export
   * @return the exported DXL
   * @throws IOException in case of I/O errors
   * @since 1.0.34
   */
  default String exportDocument(Document doc) throws IOException {
    try(StringWriter w = new StringWriter()) {
      exportDocument(doc, w);
      w.flush();
      return w.toString();
    }
  }

  boolean exportErrorWasLogged();

  /**
   * Export a set of note ids into XML format.
   *
   * @param db  database containing the export ids
   * @param ids ids to export
   * @param out result stream
   * @throws IOException in case of I/O errors
   */
  default void exportIDs(final Database db, final Collection<Integer> ids, final OutputStream out) throws IOException {
    final OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    this.exportIDs(db, ids, w);
    w.flush();
  }

  /**
   * Export a set of note IDs into XML format.
   * 
   * @param db  database containing the export ids
   * @param ids ids to export
   * @return the exported DXL
   * @throws IOException in case of I/O errors
   * @since 1.0.41
   */
  default String exportIDs(final Database db, final Collection<Integer> ids) throws IOException {
    try(StringWriter w = new StringWriter()) {
      exportIDs(db, ids, w);
      w.flush();
      return w.toString();
    }
  }

  /**
   * Export a set of note ids into XML format.
   *
   * @param db  database containing the export ids
   * @param ids ids to export
   * @param out result writer
   * @throws IOException in case of I/O errors
   */
  void exportIDs(Database db, Collection<Integer> ids, Writer out) throws IOException;

  String getAttachmentOmittedText();

  String getDefaultDoctypeSYSTEM();

  String getDoctypeSYSTEM();

  String getDXLBannerComments();

  String getDxlDefaultSchemaLocation();

  String getDxlExportResultLog();

  String getDxlExportResultLogComment();

  String getDxlSchemaLocation();

  DXLExportCharset getExportCharset();

  /**
   * @return an {@link Optional} describing the export character set as a
   *         {@link Charset}
   *         or an empty one if the charset cannot be mapped
   */
  Optional<Charset> getJDKExportCharset();

  DXLMIMEOption getMIMEOption();

  String getOLEObjectOmittedText();

  List<String> getOmitItemNames();

  String getPictureOmittedText();

  List<String> getRestrictToItemNames();

  DXLRichTextOption getRichTextOption();

  DXLValidationStyle getValidationStyle();

  boolean isConvertNotesbitmapsToGIF();

  /**
   * @return whether this exporter is configured to use raw note format
   * @since 1.0.26
   */
  boolean isForceNoteFormat();

  boolean isOmitMiscFileObjects();

  boolean isOmitOLEObjects();

  boolean isOmitPictures();

  boolean isOmitRichTextAttachments();

  boolean isOutputDoctype();

  boolean isOutputXmlDecl();

  boolean isUncompressAttachments();

  void setAttachmentOmittedText(String txt);

  void setConvertNotesbitmapsToGIF(boolean b);

  void setDoctypeSYSTEM(String docType);

  void setDXLBannerComments(String comments);

  void setDxlExportResultLogComment(String comment);

  void setDxlSchemaLocation(String loc);

  void setExportCharset(DXLExportCharset charset);

  /**
   * @param forceNoteFormat whether this exporter should use raw note format
   * @since 1.0.26
   */
  void setForceNoteFormat(boolean forceNoteFormat);

  void setMIMEOption(DXLMIMEOption option);

  void setOLEObjectOmittedText(String txt);

  void setOmitItemNames(List<String> itemNames);

  void setOmitMiscFileObjects(boolean b);

  void setOmitOLEObjects(boolean b);

  void setOmitPictures(boolean b);

  void setOmitRichTextAttachments(boolean b);

  void setOutputDoctype(boolean b);

  void setOutputXmlDecl(boolean b);

  void setPictureOmittedText(String txt);

  void setRestrictToItemNames(List<String> itemNames);

  void setRichTextOption(DXLRichTextOption option);

  void setUncompressAttachments(boolean b);

  void setValidationStyle(DXLValidationStyle style);

}
