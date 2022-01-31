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
package com.hcl.domino.mime.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementation of {@link IMimeAttachment} to use a local file
 * as MIME attachment.
 *
 * @author Karsten Lehmann
 */
public class LocalFileMimeAttachment implements IMimeAttachment {
  private final Path m_filePathOnDisk;
  private final String m_fileName;
  private final String m_contentType;

  public LocalFileMimeAttachment(final Path filePathOnDisk) {
    this(filePathOnDisk, filePathOnDisk.getFileName().toString(), null);
  }

  public LocalFileMimeAttachment(final Path filePathOnDisk, final String fileName) {
    this(filePathOnDisk, fileName, null);
  }

  public LocalFileMimeAttachment(final Path filePathOnDisk, final String fileName, final String contentType) {
    this.m_filePathOnDisk = filePathOnDisk;
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
    return Files.newInputStream(this.m_filePathOnDisk);
  }

}
