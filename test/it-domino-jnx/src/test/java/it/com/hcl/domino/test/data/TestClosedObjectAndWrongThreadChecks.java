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
package it.com.hcl.domino.test.data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.exception.ObjectDisposedException;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestClosedObjectAndWrongThreadChecks extends AbstractNotesRuntimeTest {

  @Override
  protected boolean isRestrictThreadAccess() {
    return true;
  }

  @Test
  public void testInvalidStates() throws InterruptedException, ExecutionException {
    final AtomicReference<Database> dbRef = new AtomicReference<>();
    final AtomicReference<DominoCollection> viewRef = new AtomicReference<>();
    final AtomicReference<DominoClient> clientRef = new AtomicReference<>();

    try (DominoClient client = this.getClient();) {
      clientRef.set(client);
      final Database db = client.openDatabase("pernames.ntf");
      dbRef.set(db);
      final DominoCollection view = db.openCollection("People").get();
      viewRef.set(view);

      // try to access the API object from a non-owner thread
      final Executor executor = Executors.newCachedThreadPool();
      final FutureTask<Class<? extends Exception>> task = new FutureTask<>(() -> {
        DominoProcess.get().initializeThread();
        try {
          view.getAliases();
          return null;
        } catch (final Exception e) {
          return e.getClass();
        } finally {
          DominoProcess.get().terminateThread();
        }
      });
      executor.execute(task);

      Assertions.assertEquals(IllegalStateException.class, task.get());

    }

    // try to reuse the already closed DominoClient
    Assertions.assertThrows(ObjectDisposedException.class, () -> {
      final Database db = clientRef.get().openDatabase("names.nsf");
      final DominoCollection view = db.openCollection("People").get();
      view.getAliases();
    });

    // try to access API objects of the already closed DominoClient
    Assertions.assertThrows(ObjectDisposedException.class, () -> {
      clientRef.get().openDatabase("names.nsf");
    });

    Assertions.assertThrows(ObjectDisposedException.class, () -> {
      dbRef.get().queryDQL(DQL.item("Form").in("Memo"));
    });

    Assertions.assertThrows(ObjectDisposedException.class, () -> {
      viewRef.get().getAliases();
    });

    Assertions.assertThrows(ObjectDisposedException.class, () -> {
      viewRef.get().getAllIds(true, true);
    });

  }

}
