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
package com.hcl.domino.jnx.example.resources;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.data.Database;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("collections")
public class CollectionsResource {
  @Inject
  DominoClient client;

  @GET
  @Path("{dbName}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Map<String, Object>> getCollections(@PathParam("dbName") final String dbName) {
    // Use a native client to avoid a crasher with DBs that don't exist (see Issue
    // #96)
    try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      final Database database = client.openDatabase(dbName);
      return database.getAllCollections()
          .map(c -> {
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("title", c.getTitle()); //$NON-NLS-1$
            result.put("aliases", c.getAliases()); //$NON-NLS-1$
            result.put("isfolder", c.isFolder()); //$NON-NLS-1$
            result.put("noteid", c.getNoteID()); //$NON-NLS-1$
            return result;
          })
          .collect(Collectors.toList());
    }
  }
}
