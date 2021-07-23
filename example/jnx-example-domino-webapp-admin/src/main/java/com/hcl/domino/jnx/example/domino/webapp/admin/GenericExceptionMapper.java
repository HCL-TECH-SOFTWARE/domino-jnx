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
package com.hcl.domino.jnx.example.domino.webapp.admin;

import java.util.Arrays;
import java.util.Collection;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.hcl.domino.commons.util.StringUtil;

public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable exception) {
		JsonObjectBuilder result = Json.createObjectBuilder().add("status", 500) //$NON-NLS-1$
				.add("message", exception.getLocalizedMessage()); //$NON-NLS-1$
		JsonArrayBuilder stackTrace = Json.createArrayBuilder();

		for (Throwable t = exception; t != null; t = t.getCause()) {
			JsonArrayBuilder stack = Json.createArrayBuilder();
			stack.add(t.getClass().getName() + ": " + t.getLocalizedMessage()); //$NON-NLS-1$
			Arrays.stream(trimStackTrace(t.getStackTrace())).map(String::valueOf).map(line -> " at " + line) //$NON-NLS-1$
					.forEach(stack::add);
			stackTrace.add(stack);
		}

		result.add("stackTrace", stackTrace); //$NON-NLS-1$

		return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
				.entity(result.build()).build();
	}

	private static final Collection<String> IGNORED_STACK_CLASSES = Arrays
			.asList("com.hcl.domino.jnx.example.NotesRequestFilter" //$NON-NLS-1$
			);

	public static StackTraceElement[] trimStackTrace(final StackTraceElement[] stackTrace) {
		int cmssiteIndex;
		for (cmssiteIndex = stackTrace.length - 1; cmssiteIndex >= 0; cmssiteIndex--) {
			StackTraceElement el = stackTrace[cmssiteIndex];
			String className = el.getClassName();
			if (StringUtil.isNotEmpty(className) && className.startsWith("com.hcl") //$NON-NLS-1$
					&& !IGNORED_STACK_CLASSES.contains(className)) {
				break;
			}
		}
		if (cmssiteIndex == -1) {
			return stackTrace;
		} else {
			return Arrays.copyOfRange(stackTrace, 0, cmssiteIndex + 1);
		}
	}

}
