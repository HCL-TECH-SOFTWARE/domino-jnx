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
package it.com.hcl.domino.test.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.security.ServerAclType;
import com.hcl.domino.server.ServerInfo;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestServerInfo extends AbstractNotesRuntimeTest {
	public static final String ENV_SERVER = "SERVER_INFO_SERVER";
	public static final String ENV_ADMIN = "SERVER_INFO_ADMINISTRATOR";
	
	@Test
	@EnabledIfEnvironmentVariable(named = ENV_SERVER, matches = ".*")
	@EnabledIfEnvironmentVariable(named = ENV_ADMIN, matches = ".*")
	public void testServerInfoAdminUser() throws Exception {
		DominoClient client = getClient();
		String server = System.getenv(ENV_SERVER);
		String admin = System.getenv(ENV_ADMIN);
		
		ServerInfo info = client.getServerInfo(server, server);
		assertNotNull(info);
		assertTrue(info.isAclMember(ServerAclType.SERVER_ADMIN, admin));
	}
	@Test
	@EnabledIfEnvironmentVariable(named = ENV_SERVER, matches = ".*")
	public void testServerInfoFakeAdminUser() throws Exception {
		DominoClient client = getClient();
		String server = System.getenv(ENV_SERVER);
		String admin = "I expect that I do not exist";
		
		ServerInfo info = client.getServerInfo(server, server);
		assertNotNull(info);
		assertFalse(info.isAclMember(ServerAclType.SERVER_ADMIN, admin));
	}
}
