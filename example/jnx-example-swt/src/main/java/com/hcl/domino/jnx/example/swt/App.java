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
package com.hcl.domino.jnx.example.swt;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

public class App {
  public static final String APP_NAME = "Domino Example Application"; //$NON-NLS-1$

  public static final ImageDescriptor IMAGE_SERVER;
  public static final ImageDescriptor IMAGE_DATABASE;
  public static final ImageDescriptor IMAGE_STORE;
  public static final ImageDescriptor IMAGE_STORE_LOCAL;
  static {
    IMAGE_SERVER = ImageDescriptor.createFromURL(App.class.getResource("/icons/network-server.png")); //$NON-NLS-1$
    IMAGE_DATABASE = ImageDescriptor.createFromURL(App.class.getResource("/icons/system-file-manager.png")); //$NON-NLS-1$
    IMAGE_STORE = ImageDescriptor.createFromURL(App.class.getResource("/icons/folder.png")); //$NON-NLS-1$
    IMAGE_STORE_LOCAL = ImageDescriptor.createFromURL(App.class.getResource("/icons/folder-transparent.png")); //$NON-NLS-1$
  }

  public static DominoClient client;
  private static ExecutorService executor;

  public static synchronized ExecutorService getExecutor() {
    if (App.executor == null) {
      // Note: process init must be delayed to here to avoid thread trouble with SWT

      final String notesProgramDir = System.getenv("Notes_ExecDirectory"); //$NON-NLS-1$
      final String notesIniPath = System.getenv("NotesINI"); //$NON-NLS-1$
      if (notesProgramDir != null && !notesProgramDir.isEmpty()) {
        final String[] initArgs = new String[] {
            notesProgramDir,
            notesIniPath == null ? "" : "=" + notesIniPath //$NON-NLS-1$ //$NON-NLS-2$
        };

        DominoProcess.get().initializeProcess(initArgs);
      } else {
        throw new IllegalStateException("Unable to locate Notes runtime");
      }
      App.executor = Executors.newCachedThreadPool(App.client.getThreadFactory());
    }
    return App.executor;
  }

  public static void main(final String[] args) {
    try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", App.APP_NAME); //$NON-NLS-1$

      try {
        App.client = DominoClientBuilder.newDominoClient().build();
        final Display display = Display.getDefault();
        try {
          Display.setAppName(App.APP_NAME);
          final AppShell shell = new AppShell(display);

          if (SwtUtil.isMac()) {
            final Menu systemMenu = display.getSystemMenu();
            Arrays.stream(systemMenu.getItems())
                .filter(item -> item.getID() == SWT.ID_ABOUT)
                .findFirst()
                .ifPresent(item -> item.addListener(SWT.Selection, e -> App.openAbout()));
          }
          
          shell.open();
          shell.layout();
          shell.setFocus();

          while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
              display.sleep();
            }
          }
        } catch (final Exception e) {
          e.printStackTrace();
        } finally {
          display.dispose();
          // client.close();
        }
      } finally {
        if (App.executor != null) {
          App.executor.shutdownNow();
          try {
            App.executor.awaitTermination(30, TimeUnit.SECONDS);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }
        DominoProcess.get().terminateProcess();
      }
    }
  }

  private static void openAbout() {
    MessageDialog.openInformation(null, App.APP_NAME,
        "ID user: " + App.client.getIDUserName());
  }
}
