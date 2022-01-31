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
package com.hcl.domino.jnx.console;

import java.util.List;

import com.hcl.domino.jnx.console.internal.LoginSettings;

/**
 * Base implementation of {@link IConsoleCallback} that implements the
 * methods that are used less frequently.
 */
public abstract class BaseConsoleCallback implements IConsoleCallback {

  @Override
  public abstract void adminInfosReceived(List<String> serverAdministrators, List<String> restrictedAdministrators);

  @Override
  public void closeOpenPasswordDialog() {
  }

  @Override
  public void closeOpenPrompt() {
  }

  @Override
  public abstract void consoleInitialized(IDominoServerController console);

  @Override
  public abstract void consoleMessageReceived(IConsoleLine line);

  @Override
  public void dominoStatusReceived(final DominoStatus status) {
  }

  @Override
  public String passwordRequested(final String msg, final String title) {
    return null;
  }

  @Override
  public boolean requestLoginSettings(final LoginSettings loginSettings) {
    return false;
  }

  @Override
  public abstract void serverDetailsReceived(IServerDetails details);

  @Override
  public void setStatusMessage(final String msg) {
  }

  @Override
  public <T> T showInputDialog(final String msg, final String title, final T[] values, final T initialSelection) {
    return null;
  }

  @Override
  public void showMessageDialog(final String msg, final String title) {
  }

  @Override
  public <T> T showPrompt(final String msg, final String title, final T[] options) {
    return null;
  }

}
