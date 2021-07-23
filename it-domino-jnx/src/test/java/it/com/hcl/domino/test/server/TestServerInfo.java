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
