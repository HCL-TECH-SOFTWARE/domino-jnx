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
			@QueryParam("findFirstFit") boolean findFirstFit,
			@QueryParam("from") String fromDate,
			@QueryParam("until") String untilDate,
			@QueryParam("duration") int duration,
			@QueryParam("names") List<String> names
		) throws IOException {

		try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
			TemporalAccessor from = DateTimeFormatter.ISO_INSTANT.parse(fromDate);
			TemporalAccessor until = DateTimeFormatter.ISO_INSTANT.parse(untilDate);

			return client.getFreeBusy().freeTimeSearch(null, null, findFirstFit, from, until, duration, names);
		}
	}

}
