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
package com.hcl.domino.mime.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of {@link IMimeAttachment} to use a {@link URL}
 * as MIME attachment.
 *
 * @author Karsten Lehmann
 */
public class UrlMimeAttachment implements IMimeAttachment {
  private final URL m_url;
  private String m_fileName;
  private String m_contentType;

  /**
   * Creates a new instance
   *
   * @param url url to read the attachment content. We also try to read the
   *            content type and filename.
   */
  public UrlMimeAttachment(final URL url) {
    this(url, null, null);
  }

  /**
   * Creates a new instance
   *
   * @param url      url to read the attachment content. We also try to read the
   *                 content type.
   * @param fileName attachment filename
   */
  public UrlMimeAttachment(final URL url, final String fileName) {
    this(url, fileName, null);
  }

  /**
   * Creates a new instance
   *
   * @param url         url to read the attachment content
   * @param fileName    attachment filename
   * @param contentType mime type
   */
  public UrlMimeAttachment(final URL url, final String fileName, final String contentType) {
    this.m_url = url;
    this.m_fileName = fileName;
    this.m_contentType = contentType;
  }

  @Override
  public String getContentType() throws IOException {
    if (this.m_contentType == null || this.m_contentType.length() == 0) {
      final URLConnection conn = this.m_url.openConnection();
      this.m_contentType = conn.getContentType();

      if (this.m_contentType == null || this.m_contentType.length() == 0) {
        this.m_contentType = "application/octet-stream"; //$NON-NLS-1$
      }
    }
    return this.m_contentType;
  }

  @Override
  public String getFileName() throws IOException {
    if (this.m_fileName == null || this.m_fileName.length() == 0) {
      final URLConnection conn = this.m_url.openConnection();

      // try to read "Content-Disposition" header in case this is a http url
      // example: "attachment; filename=myfile.png"
      final String disposition = conn.getHeaderField("Content-Disposition"); //$NON-NLS-1$

      if (disposition != null && disposition.length() > 0) {
        final int iPos = disposition.indexOf('=');

        if (iPos != -1) {
          this.m_fileName = disposition.substring(iPos + 1);
        }
      }

      if (this.m_fileName == null || this.m_fileName.length() == 0) {
        // grab last part of filename
        String urlPath = this.m_url.getPath();
        while (urlPath.endsWith("/")) { //$NON-NLS-1$
          urlPath = urlPath.substring(0, urlPath.length() - 1);
        }

        final int iPos = urlPath.lastIndexOf('/');
        if (iPos != -1) {
          this.m_fileName = urlPath.substring(iPos + 1, urlPath.length());
        }
      }

      if (this.m_fileName == null || this.m_fileName.length() == 0) {
        // use a dummy fallback filename
        this.m_fileName = "attachment.bin"; //$NON-NLS-1$
      }
    }
    return this.m_fileName;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return this.m_url.openStream();
  }

}
