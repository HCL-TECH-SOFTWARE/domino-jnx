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
package com.hcl.domino.commons.dxl;

import com.hcl.domino.misc.CNativeEnum;

/**
 * Represents the {@code DXL_EXPORT_PROPERTY} enum from xml.h.
 *
 * @author Jesse Gallagher
 */
public enum DxlExportProperty implements CNativeEnum {
  /**
   * {@code MEMHANDLE}
   * <p>
   * Readonly - the result log from the last export.
   * </p>
   */
  DxlExportResultLog(1),
  /**
   * {@code MEMHANDLE}
   * <p>
   * Readonly - filename of dtd/schema keyed to current version of exporter
   * </p>
   */
  DefaultDoctypeSYSTEM(2),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * What to use for the DOCTYPE SYSTEM value (if emitted).
   * </p>
   * <dl>
   * <dt>NULL or ""</dt>
   * <dd>DOCTYPE should contain no SYSTEM info</dd>
   * <dt>"filename"</dt>
   * <dd>filename of DTD or schema used as DOCTYPE SYSTEM value</dd>
   * </dl>
   */
  DoctypeSYSTEM(3),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * One or more XML comments to output at top of the DXL
   * </p>
   * <dl>
   * <dt>NULL or ""</dt>
   * <dd>no DXL banner comments</dd>
   * <dt>"whatever"</dt>
   * <dd>zero or more null-terminated strings capped by extra empty string</dd>
   * </dl>
   */
  DXLBannerComments(4),
  /**
   * {@code DXL_EXPORT_CHARSET}
   * <p>
   * Specifies output charset.
   * </p>
   */
  DxlExportCharset(5),
  /**
   * {@code DXL_RICHTEXT_OPTION}
   * <p>
   * Specifies rule for exporting rich text.
   * </p>
   */
  DxlRichtextOption(6),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * LMBCS string to be added as comment to top of result log
   * </p>
   */
  DxlExportResultLogComment(7),
  /**
   * {@code DXL_EXPORT_VALIDATION_STYLE}
   * <p>
   * Specifies style of validation info emitted by exporter. Can override other
   * settings,
   * e.g. {@link #OutputDOCTYPE}.
   * </p>
   */
  DxlValidationStyle(8),
  /**
   * {@code MEMHANDLE}
   * <p>
   * Readonly - default xsi:SchemaLocation attribute value for current DXL version
   * </p>
   */
  DxlDefaultSchemaLocation(9),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * LMBCS value of xsi:SchemaLocation attribute put into DXL root element
   * </p>
   */
  DxlSchemaLocation(10),
  /**
   * {@code DXL_MIME_OPTION}
   * <p>
   * Specifies rule for exporting native MIME.
   * </p>
   */
  DxlMimeOption(11),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * Text to insert within rich text where an attachment ref was omitted; may
   * contain XML markup but
   * must be valid DXL rich text content.
   * </p>
   */
  AttachmentOmittedText(12),
  /**
   * {@code char*(i)/MEMHANDLE(o}
   * <p>
   * Text to insert within rich text where an object ref was omitted; may contain
   * XML markup but
   * must be valid DXL rich text content.
   * </p>
   */
  OLEObjectOmittedText(13),
  /**
   * {@code char*(i)/MEMHANDLE(o)}
   * <p>
   * Text to insert within rich text where a picture was omitted; may contain XML
   * markup but
   * must be valid DXL rich text content.
   * </p>
   */
  PictureOmittedText(14),
  /**
   * {@code HANDLE of list}
   * <p>
   * List of item names to omit from DXL. Use {@code Listxxx} functions to build
   * list (use fPrefixDataType=FALSE).
   * </p>
   * <p>
   * (i)API makes a copy of list thus does not adopt HANDLE
   * </p>
   * <p>
   * (o)API returns copy of list thus caller must free
   * </p>
   */
  OmitItemNames(15),
  /**
   * {@code HANDLE of list}
   * <p>
   * List of item names; only items with one of these names will be included in
   * the output DXL. Use {@code Listxxx}
   * functions to build list (use fPrefixDataType=FALSE).
   * </p>
   * <p>
   * (i)API makes a copy of list thus does not adopt HANDLE
   * </p>
   * <p>
   * (o)API returns copy of list thus caller must free
   * </p>
   */
  RestrictToItemNames(16),

  /**
   * {@code BOOL}
   * <p>
   * TRUE = Export data as notes containing items
   * </p>
   * <p>
   * FALSE = export using a high level of abstraction
   * </p>
   */
  ForceNoteFormat(30),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = Abort on first fatal error
   * </p>
   * <p>
   * FALSE = try to continue to export
   * </p>
   */
  ExitOnFirstFatalError(31),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = Root needs xmlns, version, and other common root attrs
   * </p>
   */
  OutputRootAttrs(32),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = Emit a leading xml declaration statement (&lt;?xml ...?&gt;)
   * </p>
   */
  OutputXmlDecl(33),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = Emit a DOCTYPE statement (can be overridden by
   * {@link #DxlValidationStyle})
   * </p>
   */
  OutputDOCTYPE(34),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = Convert Notesbitmaps embedded in richtext to GIFs
   * </p>
   * <p>
   * FALSE = blob the Notesbitmap CD records
   * </p>
   */
  ConvertNotesbitmapsToGIF(35),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = omit attachments within documents: both the attachmentref within
   * richtext and
   * corresponding items that contain file objects
   * </p>
   */
  OmitRichtextAttachments(36),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = omit OLE objects within documents: both the objectref within richtext
   * and
   * corresponding items that contain file objects
   * </p>
   */
  OmitOLEObjects(37),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = omit items within documents that are not normal attachments (named
   * $FILE) and
   * that contain file objects
   * </p>
   */
  OmitMiscFileObjects(38),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = omit pictures that occur directly within document richtext and contain
   * gif, jpeg,
   * notesbitmap, or cgm--does not include picture within attachmentref or
   * imagemap
   * </p>
   */
  OmitPictures(39),
  /**
   * {@code BOOL}
   * <p>
   * TRUE = uncompress attachments
   * </p>
   */
  UncompressAttachments(40);

  int value;

  DxlExportProperty(final int value) {
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