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
package com.hcl.domino.commons.dxl;

import java.text.MessageFormat;
import java.util.List;

import com.hcl.domino.dxl.DxlImporterLog;

/**
 * Representation of a DXL importer log.
 *
 * @author Jesse Gallagher
 */
public class DxlImporterLogImpl implements DxlImporterLog {
  public static class DxlErrorImpl implements DxlError {
    private int id;
    private String text;
    private String source;
    private int line;
    private int column;

    @Override
    public int getColumn() {
      return this.column;
    }

    @Override
    public int getId() {
      return this.id;
    }

    @Override
    public int getLine() {
      return this.line;
    }

    @Override
    public String getSource() {
      return this.source;
    }

    @Override
    public String getText() {
      return this.text;
    }

    public void setColumn(final int column) {
      this.column = column;
    }

    public void setId(final int id) {
      this.id = id;
    }

    public void setLine(final int line) {
      this.line = line;
    }

    public void setSource(final String source) {
      this.source = source;
    }

    public void setText(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return MessageFormat.format("Error: id={0}: {1}", this.id, this.text); //$NON-NLS-1$
    }
  }

  public static class DxlFatalErrorImpl implements DxlFatalError {
    private String source;
    private int line;
    private int column;
    private String text;

    @Override
    public int getColumn() {
      return this.column;
    }

    @Override
    public int getLine() {
      return this.line;
    }

    @Override
    public String getSource() {
      return this.source;
    }

    @Override
    public String getText() {
      return this.text;
    }

    public void setColumn(final int column) {
      this.column = column;
    }

    public void setLine(final int line) {
      this.line = line;
    }

    public void setSource(final String source) {
      this.source = source;
    }

    public void setText(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return MessageFormat.format("Fatal Error: source={0}, line={1}, column={2}: {3}", this.source, this.line, this.column, //$NON-NLS-1$
          this.text);
    }
  }

  private List<DxlFatalError> fatalErrors;
  private List<DxlError> errors;

  public DxlImporterLogImpl(final List<DxlError> errors, final List<DxlFatalError> fatalErrors) {
    this.errors = errors;
    this.fatalErrors = fatalErrors;

  }

  @Override
  public List<DxlError> getErrors() {
    return this.errors;
  }

  @Override
  public List<DxlFatalError> getFatalErrors() {
    return this.fatalErrors;
  }

  public void setErrors(final List<DxlError> errors) {
    this.errors = errors;
  }

  public void setFatalErrors(final List<DxlFatalError> fatalErrors) {
    this.fatalErrors = fatalErrors;
  }
}
