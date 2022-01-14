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
package com.hcl.domino.jnx.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.DominoProcess;
import com.hcl.domino.misc.JNXThread;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Initializes and terminates the Notes runtime based on application init/term.
 */
@WebListener
public class NotesRuntimeListener implements ServletContextListener {
  public static ExecutorService executor;

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    NotesRuntimeListener.executor.shutdownNow();
    try {
      NotesRuntimeListener.executor.awaitTermination(1, TimeUnit.MINUTES);
    } catch (final InterruptedException e) {
    }
    DominoProcess.get().terminateProcess();
  }

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    DominoProcess.get().initializeProcess(new String[0]);
    NotesRuntimeListener.executor = Executors.newCachedThreadPool(JNXThread::new);
  }
}
