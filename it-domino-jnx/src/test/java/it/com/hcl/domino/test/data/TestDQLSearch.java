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

import static com.hcl.domino.dql.DQL.item;
import static com.hcl.domino.dql.DQL.or;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.DBQuery;
import com.hcl.domino.data.DQLQueryResult;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDQLSearch extends AbstractNotesRuntimeTest {

	@Test
	public void testDQLSearch() throws Exception {
		withTempDb((database) -> {
			generateNABPersons(database, 500);

			{
				DQLQueryResult result = database
						.queryDQL(
								or(
										item("Firstname").isEqualTo("Alexa"),
										item("Firstname").isEqualTo("Carlos")
										),
								EnumSet.of(DBQuery.EXPLAIN));

				String explainTxt = result.getExplainText();

				assertNotNull(explainTxt);
				assertTrue(explainTxt.length() > 0);

				IDTable resultsTable = result.getNoteIds().get();
				assertTrue(!resultsTable.isEmpty());

//				System.out.println("explain: "+result.getExplainText());
//				System.out.println("query result: "+result);
			}
		});

	}
}
