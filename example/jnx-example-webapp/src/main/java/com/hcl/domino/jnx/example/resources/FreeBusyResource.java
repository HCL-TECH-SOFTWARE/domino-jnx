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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/freebusy")
public class FreeBusyResource {

  @GET
  @Path("freeTimeSearch")
  public Object freeTimeSearch(
      @QueryParam("findFirstFit") final boolean findFirstFit,
      @QueryParam("from") final String fromDate,
      @QueryParam("until") final String untilDate,
      @QueryParam("duration") final int duration,
      @QueryParam("names") final List<String> names) throws IOException {

    try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      final TemporalAccessor from = DateTimeFormatter.ISO_INSTANT.parse(fromDate);
      final TemporalAccessor until = DateTimeFormatter.ISO_INSTANT.parse(untilDate);

      return client.getFreeBusy().freeTimeSearch(null, null, findFirstFit, from, until, duration, names);
    }
  }

}
