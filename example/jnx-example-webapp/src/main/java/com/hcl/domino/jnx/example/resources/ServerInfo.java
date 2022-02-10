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
package com.hcl.domino.jnx.example.resources;

import java.util.LinkedHashMap;
import java.util.Map;

import com.hcl.domino.DominoClient;
import com.hcl.domino.runtime.DominoRuntime;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/serverinfo")
public class ServerInfo {

  @Inject
  private DominoClient client;

  @Inject
  private HttpServletRequest req;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> get() {
    final Map<String, Object> result = new LinkedHashMap<>();
    result.put("userName", this.client.getEffectiveUserName()); //$NON-NLS-1$
    result.put("servletUser", this.req.getUserPrincipal().getName()); //$NON-NLS-1$
    result.put("idUserName", this.client.getIDUserName()); //$NON-NLS-1$
    result.put("isOnDomino", this.client.isOnServer()); //$NON-NLS-1$

    final DominoRuntime runtime = this.client.getDominoRuntime();
    result.put("dataDirectory", runtime.getDataDirectory().toString()); //$NON-NLS-1$
    result.put("programDirectory", runtime.getProgramDirectory().toString()); //$NON-NLS-1$

    return result;
  }

}
