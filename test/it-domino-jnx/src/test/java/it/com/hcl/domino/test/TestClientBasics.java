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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.function.Executable;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.util.DominoUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.ModificationTimePair;
import com.hcl.domino.exception.ServerNotFoundException;
import com.hcl.domino.server.ServerPingInfo;
import com.ibm.commons.util.StringUtil;

@SuppressWarnings("nls")
public class TestClientBasics extends AbstractNotesRuntimeTest {
  public static final String PING_SERVER = "PING_SERVER";

  @Test
  public void testArbitraryUsername() throws Exception {
    final DominoClient client = this.getClient();

    final String expected = "CN=Foo Bar/O=Baz";
    try (DominoClient fooClient = DominoClientBuilder.newDominoClient()
        .asUser(expected)
        .build()) {

      Assertions.assertEquals(expected, fooClient.getEffectiveUserName());
      Assertions.assertNotEquals(expected, fooClient.getIDUserName());
      Assertions.assertEquals(client.getIDUserName(), fooClient.getIDUserName());
      Assertions.assertNotEquals(client.getEffectiveUserName(), fooClient.getEffectiveUserName());
      this.withTempDb(fooClient, db -> {
        final Optional<UserNamesList> namesList = db.getNamesList();
        Assertions.assertTrue(namesList.isPresent());
        Assertions.assertEquals(expected, namesList.get().getPrimaryName());
      });
    }

  }

  @Test
  public void testBaseUsernameEquality() {
    final DominoClient client = this.getClient();
    Assertions.assertEquals(client.getIDUserName(), client.getEffectiveUserName());
  }

  @Test
  public void testIsOnServer() {
    final boolean expected = DominoUtils.checkBooleanProperty("JNX_ON_SERVER", "jnx.onserver");
    Assertions.assertEquals(expected, this.getClient().isOnServer());
  }

  @Test
  public void testJNADominoClientBuilder() throws IOException {
    try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      Assertions.assertNotNull(client);
    }
  }

  @Test
  public void testLifecycleListenerClose() {
    final boolean[] flag = new boolean[1];
    try (DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      client.addLifecycleListener(client1 -> flag[0] = true);
    }
    Assertions.assertTrue(flag[0], "Listener#onClose should have been called");
  }

  @Test
  public void testNonEmptyUsername() {
    Assertions.assertTrue(StringUtil.isNotEmpty(this.getClient().getIDUserName()));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = TestClientBasics.PING_SERVER, matches = ".+")
  public void testPingServer() {
    final String pingServer = System.getenv(TestClientBasics.PING_SERVER);
    final DominoClient client = this.getClient();

    {
      final ServerPingInfo info = client.pingServer(pingServer, false, false);
      Assertions.assertNotNull(info);
      Assertions.assertFalse(info.getAvailabilityIndex().isPresent());
      Assertions.assertFalse(info.getClusterName().isPresent());
      Assertions.assertFalse(info.getClusterMembers().isPresent());
    }
    {
      final ServerPingInfo info = client.pingServer(pingServer, true, false);
      Assertions.assertNotNull(info);
      Assertions.assertTrue(info.getAvailabilityIndex().isPresent());
      Assertions.assertFalse(info.getClusterName().isPresent());
      Assertions.assertFalse(info.getClusterMembers().isPresent());
    }
    {
      final ServerPingInfo info = client.pingServer(pingServer, false, true);
      Assertions.assertNotNull(info);
      Assertions.assertFalse(info.getAvailabilityIndex().isPresent());
      Assertions.assertTrue(info.getClusterName().isPresent());
      Assertions.assertTrue(info.getClusterMembers().isPresent());
    }
    {
      final ServerPingInfo info = client.pingServer(pingServer, true, true);
      Assertions.assertNotNull(info);
      Assertions.assertTrue(info.getAvailabilityIndex().isPresent());
      Assertions.assertTrue(info.getClusterName().isPresent());
      Assertions.assertTrue(info.getClusterMembers().isPresent());
    }
    {
      Assertions.assertThrows(ServerNotFoundException.class, () -> client.pingServer("dfsffsfdfd", false, false));
    }
  }

  @Test
  public void testRunAsync() {
    final DominoClient client = this.getClient();
    final int result = IntStream.range(0, 5)
        .mapToObj(i -> (Callable<Integer>) () -> {
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
    // This is particularly perfunctory since the process will crash if the thread
    // factory doesn't work
    Assertions.assertEquals(4 + 3 + 2 + 1 + 0, result);
  }

  @Test
  public void testThreadFactory() throws InterruptedException {
    final DominoClient client = this.getClient();
    final ExecutorService executor = Executors.newCachedThreadPool(client.getThreadFactory());
    try {
      final int result = IntStream.range(0, 5)
          .mapToObj(i -> (Callable<Integer>) () -> {
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
      // This is particularly perfunctory since the process will crash if the thread
      // factory doesn't work
      Assertions.assertEquals(4 + 3 + 2 + 1 + 0, result);
    } finally {
      executor.shutdown();
      executor.awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testUseClosedClient() throws IOException {
    final DominoClient client = DominoClientBuilder.newDominoClient().build();
    client.close();

    Assertions.assertThrows(DominoException.class, (Executable) () -> {
      client.openDatabase("names.nsf");
    }, "Did not throw exception when using closed DominoClient");
  }

  @Test
  public void testUseClosedClientsObjectMethods() throws IOException {
    final DominoClient client = DominoClientBuilder.newDominoClient().build();
    final Database db = client.openDatabase("names.nsf");
    client.close();

    Assertions.assertThrows(DominoException.class, (Executable) () -> db.getOptions(), "Did not throw exception when using database methods of closed DominoClient");
  }

  @Test
  public void testUseClosedClientsObjects() throws IOException {
    final DominoClient client = DominoClientBuilder.newDominoClient().build();
    final Database db = client.openDatabase("names.nsf");
    client.close();

    Assertions.assertThrows(DominoException.class, (Executable) () -> db.getACL(), "Did not throw exception when using database of closed DominoClient");
  }
  
  @Test
  public void testDbModificationTimes() throws IOException {
    try(DominoClient client = DominoClientBuilder.newDominoClient().build()) {
      ModificationTimePair pair = client.getDatabaseModificationTimes("names.nsf");
      assertNotNull(pair);
      assertNotNull(pair.getDataModified());
      assertTrue(pair.getDataModified().isValid());
      assertNotNull(pair.getNonDataModified());
      assertTrue(pair.getNonDataModified().isValid());
    }
  }
}
