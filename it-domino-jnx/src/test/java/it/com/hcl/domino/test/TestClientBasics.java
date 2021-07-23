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
package it.com.hcl.domino.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.function.Executable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.LifecycleListener;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.exception.ServerNotFoundException;
import com.hcl.domino.server.ServerPingInfo;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestClientBasics extends AbstractNotesRuntimeTest {
	public static final String PING_SERVER = "PING_SERVER";

	@Test
	public void testJNADominoClientBuilder() throws IOException {
		try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
			assertNotNull(client);
		}
	}
	
	@Test
	public void testUseClosedClient() throws IOException {
		DominoClient client = DominoClientBuilder.newDominoClient().build();
		client.close();
		
		assertThrows(DominoException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				Database db = client.openDatabase("names.nsf");
			}
		}, "Did not throw exception when using closed DominoClient");
	}
	
	@Test
	public void testUseClosedClientsObjects() throws IOException {
		DominoClient client = DominoClientBuilder.newDominoClient().build();
		Database db = client.openDatabase("names.nsf");
		client.close();
		
		assertThrows(DominoException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				db.getACL();
			}
		}, "Did not throw exception when using database of closed DominoClient");
	}
	
	@Test
	public void testUseClosedClientsObjectMethods() throws IOException {
		DominoClient client = DominoClientBuilder.newDominoClient().build();
		Database db = client.openDatabase("names.nsf");
		client.close();
		
		assertThrows(DominoException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				db.getOptions();
			}
		}, "Did not throw exception when using database methods of closed DominoClient");
	}
	
	@Test
	public void testNonEmptyUsername() {
		assertTrue(StringUtil.isNotEmpty(getClient().getIDUserName()));
	}
	
	@Test
	public void testBaseUsernameEquality() {
		DominoClient client = getClient();
		assertEquals(client.getIDUserName(), client.getEffectiveUserName());
	}

	@Test
	public void testArbitraryUsername() throws Exception {
		DominoClient client = getClient();
		
		String expected = "CN=Foo Bar/O=Baz";
		try (DominoClient fooClient = DominoClientBuilder.newDominoClient()
				.asUser(expected)
				.build()) {

			assertEquals(expected, fooClient.getEffectiveUserName());
			assertNotEquals(expected, fooClient.getIDUserName());
			assertEquals(client.getIDUserName(), fooClient.getIDUserName());
			assertNotEquals(client.getEffectiveUserName(), fooClient.getEffectiveUserName());
			withTempDb(fooClient, (db) -> {
				Optional<UserNamesList> namesList = db.getNamesList();
				assertTrue(namesList.isPresent());
				assertEquals(expected, namesList.get().getPrimaryName());
			});
		}

	}
	
	@Test
	public void testThreadFactory() throws InterruptedException {
		DominoClient client = getClient();
		ExecutorService executor = Executors.newCachedThreadPool(client.getThreadFactory());
		try {
			int result = IntStream.range(0, 5)
				.mapToObj(i -> (Callable<Integer>)() -> {
					client.openDatabase("names.nsf");
					return i;
				})
				.map(executor::submit)
				.mapToInt(future -> {
					try {
						return future.get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				})
				.sum();
			// This is particularly perfunctory since the process will crash if the thread factory doesn't work
			assertEquals(4+3+2+1+0, result);
		} finally {
			executor.shutdown();
			executor.awaitTermination(30, TimeUnit.SECONDS);
		}
	}
	
	@Test
	public void testRunAsync() {
		DominoClient client = getClient();
		int result = IntStream.range(0, 5)
			.mapToObj(i -> (Callable<Integer>)() -> {
				client.openDatabase("names.nsf");
				return i;
			})
			.map(client::runAsync)
			.mapToInt(future -> {
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			})
			.sum();
		// This is particularly perfunctory since the process will crash if the thread factory doesn't work
		assertEquals(4+3+2+1+0, result);
	}
	
	@Test
	public void testLifecycleListenerClose() {
		boolean[] flag = new boolean[1];
		try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
			client.addLifecycleListener(new LifecycleListener() {
				@Override
				public void onClose(DominoClient client) {
					flag[0] = true;
				}
			});
		}
		assertTrue(flag[0], "Listener#onClose should have been called");
	}
	
	@Test
	@EnabledIfEnvironmentVariable(named = PING_SERVER, matches = ".+")
	public void testPingServer() {
		String pingServer = System.getenv(PING_SERVER);
		DominoClient client = getClient();
		
		{
			ServerPingInfo info = client.pingServer(pingServer, false, false);
			assertNotNull(info);
			assertFalse(info.getAvailabilityIndex().isPresent());
			assertFalse(info.getClusterName().isPresent());
			assertFalse(info.getClusterMembers().isPresent());
		}
		{
			ServerPingInfo info = client.pingServer(pingServer, true, false);
			assertNotNull(info);
			assertTrue(info.getAvailabilityIndex().isPresent());
			assertFalse(info.getClusterName().isPresent());
			assertFalse(info.getClusterMembers().isPresent());
		}
		{
			ServerPingInfo info = client.pingServer(pingServer, false, true);
			assertNotNull(info);
			assertFalse(info.getAvailabilityIndex().isPresent());
			assertTrue(info.getClusterName().isPresent());
			assertTrue(info.getClusterMembers().isPresent());
		}
		{
			ServerPingInfo info = client.pingServer(pingServer, true, true);
			assertNotNull(info);
			assertTrue(info.getAvailabilityIndex().isPresent());
			assertTrue(info.getClusterName().isPresent());
			assertTrue(info.getClusterMembers().isPresent());
		}
		{
			assertThrows(ServerNotFoundException.class, () -> client.pingServer("dfsffsfdfd", false, false));
		}
	}
	
	@Test
	public void testIsOnServer() {
		boolean expected = DominoUtils.checkBooleanProperty("JNX_ON_SERVER", "jnx.onserver");
		assertEquals(expected, getClient().isOnServer());
	}
}
