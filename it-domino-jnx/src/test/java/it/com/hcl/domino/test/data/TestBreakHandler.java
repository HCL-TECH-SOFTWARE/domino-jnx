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
package it.com.hcl.domino.test.data;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoException;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FTQuery;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestBreakHandler extends AbstractNotesRuntimeTest {

  @Test
  public void testBreakHandler() throws Exception {
    final int numDocs = 200;

    this.withTempDb(database -> {
      AbstractNotesRuntimeTest.generateNABPersons(database, numDocs);

      final DominoClient client = this.getClient();

      // make sure to not collect more than 5000 note ids, because
      // searching the DB with temp FT index has this limit
      final Set<Integer> someIds = database
          .getAllNoteIds(EnumSet.of(DocumentClass.DATA), false)
          .stream()
          .limit(5000)
          .collect(Collectors.toSet());

      final AtomicInteger breakHandlerInvocations = new AtomicInteger();

      boolean receivedBreakSignal = false;

      try {
        client.runInterruptable(() -> {

          // run FT search on unindexed database which create a
          // temp FT index with break handler invocations
          return database
              .queryFTIndex("Lehmann", 0, EnumSet.of(FTQuery.NOINDEX), someIds, 0, 0);

        }, () -> {
          // count handler invocation
          final int oldVal = breakHandlerInvocations.getAndIncrement();
          if (oldVal == 1) {
            // send break signal on second invocation
            // return true;
            return Action.Stop;
          } else {
            return Action.Continue;
          }
        });
      } catch (final DominoException e) {
        if (e.getId() == 413) { // Operation stopped at your request
          receivedBreakSignal = true;
        } else {
          throw e;
        }
      }

      Assertions.assertTrue(breakHandlerInvocations.get() > 1);
      Assertions.assertTrue(receivedBreakSignal);
    });

  }

}
