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
	
	@Override
	public void start(Stage stage) throws Exception {
		String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
		String javafxVersion = System.getProperty("javafx.version"); //$NON-NLS-1$

		String notesUser = exec.submit(() -> {
			try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
				return client.getIDUserName();
			}
		}).get();
		
		Label notesUserLabel = new Label("Notes uer: " + notesUser);
		Label javaVersionLabel = new Label("Java version: " + javaVersion);
		Label javaFxVersionLabel = new Label("JavaFX version: " + javafxVersion);

		VBox root = new VBox(30, notesUserLabel, javaVersionLabel, javaFxVersionLabel);
		root.setAlignment(Pos.CENTER);
		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		DominoProcess.get().initializeProcess(args);
		try {
			launch(args);
		} finally {
			exec.shutdownNow();
			try {
				exec.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			DominoProcess.get().terminateProcess();
		}
	}
}
