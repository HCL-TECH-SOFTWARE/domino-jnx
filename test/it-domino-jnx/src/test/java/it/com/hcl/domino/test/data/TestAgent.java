/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.hcl.domino.data.Agent;
import com.hcl.domino.data.Agent.AgentRunContext;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignAgent.SecurityLevel;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.exception.AgentTimeoutException;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestAgent extends AbstractNotesRuntimeTest {
  public static final String ENV_SERVERAGENTDB = "ServerAgentDB"; //$NON-NLS-1$
  public static final String ENV_SERVERAGENGNAME = "ServerAgentName"; //$NON-NLS-1$

  @Test
  @EnabledIfEnvironmentVariable(named = TestAgent.ENV_SERVERAGENTDB, matches = ".+")
  @EnabledIfEnvironmentVariable(named = TestAgent.ENV_SERVERAGENGNAME, matches = ".+")
  public void testRunOnServer() {
    final String serverAgentDb = System.getenv(TestAgent.ENV_SERVERAGENTDB);
    final String serverAgentName = System.getenv(TestAgent.ENV_SERVERAGENGNAME);

    final Database database = this.getClient().openDatabase(serverAgentDb);
    final Agent agent = database.getAgent(serverAgentName).get();
    agent.runOnServer(false);
  }
  
  @Test
  public void testExecutionTimeoutOfNewAgent() throws Exception {
    withTempDb((db) -> {
      DbDesign design = db.getDesign();
      
      String agentName = "testagent"; //$NON-NLS-1$
      
      DesignLotusScriptAgent newAgent = design.createAgent(DesignLotusScriptAgent.class, agentName);
      String script = "Option Public\n"
          + "Option Declare\n"
          + "\n"
          + "Sub Initialize\n"
          + "  Dim session As New NotesSession\n"
          + "  Dim docCtx as NotesDocument\n"
          + "  Set docCtx = session.DocumentContext\n"
          + "  docCtx.EffectiveUsername = session.EffectiveUserName\n"
          + "  docCtx.AtUsername = Evaluate(|@Username|)\n"
          + "  docCtx.AtUserNamesList = Evaluate(|@UserNamesList|)\n"
          + "  docCtx.save true, false\n"
          + "  sleep 3\n"
          + "End Sub";

      newAgent.setScript(script);
      newAgent.setRunAsWebUser(true);
      newAgent.setSecurityLevel(SecurityLevel.RESTRICTED);
      
      newAgent.setTrigger(AgentTrigger.MANUAL);
      newAgent.setTarget(AgentTarget.UI);
      
      newAgent.sign();
      newAgent.save();

      Optional<Agent> optRunnableAgent = db.getAgent(agentName);
      Assertions.assertTrue(optRunnableAgent.isPresent());
      Agent runnableAgent = optRunnableAgent.get();
      
      Document tmpDoc = db.createDocument();
      
      AgentRunContext runCtx = runnableAgent.createAgentContext()
          .setDocumentContext(tmpDoc)
          .setTimeoutSeconds(1);
      
      Assertions.assertThrows(AgentTimeoutException.class, () -> {
        runnableAgent.run(runCtx);
      });

    });
  }
  
}
