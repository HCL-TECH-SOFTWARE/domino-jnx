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
package com.hcl.domino.commons.mime;

import java.io.IOException;
import java.io.InputStream;

import com.hcl.domino.mime.attachments.IMimeAttachment;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;

/**
 * Implementation of {@link IMimeAttachment} that reads its
 * data from a Jakarta {@link BodyPart}.
 *
 * @author Karsten Lehmann
 */
public class MimeBodyPartAttachment implements IMimeAttachment {
  private final Part m_bodyPart;

  public MimeBodyPartAttachment(final Part bodyPart) {
    this.m_bodyPart = bodyPart;
  }

  @Override
  public String getContentType() throws IOException {
    try {
      return this.m_bodyPart.getContentType();
    } catch (final MessagingException e) {
      throw new IOException("Error accessing MIME body part", e);
    }
  }

  @Override
  public String getFileName() throws IOException {
    try {
      return this.m_bodyPart.getFileName();
    } catch (final MessagingException e) {
      throw new IOException("Error accessing MIME body part", e);
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    try {
      return this.m_bodyPart.getInputStream();
    } catch (final MessagingException e) {
      throw new IOException("Error accessing MIME body part", e);
    }
  }

}
