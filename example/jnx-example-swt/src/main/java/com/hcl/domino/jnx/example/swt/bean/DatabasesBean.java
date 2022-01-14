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
package com.hcl.domino.jnx.example.swt.bean;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.hcl.domino.DominoClient;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.dbdirectory.DbDirectory;
import com.hcl.domino.dbdirectory.DirEntry;
import com.hcl.domino.dbdirectory.FileType;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DatabasesBean {

  @Inject
  private DominoClient client;

  @Inject
  private ExecutorService executor;

  public Collection<String> getDatabasePaths(final String serverName) {
    try {
      return this.executor.submit(() -> {
        if(StringUtil.isEmpty(serverName)) {
          // Special handling for local, as this will be on a client - getDatabasePath is
          //   unreliable there
          DbDirectory dir = client.openDbDirectory();
          return dir.query()
            .withServer(null)
            .withDirectory("") //$NON-NLS-1$
            .withFormula("") //$NON-NLS-1$
            .withFlags(EnumSet.of(SearchFlag.FILETYPE, SearchFlag.SUMMARY))
            .withFileTypes(EnumSet.of(FileType.DBANY, FileType.RECURSE))
            .stream()
            .map(DirEntry::getFilePath)
            .collect(Collectors.toList());
        } else {
          return this.client.getDatabasePaths(serverName, null).getDatabasePaths();
        }
      }).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<String> getKnownServers() {
    try {
      return this.executor.submit(() -> this.client.getKnownServers(null)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
