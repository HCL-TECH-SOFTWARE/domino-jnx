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
package com.hcl.domino.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Attachment in a Document
 *
 * @author t.b.d.
 * @since 0.5.0
 */
public interface Attachment {

  /**
   * Enum for method of compression. Typically, modern applications should support
   * LZ1
   */
  public enum Compression {

    /** no compression */
    NONE(0),
    /** huffman encoding for compression */
    HUFF(1),
    /** LZ1 compression */
    LZ1(2),
    /** Huffman compression even if server supports LZ1 */
    RECOMPRESS_HUFF(3);

    private final int m_val;

    Compression(final int val) {
      this.m_val = val;
    }

    public int getValue() {
      return this.m_val;
    }

  }

  /**
   * Callback class to read the streamed attachment data
   */
  @FunctionalInterface
  public interface IDataCallback {
    public enum Action {
      Continue, Stop
    }

    /**
     * Implement this method to receive attachment data
     *
     * @param data data
     * @return action, either Continue or Stop
     */
    Action read(byte[] data);
  }

  /**
   * Deletes an attached file item from a note and also deallocates the disk space
   * used to store the attached file in the database.
   */
  void deleteFromDocument();

  /**
   * Writes the attachment data to a designated path.
   * <p>
   * Implementation note: using a local filesystem path is more efficient
   * than using a path from an abstracted provider.
   * </p>
   *
   * @param targetFilePath the file path of the file to write
   * @throws IOException in case of I/O errors
   */
  void extract(Path targetFilePath) throws IOException;

  /**
   * Gets the compression type for the attachment
   *
   * @return compression enum
   */
  Compression getCompression();

  /**
   * Gets the created datetime of the attachment.
   * TODO: Is the datetime it was attached, or the original created datetime of
   * the file?
   *
   * @return datetime the file was initially created
   */
  DominoDateTime getFileCreated();

  /**
   * Gets the modified datetime of the attachment
   *
   * @return the modified datetime of the attachment
   */
  DominoDateTime getFileModified();

  /**
   * Gets the filename for the attachment
   *
   * @return filename, not null
   */
  String getFileName();

  /**
   * Gets the filesize of the attachment
   *
   * @return filesize in bytes
   */
  long getFileSize();

  /**
   * Retrieves an input stream to access the data of the attachment.
   *
   * @return a new {@link InputStream} for the attachment data
   * @throws IOException in case of I/O errors
   */
  InputStream getInputStream() throws IOException;

  /**
   * Returns the parent document of this attachment
   *
   * @return document
   */
  Document getParent();

  /**
   * Method to access the binary attachment data
   *
   * @param callback callback is called with streamed data
   */
  void readData(final IDataCallback callback);

  /**
   * Method to access the binary attachment data beginning at an offset in the
   * file. The method is only supported when the attachment has no compression.
   * Otherwise we will throw an {@link UnsupportedOperationException}.
   *
   * @param callback callback is called with streamed data
   * @param offset   offset to start reading
   */
  void readData(final IDataCallback callback, int offset);
}
