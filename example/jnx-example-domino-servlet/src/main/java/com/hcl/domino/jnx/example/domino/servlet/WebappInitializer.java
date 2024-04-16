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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.mq.MessageQueue;
import com.hcl.domino.server.ServerStatusLine;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

public class WebappInitializer extends HttpService {

  public static final String MQ_NAME = "MQ$EXAMPLEAPP"; //$NON-NLS-1$
  private DominoClient client;
  private ExecutorService exec;
  private MessageQueue mq;
  private ServerStatusLine statusLine;
  private int messageCount;

  public WebappInitializer(final LCDEnvironment env) {
    super(env);

    try {
      DominoUtils.setNoInit(true);
      DominoUtils.setNoTerm(true);

      DominoProcess.get().initializeProcess(new String[0]);
      this.client = DominoClientBuilder.newDominoClient().build();
      this.exec = Executors.newCachedThreadPool(this.client.getThreadFactory());
      this.exec.submit(() -> {
        try {
          this.statusLine = this.client.getServerAdmin().createServerStatusLine("ExampleApp");
          this.statusLine.setLine("Waiting");
          this.mq = this.client.getMessageQueues().createAndOpen(WebappInitializer.MQ_NAME, 0);

          System.out.println("JNX Example Webapp initialized. Use `tell exampleapp foo` to mirror messages");

          String message;
          try {
            while ((message = this.mq.take()) != null) {
              System.out.println("Received message " + message);
              this.statusLine.setLine("Processed count: " + ++this.messageCount);
            }
          } catch (InterruptedException | ObjectDisposedException e) {
            // This occurs during shutdown
          }
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      });
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  public void destroyService() {
    super.destroyService();

    try {
      this.exec.submit(() -> {
        try {
          this.statusLine.close();
          this.mq.close();
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }).get();
      this.exec.shutdownNow();
      try {
        this.exec.awaitTermination(1, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      this.client.close();
      DominoProcess.get().terminateProcess();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  public boolean doService(final String arg0, final String arg1, final HttpSessionAdapter session,
      final HttpServletRequestAdapter req,
      final HttpServletResponseAdapter resp) throws ServletException, IOException {
    // NOP
    return false;
  }

  // *******************************************************************************
  // * Stub service methods
  // *******************************************************************************

  @Override
  public void getModules(final List<ComponentModule> arg0) {
    // NOP
  }

}
