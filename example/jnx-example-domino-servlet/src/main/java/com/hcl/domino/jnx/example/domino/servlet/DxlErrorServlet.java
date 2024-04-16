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
package com.hcl.domino.jnx.example.domino.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.dxl.DxlImporter;

/**
 * This servlet is intended to generate and output an error during DXL import,
 * which tests that Jakarta XML Binding 3.0 functions on Domino.
 * 
 * @author Jesse Gallagher
 * @since 1.0.12
 */
public class DxlErrorServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private DominoClient client;

  @Override
  public void destroy() {
    super.destroy();

    this.client.close();
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/plain"); //$NON-NLS-1$
    try (PrintWriter w = resp.getWriter()) {
      try {
        String primaryDirectory = this.client.openUserDirectory(null).getPrimaryDirectoryPath()
            .orElseThrow(() -> new IllegalStateException("Unable to identify primary directory path"));
        final Database names = this.client.openDatabase(primaryDirectory);
        final DxlImporter importer = this.client.createDxlImporter();
        importer.importDxl("I am not valid DXL", names); //$NON-NLS-1$
      } catch (final Throwable t) {
        t.printStackTrace(w);
      }
    }
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    DominoUtils.setNoInit(true);
    DominoUtils.setNoTerm(true);

    this.client = DominoClientBuilder.newDominoClient().build();
  }

  @Override
  protected void service(final HttpServletRequest arg0, final HttpServletResponse arg1) throws ServletException, IOException {
    DominoProcess.get().initializeThread();
    try {
      super.service(arg0, arg1);
    } finally {
      DominoProcess.get().terminateThread();
    }
  }

}
