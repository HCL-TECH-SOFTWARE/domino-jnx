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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Database.FormulaQueryCallback;
import com.hcl.domino.data.Database.SearchMatch;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.TypedAccess;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestFormulaSearch extends AbstractNotesRuntimeTest {

	@Test
	public void testSearch() throws Exception {
		String selectionFormula = "form=\"MatchForm\"";
		
		//specify with doc items we want to read/compute
		Map<String,String> computeValues = new HashMap<>();
		computeValues.put("form", "");
		computeValues.put("_created", "@created");
		computeValues.put("randomvalue", "");
		
		Set<SearchFlag> searchFlags = EnumSet.of(SearchFlag.NOTIFYDELETIONS);
		Set<DocumentClass> docClass = EnumSet.of(DocumentClass.DATA, DocumentClass.NOTIFYDELETION);
		
		withTempDb((database) -> {
			DominoDateTime since = null;
			
			{
				//Initial search, database is empty
				AtomicInteger matchCount = new AtomicInteger();
				AtomicInteger nonMatchCount = new AtomicInteger();
				AtomicInteger deletionCount = new AtomicInteger();
				
				since = database.queryFormula(selectionFormula, null,
						searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {
							
							@Override
							public Action nonMatchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								nonMatchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action matchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								matchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action deletionFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								deletionCount.incrementAndGet();
								return Action.Continue;
							}
						});
				
				assertNotNull(since);
				assertEquals(0, matchCount.get());
				assertEquals(0, nonMatchCount.get());
				assertEquals(0, deletionCount.get());
			}
			
			//create a non-match
			Document nonMatchDoc = database.createDocument();
			nonMatchDoc.replaceItemValue("Form", "NotMatchForm");
			nonMatchDoc.save();
//			System.out.println("Wrote non-match doc with UNID "+nonMatchDoc.getUNID());

			{
				AtomicInteger matchCount = new AtomicInteger();
				AtomicInteger nonMatchCount = new AtomicInteger();
				AtomicInteger deletionCount = new AtomicInteger();
				
				since = database.queryFormula(selectionFormula, null,
						searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {
							
							@Override
							public Action nonMatchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								nonMatchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action matchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								matchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action deletionFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								deletionCount.incrementAndGet();
								return Action.Continue;
							}
						});
				
				assertNotNull(since);
				assertEquals(0, matchCount.get());
				assertEquals(1, nonMatchCount.get());
				assertEquals(0, deletionCount.get());
			}
			
			//delete non-match
			nonMatchDoc.delete();
			
			{
				AtomicInteger matchCount = new AtomicInteger();
				AtomicInteger nonMatchCount = new AtomicInteger();
				AtomicInteger deletionCount = new AtomicInteger();
				
				since = database.queryFormula(selectionFormula, null,
						searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {
							
							@Override
							public Action nonMatchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								nonMatchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action matchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								matchCount.incrementAndGet();
								
								return Action.Continue;
							}
							
							@Override
							public Action deletionFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
//								System.out.println("Deletion found, unid="+searchMatch.getUNID());
								
								deletionCount.incrementAndGet();
								return Action.Continue;
							}
						});
				
				assertNotNull(since);
				assertEquals(0, matchCount.get());
				assertEquals(0, nonMatchCount.get());
				assertEquals(1, deletionCount.get());
			}
			
			Document matchDoc = database.createDocument();
			matchDoc.replaceItemValue("Form", "MatchForm");
			String writtenRandomValue = UUID.randomUUID().toString();
			matchDoc.replaceItemValue("RandomValue", writtenRandomValue);
			matchDoc.save();
			String matchDocUnid = matchDoc.getUNID();
//			System.out.println("Wrote match doc with UNID "+matchDocUnid);
			
			{
				AtomicInteger matchCount = new AtomicInteger();
				AtomicInteger nonMatchCount = new AtomicInteger();
				AtomicInteger deletionCount = new AtomicInteger();
				
				since = database.queryFormula(selectionFormula, null,
						searchFlags, since, docClass, computeValues, new FormulaQueryCallback() {
							
							@Override
							public Action nonMatchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								nonMatchCount.incrementAndGet();
								return Action.Continue;
							}
							
							@Override
							public Action matchFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								matchCount.incrementAndGet();
								
//								System.out.println("Match found, unid="+searchMatch.getUNID()+", items="+computedValues.getItemNames());
								
								assertEquals(matchDocUnid, searchMatch.getUNID());
								
								String form = computedValues.get("form", String.class, "");
								assertEquals("MatchForm", form, "Form is correct");

								String readRandomValue = computedValues.get("RandomValue", String.class, "");
								assertEquals(writtenRandomValue, readRandomValue, "Random value is correct");

								DominoDateTime readCreationDate = computedValues.get("_created", DominoDateTime.class, null);
								assertNotNull(readCreationDate);
								assertNotNull(matchDoc.getCreated());
								assertEquals(matchDoc.getCreated().toTemporal(), readCreationDate.toTemporal(), "Creation date is correct");
								
								return Action.Continue;
							}
							
							@Override
							public Action deletionFound(Database db, SearchMatch searchMatch, TypedAccess computedValues) {
								deletionCount.incrementAndGet();
								return Action.Continue;
							}
						});
				
				assertNotNull(since);
				assertEquals(1, matchCount.get());
				assertEquals(0, nonMatchCount.get());
				assertEquals(0, deletionCount.get());
			}
			
		});

		
	}
}
