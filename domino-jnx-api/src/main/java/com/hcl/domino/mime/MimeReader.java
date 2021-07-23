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
package com.hcl.domino.mime;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import com.hcl.domino.data.Document;

import jakarta.mail.internet.MimeMessage;

/**
 * Utility class to read MIME content from a document
 */
public interface MimeReader {

  /**
   * Used to specify which part of the document MIME content should be read
   */
  public enum ReadMimeDataType {
    MIMEHEADERS, RFC822HEADERS
  }

  /**
   * Returns a {@link Reader} to read the MIME content
   *
   * @param doc      document to read MIME
   * @param itemName name of item for the MIME content (e.g. "body")
   * @param dataType specifies with MIME data types should be read
   * @return reader
   */
  Reader getMIMEReader(Document doc, String itemName, Set<ReadMimeDataType> dataType);

  /**
   * Reads MIME content from a document and returns it as a {@link MimeMessage}.
   *
   * @param doc      document to read MIME
   * @param itemName name of item for the MIME content (e.g. "body")
   * @param dataType specifies with MIME data types should be read
   * @return MIME message
   */
  MimeMessage readMIME(Document doc, String itemName, Set<ReadMimeDataType> dataType);

  /**
   * Reads MIME content from a document into a {@link Writer} in a streaming
   * fashion.
   *
   * @param doc          document to read MIME
   * @param itemName     name of item for the MIME content (e.g. "body")
   * @param dataType     specifies with MIME data types should be read
   * @param targetWriter writer to receive the MIME content
   * @throws IOException in case of I/O errors writing into the {@link Writer}
   */
  void readMIME(Document doc, String itemName, Set<ReadMimeDataType> dataType, Writer targetWriter) throws IOException;

}
