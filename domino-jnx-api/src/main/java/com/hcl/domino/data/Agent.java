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
package com.hcl.domino.data;

import java.io.Writer;
import java.util.List;
import java.util.Optional;

/**
 * Details about the Agent
 *
 * @author t.b.d.
 */
public interface Agent {

  public interface AgentRunContext {

    /**
     * Returns the Document for Session.DocumentContext as a {@link Document}
     *
     * @return document context
     */
    Optional<Document> getDocumentContext();

    /**
     * Returns the output writer used for Print statements during agent execution
     *
     * @return an {@link Optional} describing the target {@link Writer}, or an empty
     *         one
     *         if no writer has been set
     */
    Optional<Writer> getOutputWriter();

    /**
     * Returns the note id for Session.ParameterDocId
     *
     * @return note id
     */
    int getParamDocId();

    /**
     * Returns the agent timeout
     *
     * @return timeout
     */
    int getTimeoutSeconds();

    /**
     * Returns the Notes username e.g. to be used for evaluating @UserNamesList
     *
     * @return username
     */
    String getUsername();

    /**
     * Returns whether security should be checked
     *
     * @return true to check
     */
    boolean isCheckSecurity();

    /**
     * Returns whether the DB should be reopened as agent signer
     *
     * @return true to reopen
     */
    boolean isReopenDbAsSigner();

    /**
     * Use this method to set the AGENT_SECURITY_ON flag:<br>
     * <br>
     * AGENT_SECURITY_ON:<br>
     * Use this flag to tell the run context that when it runs an agent, you want it
     * to check the privileges of the signer of that agent and apply them. For
     * example, if the signer of the agent has "restricted" agent privileges, then
     * the agent will be restricted. If you don't set this flag, all agents run as
     * unrestricted.<br>
     * <ul>
     * <li>List of security checks enabled by this flag:</li>
     * <li>Restricted/unrestricted agent</li>
     * <li>Can create databases</li>
     * <li>Is agent targeted to this machine</li>
     * <li>Is user allowed to access this machine</li>
     * <li>Can user run personal agents</li>
     * </ul>
     *
     * @param checkSecurity true to check security, true by default
     * @return this context object (for chained calls)
     */
    AgentRunContext setCheckSecurity(boolean checkSecurity);

    /**
     * Sets the Document for Session.DocumentContext as a {@link Document}.<br>
     * <br>
     *
     * @param doc document context, can be in-memory only (not
     *            saved yet)
     * @return this context object (for chained calls)
     */
    AgentRunContext setDocumentContext(Document doc);

    /**
     * If this method is used to set an output writer, we will collect the agent
     * output produced during execution (e.g. via Print statements in LotusScript)
     * and write it to the specified writer <b>when the agent execution is done</b>.
     *
     * @param writer output writer, null by default
     * @return this context object (for chained calls)
     */
    AgentRunContext setOutputWriter(Writer writer);

    /**
     * Sets the note id for Session.ParameterDocId
     *
     * @param paramDocId note id, 0 by default
     * @return this context object (for chained calls)
     */
    AgentRunContext setParamDocId(int paramDocId);

    /**
     * Use this method to set the AGENT_REOPEN_DB flag:<br>
     * <br>
     * AGENT_REOPEN_DB:<br>
     * If AGENT_REOPEN_DB is set, the AgentRun call will reopen the agent's database
     * with the privileges of the signer of the agent. If the flag is not set, the
     * agent's "context" database will be open with the privileges of the current
     * user (the current Notes id or the current Domino web user).
     *
     * @param reopenAsSigner true to reopen database, false by default
     * @return this context object (for chained calls)
     */
    AgentRunContext setReopenDbAsSigner(boolean reopenAsSigner);

    /**
     * Sets an execution timeout in seconds
     *
     * @param timeoutSeconds timeout in seconds, 0 by default
     * @return this context object (for chained calls)
     */
    AgentRunContext setTimeoutSeconds(int timeoutSeconds);

    /**
     * Sets the Notes username e.g. to be used for evaluating @UserNamesList.
     * Unfortunately, this does not cover Session.EffectiveUserName. We still need
     * to find a way to change this (when calling WebQueryOpen/Save agents
     * manually), if there is any.
     *
     * @param sessionEffectiveName either in canonical or abbreviated format, null
     *                             by default, which means Session.EffectiveUserName
     *                             will be the agent signer
     * @return this context object (for chained calls)
     */
    AgentRunContext setUsername(String sessionEffectiveName);

  }

  /**
   * Initializes the AgentRunContext in which the agent should run, for defining
   * settings like re-opening the database as the signer, which document the agent
   * should run on, timeouts etc.
   *
   * @return AGentRunContext, ready to receive setters
   */
  AgentRunContext createAgentContext();

  /**
   * The returned document is created when you save an agent, and it is stored in
   * the same database as the agent.<br>
   * The document replicates, but is not displayed in views.<br>
   * Each time you edit and re-save an agent, its saved data document is deleted
   * and a new, blank one is created. When you delete an agent, its saved data
   * document is deleted.
   *
   * @return an {@link Optional} describing the saved agent data, or an empty one
   *         if there is no saved data
   */
  Optional<Document> getAgentSavedData();

  /**
   * Gets any aliases for the agent. If there are no aliases, an empty List is
   * returned.
   *
   * @return list of aliases
   */
  List<String> getAliases();

  /**
   * Gets any comment the developer has assigned for the agent
   *
   * @return comment, not null
   */
  String getComment();

  /**
   * Gets the agent's Name
   *
   * @return agent's name, not null
   */
  String getName();

  /**
   * NoteID location of the agent's design element, specific only for this replica
   * of the Database
   *
   * @return int corresponding to the String note ID of format 000020FA
   */
  int getNoteID();

  /**
   * Database containing the agent
   *
   * @return parent database in which the agent resides
   */
  Database getParentDatabase();

  /**
   * Domino UNID of the agent's design element
   *
   * @return 32 character hex string to retrieve the agent's design element
   */
  String getUNID();

  /**
   * Gets whether or not the agent is set to be run on a schedule and is enabled
   *
   * @return true if the agent is scheduled and enabled
   */
  boolean isEnabled();

  /**
   * Gets whether or not the agent should run as the web user instead of the
   * signer
   *
   * @return true if the agent should run as the web user
   */
  boolean isRunAsWebUser();

  /**
   * Runs the agent with the specific agent context
   *
   * @param runCtx AgentRunContext that defines run settings
   */
  void run(AgentRunContext runCtx);

  /**
   * Runs the agent on the server
   *
   * @param suppressPrintToConsole true to not write "Print" statements in the
   *                               agent code to the server console
   */
  void runOnServer(boolean suppressPrintToConsole);

  /**
   * Runs the agent on the server
   *
   * @param noteIdParamDoc         note id of parameter document
   * @param suppressPrintToConsole true to not write "Print" statements in the
   *                               agent code to the server console
   */
  void runOnServer(int noteIdParamDoc, boolean suppressPrintToConsole);
}
