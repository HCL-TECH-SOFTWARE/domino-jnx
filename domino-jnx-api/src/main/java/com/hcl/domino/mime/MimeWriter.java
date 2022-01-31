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
package com.hcl.domino.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

/**
 * Utility class to write MIME data to a document
 */
public interface MimeWriter {

  /**
   * Used to specify which part of the MIME content should be itemized into the
   * document
   */
  public enum WriteMimeDataType {
    /** write the MIME header (e.g. Subject:, To: etc.) */
    HEADERS,
    /** write the MIME body parts */
    BODY,
    /** don't delete attachment during itemization. */
    NO_DELETE_ATTACHMENTS
  }

  /**
   * This function converts all rich text items (type
   * {@link ItemDataType#TYPE_COMPOSITE}) in the
   * document to MIME items (type {@link ItemDataType#TYPE_MIME_PART}).<br>
   * <br>
   * It does not update the database; to update the database, save the
   * document.<br>
   *
   * @param doc             document to convert
   * @param convertSettings settings to control the conversion process or null to
   *                        use the system default
   */
  void convertToMime(Document doc, RichTextMimeConversionSettings convertSettings);

  /**
   * Creates a new {@link RichTextMimeConversionSettings} with system default
   * settings.
   *
   * @return conversion settings
   */
  RichTextMimeConversionSettings createRichTextMimeConversionSettings();

  /**
   * Writes the content of a {@link Message} to a document.
   * <p>
   * Note: to write the body of the message, the document must be in a database
   * where the current
   * user has at least Author rights.
   * </p>
   *
   * @param doc         target document
   * @param itemName    name of item used for the MIME data (e.g. "body")
   * @param mimeMessage MIME message to write
   * @param dataType    itemize flags (write header, body or both)
   * @throws IOException        in case of I/O errors
   * @throws MessagingException in case of errors accessing the {@link Message}
   */
  void writeMime(Document doc, String itemName, Message mimeMessage, Set<WriteMimeDataType> dataType)
      throws IOException, MessagingException;

  /**
   * Writes raw MIME content to a document.
   * <p>
   * Note: to write the body of the message, the document must be in a database
   * where the current user has at least Author rights.
   * </p>
   *
   * @param doc      target document
   * @param itemName name of item used for the MIME data (e.g. "body")
   * @param in   stream used to read the MIME content
   * @param dataType itemize flags (write header, body or both)
   * @throws IOException in case of I/O errors
   */
  void writeMime(Document doc, String itemName, InputStream in, Set<WriteMimeDataType> dataType) throws IOException;

}
