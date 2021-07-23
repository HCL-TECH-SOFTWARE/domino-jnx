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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.data.Agent;
import com.hcl.domino.data.Database;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestAgent extends AbstractNotesRuntimeTest {	
	public static final String ENV_SERVERAGENTDB = "ServerAgentDB"; //$NON-NLS-1$
	public static final String ENV_SERVERAGENGNAME = "ServerAgentName"; //$NON-NLS-1$
	
	@Test
	@EnabledIfEnvironmentVariable(named = ENV_SERVERAGENTDB, matches = ".+")
	@EnabledIfEnvironmentVariable(named = ENV_SERVERAGENGNAME, matches = ".+")
	public void testRunOnServer() {
		String serverAgentDb = System.getenv(ENV_SERVERAGENTDB);
		String serverAgentName = System.getenv(ENV_SERVERAGENGNAME);
		
		Database database = getClient().openDatabase(serverAgentDb);
		Agent agent = database.getAgent(serverAgentName).get();
		agent.runOnServer(false);
	}
}
