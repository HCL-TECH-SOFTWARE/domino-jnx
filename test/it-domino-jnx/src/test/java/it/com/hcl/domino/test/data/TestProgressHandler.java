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
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.ProgressAdapter;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestProgressHandler extends AbstractNotesRuntimeTest {

  @Test
  public void testProgressHandler() throws Exception {
    final int numDocs = 30;

    this.withTempDb(database -> {
      AbstractNotesRuntimeTest.generateNABPersons(database, numDocs);

      final DominoClient client = this.getClient();

      final Set<Long> progressEvents = new HashSet<>();
      for (long i = 1; i <= numDocs; i++) {
        progressEvents.add(i);
      }

      // run FT search on unindexed database which create a
      // temp FT index for all documents
      client.runWithProgress(() -> {
        return database
            .queryFTIndex("Lehmann", 0, EnumSet.of(FTQuery.NOINDEX),
                database
                    .getAllNoteIds(EnumSet.of(DocumentClass.DATA), false),
                0, 0);
      }, new ProgressAdapter() {

        @Override
        public void setPos(final long pos) {
          progressEvents.remove(pos);
        }
      });

      // check if we got all progress events
      Assertions.assertTrue(progressEvents.isEmpty());
    });

  }

}
