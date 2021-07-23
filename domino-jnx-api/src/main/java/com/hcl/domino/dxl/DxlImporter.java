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
package com.hcl.domino.dxl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.misc.CNativeEnum;
import com.hcl.domino.misc.SplitterOutputStream;

public interface DxlImporter {

  public enum DXLImportOption implements CNativeEnum {
    /** ignore imported data */
    IGNORE(1),
    /** create new data from imported data */
    CREATE(2),
    /**
     * if imported data matches existing data, ignore the imported data, otherwise
     * create it
     */
    IGNORE_ELSE_CREATE(3),
    /** do not used - reserved for future variation of create option */
    CREATE_RESERVED2(4),
    /**
     * if imported data matches existing data, then replace existing data with
     * imported data, else ignore imported data.
     */
    REPLACE_ELSE_IGNORE(5),
    /**
     * if imported data matches existing data, then replace existing data with
     * imported data, else create
     * new data from imported data
     */
    REPLACE_ELSE_CREATE(6),
    /** do not used - reserved for future variation of replace option */
    REPLACE_RESERVED1(7),
    /** do not used - reserved for future variation of replace option */
    REPLACE_RESERVED2(8),
    /**
     * if imported data matches existing data, then update existing data with
     * imported data, else ignore imported data.
     */
    UPDATE_ELSE_IGNORE(9),
    /**
     * if imported data matches existing data, then update existing data with
     * imported data, else create
     * new data from imported data
     */
    UPDATE_ELSE_CREATE(10),
    /** do not used - reserved for future variation of update option */
    UPDATE_RESERVED1(11),
    /** do not used - reserved for future variation of update option */
    UPDATE_RESERVED2(12);

    private final int m_option;

    DXLImportOption(final int option) {
      this.m_option = option;
    }

    @Override
    public long getLongValue() {
      return this.m_option;
    }

    @Override
    public Integer getValue() {
      return this.m_option;
    }
  }

  public enum DXLLogOption implements CNativeEnum {
    /** ignore the action. don't log anything and just continue */
    IGNORE(1),
    /** log the problem as a warning */
    WARNING(2),
    /** log the problem as an error */
    ERROR(3),
    /** log the problem as a fatal error */
    FATALERROR(4);

    private final int m_option;

    DXLLogOption(final int option) {
      this.m_option = option;
    }

    @Override
    public long getLongValue() {
      return this.m_option;
    }

    @Override
    public Integer getValue() {
      return this.m_option;
    }
  }

  public enum XMLValidationOption implements CNativeEnum {
    NEVER(0),
    ALWAYS(1),
    AUTO(2);

    private final int m_option;

    XMLValidationOption(final int option) {
      this.m_option = option;
    }

    @Override
    public long getLongValue() {
      return this.m_option;
    }

    @Override
    public Integer getValue() {
      return this.m_option;
    }
  }

  DXLImportOption getACLImportOption();

  DXLImportOption getDesignImportOption();

  DXLImportOption getDocumentsImportOption();

  /**
   * @return an {@link Optional} describing the IDs imported in the last
   *         operation,
   *         or an empty one if no imports have been performed
   */
  Optional<IDTable> getImportedNoteIds();

  XMLValidationOption getInputValidationOption();

  String getResultLog();

  String getResultLogComment();

  DXLLogOption getUnknownTokenLogOption();

  void importDxl(final InputStream in, final Database db) throws IOException;

  default void importDxl(final Reader in, final Database db) throws IOException {
    // TODO skip reading the whole thing in, likely by adapting IBM Commons
    // ReaderInputStream
    final StringBuilder buffer = new StringBuilder();
    final char[] arr = new char[8 * 1024];
    int numCharsRead;
    while ((numCharsRead = in.read(arr, 0, arr.length)) != -1) {
      buffer.append(arr, 0, numCharsRead);
    }

    try (InputStream is = new ByteArrayInputStream(buffer.toString().getBytes())) {
      this.importDxl(is, db);
    }
  }

  default void importDxl(final String dxl, final Database db) throws IOException {
    try (StringReader r = new StringReader(dxl)) {
      this.importDxl(r, db);
    }
  }

  default void importDxlFromACLExporter(final Database dbSource, final DxlExporter exporter, final Database dbTarget,
      final OutputStream debugExportOut) throws IOException {
    byte[] buf;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      OutputStream os;
      if (debugExportOut != null) {
        os = new SplitterOutputStream(baos, debugExportOut);
      } else {
        os = baos;
      }
      exporter.exportACL(dbSource, os);

      baos.flush();
      buf = baos.toByteArray();
    }

    try (InputStream is = new ByteArrayInputStream(buf)) {
      this.importDxl(is, dbTarget);
    }
  }

  default void importDxlFromDBExporter(final Database dbSource, final DxlExporter exporter, final Database dbTarget,
      final OutputStream debugExportOut) throws IOException {
    byte[] buf;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      OutputStream os;
      if (debugExportOut != null) {
        os = new SplitterOutputStream(baos, debugExportOut);
      } else {
        os = baos;
      }
      exporter.exportDatabase(dbSource, os);

      baos.flush();
      buf = baos.toByteArray();
    }

    try (InputStream is = new ByteArrayInputStream(buf)) {
      this.importDxl(is, dbTarget);
    }
  }

  default void importDxlFromIDExporter(final Database dbSource, final Collection<Integer> ids,
      final DxlExporter exporter, final Database dbTarget, final OutputStream debugExportOut) throws IOException {
    byte[] buf;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      OutputStream os;
      if (debugExportOut != null) {
        os = new SplitterOutputStream(baos, debugExportOut);
      } else {
        os = baos;
      }
      exporter.exportIDs(dbSource, ids, os);

      baos.flush();
      buf = baos.toByteArray();
    }

    try (InputStream is = new ByteArrayInputStream(buf)) {
      this.importDxl(is, dbTarget);
    }
  }

  boolean importErrorWasLogged();

  boolean isCreateFullTextIndex();

  boolean isExitOnFirstFatalError();

  boolean isReplaceDbProperties();

  boolean isReplicaRequiredForReplaceOrUpdate();

  void setACLImportOption(DXLImportOption option);

  void setCreateFullTextIndex(boolean b);

  void setDesignImportOption(DXLImportOption option);

  void setDocumentsImportOption(DXLImportOption option);

  void setExitOnFirstFatalError(boolean b);

  void setInputValidationOption(XMLValidationOption option);

  void setReplaceDbProperties(boolean b);

  void setReplicaRequiredForReplaceOrUpdate(boolean b);

  void setResultLogComment(String comment);

  void setUnknownTokenLogOption(DXLLogOption option);

}
