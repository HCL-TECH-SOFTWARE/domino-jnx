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
package com.hcl.domino.jna.test.mq;

import java.io.IOException;

import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.test.AbstractJNARuntimeTest;

public abstract class AbstractMessageQueueRunner extends AbstractJNARuntimeTest implements Runnable {
  private boolean m_hasError = false;

  protected abstract void doRun() throws Exception;

  protected boolean hasError() {
    return this.m_hasError;
  }

  @Override
  public final void run() {
    try {
      AbstractJNARuntimeTest.initRuntime(false);

      this.initClient();

      this.doRun();
    } catch (final Exception e) {
      e.printStackTrace();

      this.m_hasError = true;
    } finally {
      try {
        this.termClient();
      } catch (final IOException e) {
        e.printStackTrace();

        this.m_hasError = true;
      }

      if (!DominoUtils.isNoTerm()) {
        NotesCAPI.get().NotesTerm();
      }
    }
  }
}
