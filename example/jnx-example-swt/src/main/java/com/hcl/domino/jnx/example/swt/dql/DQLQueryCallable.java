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
package com.hcl.domino.jnx.example.swt.dql;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.DQLQueryResult;
import com.hcl.domino.data.Database;
import com.hcl.domino.dql.QueryResultsProcessor;
import com.hcl.domino.dql.QueryResultsProcessor.QRPOptions;

public class DQLQueryCallable implements Callable<String> {

  private final String sourceDb;
  private final String query;
  private final String[] extract;

  public DQLQueryCallable(String sourceDb, String query, String[] extract) {
    this.sourceDb = sourceDb;
    this.query = query;
    this.extract = extract;
    System.out.println("query: " + this.query);
    System.out.println("extract: " + Arrays.toString(this.extract));
  }

  @Override
  public String call() throws Exception {
    if(StringUtil.isNotEmpty(sourceDb)) {
      try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
        Database database = client.openDatabase(sourceDb);
        if(database == null) {
          throw new RuntimeException("Unable to open database " + sourceDb); //$NON-NLS-1$
        }
        
        DQLQueryResult queryResult = database.queryDQL(this.query);
        Database names = client.openDatabase("names.nsf");
        QueryResultsProcessor processor = names.createQueryResultsProcessor();
        for(String item : extract) {
          processor.addColumn(item);
        }
        processor.addNoteIds(database, queryResult.collectIds(0, Integer.MAX_VALUE), null);
        
        StringBuilder result = new StringBuilder();
        try(
          Reader r = processor.executeToJSON(EnumSet.allOf(QRPOptions.class));
          BufferedReader br = new BufferedReader(r);
        ) {
          String line;
          while((line = br.readLine()) != null) {
            result.append(line);
          }
        }
        return result.toString();
      } catch(Exception ne) {
        try(
          StringWriter w = new StringWriter();
          PrintWriter pw = new PrintWriter(w);
        ) {
          ne.printStackTrace(pw);
          pw.flush();
          return w.toString();
        }
      }
    }
    return ""; //$NON-NLS-1$
  }

}