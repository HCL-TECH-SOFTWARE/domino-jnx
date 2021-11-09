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
package com.hcl.domino.commons.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.util.StringTokenizerExt;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Attachment.IDataCallback;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.NativeItemCoder;

public class NotesMIMEPart {
  /**
   * Extension of {@link IDataCallback} that also receives the filesize
   */
  public interface IDataCallbackWithFileSize extends IDataCallback {

    void setFileSize(long fileSize);

  }

  public enum PartType {
    /** Mime part type is a prolog. */
    PROLOG,
    /** Mime part type is a body. */
    BODY,
    /** Mime part type is a epilog. */
    EPILOG,
    /** Mime part type is retrieve information. */
    RETRIEVE_INFO,
    /** Mime part type is a message. */
    MESSAGE
  }

  private static final Pattern FOLDING = Pattern.compile("\\r\\n[\\t| ]+"); //$NON-NLS-1$

  private static LinkedHashMap<String, String> parseHeaders(String headers) {
    final LinkedHashMap<String, String> headersMap = new LinkedHashMap<>();
    headers = NotesMIMEPart.FOLDING.matcher(headers).replaceAll(" "); //$NON-NLS-1$
    final StringTokenizerExt st = new StringTokenizerExt(headers, "\r\n"); //$NON-NLS-1$
    while (st.hasMoreTokens()) {
      final String currToken = st.nextToken();
      if (currToken.length() > 0) {
        final int iPos = currToken.indexOf(':');
        if (iPos != -1) {
          final String currHeaderName = currToken.substring(0, iPos);
          final String currHeaderValue = currToken.substring(iPos + 1).trim();
          headersMap.put(currHeaderName, currHeaderValue);
        }
      }
    }
    return headersMap;
  }

  private final Document m_parentNote;
  private final EnumSet<MimePartOptions> m_options;
  private final PartType m_partType;
  private final String m_boundaryStr;

  private final String m_headers;
  private final LinkedHashMap<String, String> m_headersParsed;

  private final byte[] m_data;

  public NotesMIMEPart(final Document parentNote, final EnumSet<MimePartOptions> options, final PartType partType,
      final String boundaryStr, final String headers, final byte[] data) {
    this.m_parentNote = parentNote;
    this.m_options = options;
    this.m_partType = partType;
    this.m_boundaryStr = boundaryStr;
    this.m_headers = headers;
    this.m_headersParsed = NotesMIMEPart.parseHeaders(headers);
    this.m_data = data;
  }

  /**
   * Returns the boundary string
   * 
   * @return boundary string
   */
  public String getBoundaryString() {
    return this.m_boundaryStr;
  }

  /**
   * Method to access the MIME part's data
   * 
   * @param callback callback to receive data
   */
  public void getContentAsBytes(final IDataCallbackWithFileSize callback) {
    if (this.m_options.contains(MimePartOptions.BODY_IN_DBOBJECT)) {
      final String fileName = new String(this.m_data, NativeItemCoder.get().getLmbcsCharset());
      final Attachment att = this.m_parentNote.getAttachment(fileName).orElseThrow(
          () -> new DominoException(0, MessageFormat.format("No attachment item found for filename {0}", fileName)));
      final long size = att.getFileSize();

      callback.setFileSize(size);
      att.readData(callback);
    } else {
      callback.setFileSize(this.m_data.length);
      callback.read(this.m_data);
    }
  }

  public String getContentAsText() {
    if (this.m_options.contains(MimePartOptions.BODY_IN_DBOBJECT)) {
      final String fileName = new String(this.m_data, RichTextUtil.LMBCS);
      final Attachment att = this.m_parentNote.getAttachment(fileName).orElseThrow(
          () -> new DominoException(0, MessageFormat.format("No attachment item found for filename {0}", fileName)));
      final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      att.readData(data -> {
        try {
          bOut.write(data);
          return IDataCallback.Action.Continue;
        } catch (final IOException e) {
          throw new DominoException(0, "Error writing attachment data", e);
        }
      });
      final byte[] bOutArr = bOut.toByteArray();
      return new String(bOutArr, RichTextUtil.LMBCS);
    } else {
      return new String(this.m_data, RichTextUtil.LMBCS);
    }
  }

  public String getEncoding() {
    final String encoding = this.getHeaders().get("Content-Transfer-Encoding"); //$NON-NLS-1$
    if (encoding == null) {
      return "binary"; //$NON-NLS-1$
    } else {
      return encoding;
    }

    // https://www.w3.org/Protocols/rfc1341/5_Content-Transfer-Encoding.html
    // Content-Transfer-Encoding := "BASE64" / "QUOTED-PRINTABLE" /
    // "8BIT" / "7BIT" /
    // "BINARY" / x-token
    //
    // These values are not case sensitive. That is, Base64 and BASE64 and bAsE64
    // are all equivalent.
    // An encoding type of 7BIT requires that the body is already in a seven-bit
    // mail- ready
    // representation. This is the default value -- that is,
    // "Content-Transfer-Encoding:
    // 7BIT" is assumed if the Content-Transfer-Encoding header field is not
    // present.
    // The values "8bit", "7bit", and "binary" all imply that NO encoding has been
    // performed.
    // However, they are potentially useful as indications of the kind of data
    // contained in
    // the object, and therefore of the kind of encoding that might need to be
    // performed
    // for transmission in a given transport system. "7bit" means that the data is
    // all
    // represented as short lines of US-ASCII data. "8bit" means that the lines are
    // short,
    // but there may be non-ASCII characters (octets with the high-order bit set).
    // "Binary"
    // means that not only may non-ASCII characters be present, but also that the
    // lines are
    // not necessarily short enough for SMTP transport.
    //
    // The difference between "8bit" (or any other conceivable bit-width token) and
    // the "binary"
    // token is that "binary" does not require adherence to any limits on line
    // length or to
    // the SMTP CRLF semantics, while the bit-width tokens do require such
    // adherence. If the body
    // contains data in any bit-width other than 7-bit, the appropriate bit-width
    // Content-Transfer-Encoding token must be used (e.g., "8bit" for unencoded 8
    // bit wide data).
    // If the body contains binary data, the "binary" Content-Transfer-Encoding
    // token must be used.

    // https://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/net/QuotedPrintableCodec.html

  }

  public LinkedHashMap<String, String> getHeaders() {
    return this.m_headersParsed;
  }

  /**
   * Returns the mime part options
   * 
   * @return options
   */
  public EnumSet<MimePartOptions> getOptions() {
    return this.m_options;
  }

  public Document getParentNote() {
    return this.m_parentNote;
  }

  /**
   * Returns the mime part type
   * 
   * @return type
   */
  public PartType getType() {
    return this.m_partType;
  }

  @Override
  public String toString() {
    return MessageFormat.format("NotesMIMEPart [options={0}, type={1}, boundary={2}, headers={3}, datalen={4}]", //$NON-NLS-1$
        this.m_options, this.m_partType, this.m_boundaryStr, this.m_headers, this.m_data.length);
  }
}
