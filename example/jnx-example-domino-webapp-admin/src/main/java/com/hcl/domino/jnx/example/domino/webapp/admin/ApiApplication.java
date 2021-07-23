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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.hcl.domino.jnx.example.domino.webapp.admin.console.ConsoleResource;
import com.hcl.domino.jnx.example.domino.webapp.admin.console.KnownServersResource;

public class ApiApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(
			HelloWorldResource.class,
			CollectionsResource.class
		));
	}
	
	@Override
	public Set<Object> getSingletons() {
		BasicCorsFilter cors = new BasicCorsFilter();
		cors.getAllowedOrigins().add("*"); //$NON-NLS-1$
		cors.setAllowedHeaders("origin, content-type, accept, authorization, x-requested-with"); //$NON-NLS-1$
		cors.setAllowedMethods("GET, POST, PUT, DELETE, OPTIONS, HEAD"); //$NON-NLS-1$
		cors.setCorsMaxAge(1209600);

		return new HashSet<>(Arrays.asList(
			cors,
			new ConsoleResource(),
			new KnownServersResource(),
			new JsonbMessageBodyWriter(),
			new GenericExceptionMapper()
		));
	}
}