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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link IMimeAttachment} to use a byte array
 * as MIME attachment.
 *
 * @author Karsten Lehmann
 */
public class ByteArrayMimeAttachment implements IMimeAttachment {
  private final byte[] m_data;
  private final String m_fileName;
  private final String m_contentType;

  public ByteArrayMimeAttachment(final byte[] data, final String fileName) {
    this(data, fileName, null);
  }

  public ByteArrayMimeAttachment(final byte[] data, final String fileName, final String contentType) {
    this.m_data = data;
    this.m_fileName = fileName;
    this.m_contentType = contentType;
  }

  @Override
  public String getContentType() throws IOException {
    return this.m_contentType;
  }

  @Override
  public String getFileName() throws IOException {
    return this.m_fileName;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(this.m_data);
  }

}
