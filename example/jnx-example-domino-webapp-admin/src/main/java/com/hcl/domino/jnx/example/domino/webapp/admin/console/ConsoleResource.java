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
package com.hcl.domino.jnx.example.domino.webapp.admin.console;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.admin.IConsoleLine;
import com.hcl.domino.admin.ServerAdmin.ConsoleHandler;
import com.hcl.domino.exception.CancelException;
import com.hcl.domino.jnx.example.domino.webapp.admin.RestEasyServlet;
import com.hcl.domino.mq.MessageQueue;

import jakarta.json.bind.JsonbBuilder;

@Path("console")
public class ConsoleResource {
  private static class SseConsoleHandler implements ConsoleHandler, AutoCloseable {
    private final Sse sse;
    private final SseBroadcaster sseBroadcaster;
    private final long opened = System.currentTimeMillis();
    private boolean shouldStop;
    private final DominoClient dominoClient;
    private final MessageQueue httpQueue;

    public SseConsoleHandler(final Sse sse, final SseBroadcaster sseBroadcaster) {
      this.sse = sse;
      this.sseBroadcaster = sseBroadcaster;
      this.dominoClient = DominoClientBuilder.newDominoClient().build();
      this.httpQueue = this.dominoClient.getMessageQueues().open("MQ$HTTP", false); //$NON-NLS-1$

      this.sseBroadcaster.onClose(sink -> this.close());
    }

    @Override
    public void close() {
      this.shouldStop = true;
      this.httpQueue.close();
      this.dominoClient.close();
    }

    @Override
    public void messageReceived(final IConsoleLine line) {
      final UUID eventId = UUID.randomUUID();

      final String json = JsonbBuilder.create().toJson(line);

      this.sseBroadcaster.broadcast(this.sse.newEventBuilder()
          .name("logline") //$NON-NLS-1$
          .id(eventId.toString())
          .mediaType(MediaType.APPLICATION_JSON_TYPE)
          .data(String.class, json)
          .reconnectDelay(250)
          .build());
    }

    @Override
    public boolean shouldStop() {
      if (this.httpQueue.isQuitPending() || this.shouldStop || Thread.currentThread().isInterrupted()) {
        return true;
      }
      final boolean timedOut = System.currentTimeMillis() > this.opened + TimeUnit.HOURS.toMillis(1);
      if (timedOut) {
        return true;
      }
      return false;
    }

  }

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public void getConsoleOutput(@Context final SseEventSink sseEventSink, @Context final Sse sse,
      @QueryParam("serverName") final String serverName) throws InterruptedException, ExecutionException {
    RestEasyServlet.instance.executor.submit(() -> {
      try (SseBroadcaster broadcaster = sse.newBroadcaster(); SseConsoleHandler handler = new SseConsoleHandler(sse, broadcaster)) {
        broadcaster.register(sseEventSink);
        try (DominoClient dominoClient = DominoClientBuilder.newDominoClient().build()) {
          dominoClient.getServerAdmin().openServerConsole(serverName, handler);
        } catch (final CancelException e) {
          // Expected when shutting down
        } catch (final Throwable t) {
          t.printStackTrace();
        } finally {
          sseEventSink.close();
        }
      }
    }).get();
  }

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public String sendCommand(@FormParam("serverName") final String serverName, @FormParam("command") final String command)
      throws InterruptedException, ExecutionException {
    return RestEasyServlet.instance.executor
        .submit(() -> RestEasyServlet.instance.dominoClient.getServerAdmin().sendConsoleCommand(serverName, command)).get();
  }
}
