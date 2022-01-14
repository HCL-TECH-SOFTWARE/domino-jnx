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
package com.hcl.domino.example.gluon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.misc.JNXThread;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
  private static final ExecutorService exec = Executors.newCachedThreadPool(JNXThread::new);

  public static void main(final String[] args) {
    DominoProcess.get().initializeProcess(args);
    try {
      Application.launch(args);
    } finally {
      Main.exec.shutdownNow();
      try {
        Main.exec.awaitTermination(1, TimeUnit.MINUTES);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      DominoProcess.get().terminateProcess();
    }
  }

  @Override
  public void start(final Stage stage) throws Exception {
    final String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
    final String javafxVersion = System.getProperty("javafx.version"); //$NON-NLS-1$

    final String notesUser = Main.exec.submit(() -> {
      try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        return client.getIDUserName();
      }
    }).get();

    final Label notesUserLabel = new Label("Notes uer: " + notesUser);
    final Label javaVersionLabel = new Label("Java version: " + javaVersion);
    final Label javaFxVersionLabel = new Label("JavaFX version: " + javafxVersion);

    final VBox root = new VBox(30, notesUserLabel, javaVersionLabel, javaFxVersionLabel);
    root.setAlignment(Pos.CENTER);
    final Scene scene = new Scene(root, 640, 480);
    stage.setScene(scene);
    stage.show();
  }
}
