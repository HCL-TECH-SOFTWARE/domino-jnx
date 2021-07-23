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
		private DominoClient dominoClient;
		private MessageQueue httpQueue;
		
		public SseConsoleHandler(Sse sse, SseBroadcaster sseBroadcaster) {
			this.sse = sse;
			this.sseBroadcaster = sseBroadcaster;
			this.dominoClient = DominoClientBuilder.newDominoClient().build();
			this.httpQueue = dominoClient.getMessageQueues().open("MQ$HTTP", false); //$NON-NLS-1$
			
			this.sseBroadcaster.onClose(sink -> this.close());
		}
		
		@Override
		public void close() {
			this.shouldStop = true;
			this.httpQueue.close();
			this.dominoClient.close();
		}

		@Override
		public boolean shouldStop() {
			if(httpQueue.isQuitPending()) {
				return true;
			}
			if(this.shouldStop || Thread.currentThread().isInterrupted()) {
				return true;
			}
			boolean timedOut = System.currentTimeMillis() > opened + TimeUnit.HOURS.toMillis(1);
			if(timedOut) {
				return true;
			}
			return false;
		}

		@Override
		public void messageReceived(IConsoleLine line) {
			UUID eventId = UUID.randomUUID();
			
			String json = JsonbBuilder.create().toJson(line);
			
			this.sseBroadcaster.broadcast(sse.newEventBuilder()
				.name("logline") //$NON-NLS-1$
				.id(eventId.toString())
				.mediaType(MediaType.APPLICATION_JSON_TYPE)
				.data(String.class, json)
				.reconnectDelay(250)
				.build());
		}
		
	}

	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void getConsoleOutput(@Context SseEventSink sseEventSink, @Context Sse sse, @QueryParam("serverName") String serverName) throws InterruptedException, ExecutionException {
		RestEasyServlet.instance.executor.submit(() -> {
			try(SseBroadcaster broadcaster = sse.newBroadcaster(); SseConsoleHandler handler = new SseConsoleHandler(sse, broadcaster)) {
				broadcaster.register(sseEventSink);
				try(DominoClient dominoClient = DominoClientBuilder.newDominoClient().build()) {
					dominoClient.getServerAdmin().openServerConsole(serverName, handler);
				} catch(CancelException e) {
					// Expected when shutting down
				} catch(Throwable t) {
					t.printStackTrace();
				} finally {
					sseEventSink.close();
				}
			}
		}).get();
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String sendCommand(@FormParam("serverName") String serverName, @FormParam("command") String command) throws InterruptedException, ExecutionException {
		return RestEasyServlet.instance.executor.submit(() ->
			RestEasyServlet.instance.dominoClient.getServerAdmin().sendConsoleCommand(serverName, command)
		).get();
	}
}
