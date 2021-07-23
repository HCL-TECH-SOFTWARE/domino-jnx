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
package com.hcl.domino.misc;

import com.hcl.domino.DominoProcess;
import com.hcl.domino.DominoProcess.DominoThreadContext;

/**
 * Thread subclass that runs with a thread initialized and terminated via
 * {@link DominoProcess#initializeThread()} and
 * {@link DominoProcess#terminateThread()}.
 *
 * @author Jesse Gallagher
 */
public class JNXThread extends Thread {
  private final Runnable target;

  public JNXThread() {
    super();
    this.target = null;
  }

  public JNXThread(final Runnable target) {
    super(target, "Domino JNX Thread");
    this.target = target;
  }

  /**
   * This method can be overridden by subclasses to perform their actions within
   * the Notes-initialized environment.
   */
  protected void doRun() {

  }

  @Override
  public final void run() {
    try (DominoThreadContext ctx = DominoProcess.get().initializeThread()) {
      if (this.target != null) {
        super.run();
      } else {
        this.doRun();
      }
    }
  }
}
