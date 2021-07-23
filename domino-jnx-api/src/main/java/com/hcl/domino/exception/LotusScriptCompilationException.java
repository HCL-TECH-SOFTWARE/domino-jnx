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
package com.hcl.domino.exception;

import java.text.MessageFormat;

import com.hcl.domino.DominoException;

/**
 * Specialized exception type for the contents of LSCOMPILE_ERR_INFO
 *
 * @since 1.0.5
 */
public class LotusScriptCompilationException extends DominoException {
  private static final long serialVersionUID = 1L;

  private static String toMessage(final String errorText, final String errorFile, final int version, final int line) {
    return MessageFormat.format("Line {0}: {1}", line, errorText);
  }

  private final String errorText;
  private final String errorFile;
  private final int version;

  private final int line;

  public LotusScriptCompilationException(final String errorText, final String errorFile, final int version, final int line) {
    super(LotusScriptCompilationException.toMessage(errorText, errorFile, version, line));

    this.errorText = errorText;
    this.errorFile = errorFile;
    this.version = version;
    this.line = line;
  }

  /**
   * @return the script file name, if applicable
   */
  public String getErrorFile() {
    return this.errorFile;
  }

  /**
   * @return the human-readable description of the error
   */
  public String getErrorText() {
    return this.errorText;
  }

  /**
   * @return source line number, relative to the module containing the error
   */
  public int getLine() {
    return this.line;
  }

  /**
   * Retrieves the error version. The only known value currently is {@code 1}.
   *
   * @return the error version
   */
  public int getVersion() {
    return this.version;
  }
}
