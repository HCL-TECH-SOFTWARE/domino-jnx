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
package com.hcl.domino.design;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.DesignElement.NamedDesignElement;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTarget;
import com.hcl.domino.design.agent.AgentTrigger;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;

/**
 * Access to a database design. Search for design, database as constructor
 * parameter
 */
public interface DesignAgent extends NamedDesignElement, DesignElement.ReadersRestrictedElement {
  /**
   * Represents last-run information for agent notes.
   * 
   * @author Jesse Gallagher
   * @since 1.0.38
   */
  interface LastRunInfo {
    /**
     * Retrieves the time the agent was last run.
     * 
     * @return a {@link DominoDateTime} representing the last-run time
     */
    DominoDateTime getTime();
    
    /**
     * Determines the number of documents processed when the agent was last
     * run.
     * 
     * @return the count of processed documents
     */
    long getDocumentCount();
    
    /**
     * Determines the version of the agent when it was last run.
     * 
     * <p>This corresponds to the version value represented by
     * {@link DesignAgent#getAgentVersion()}.</p>
     * 
     * @return a {@link DominoDateTime} modification version for the agent
     *         when it was last run
     */
    DominoDateTime getVersion();
    
    /**
     * Determines the specific instance ID (as opposed to the replica ID) of the
     * database where the agent was last run, represented as a pair of int values.
     * 
     * @return a two-element {@code int} array representing the database ID
     */
    int[] getDbId();
    
    /**
     * Determines the exit code from the agent's last run.
     * 
     * @return a {@code long} representing the last exit code
     */
    long getExitCode();
    
    /**
     * Retrieves the log from the last run.
     * 
     * @return a {@link String} of the agent log, which may be empty
     */
    String getLog();
  }
  
  enum SecurityLevel {
    RESTRICTED,
    UNRESTRICTED,
    UNRESTRICTED_FULLADMIN
  }
  
  /**
   * Determines the effective version of the agent design, as opposed to just when
   * the note was last modified.
   * 
   * <p>This version is expressed as the time the agent design was last modified.</p>
   * 
   * @return a {@link DominoDateTime} instance representing the time that the agent's
   *         design was last changed
   * @since 1.0.38
   */
  DominoDateTime getAgentVersion();

  /**
   * Gets the last date that the agent is able to run, if applicable
   *
   * @return a {@link Optional} representing the end date as a
   *         {@link DominoDateTime}, or an empty
   *         one if this is not applicable
   */
  Optional<DominoDateTime> getEndDate();

  /**
   * Retrieves the interval value for the agent. The meaning of this number
   * depends on the value of
   * {@link #getIntervalType()}:
   * <dl>
   * <dt>{@link AgentInterval#NONE NONE}</dt>
   * <dd>An empty value</dd>
   * <dt>{@link AgentInterval#MINUTES MINUTES}</dt>
   * <dd>The interval between invocations in minutes</dd>
   * <dt>{@link AgentInterval#DAYS DAYS}</dt>
   * <dd>The interval between invocations in days*</dd>
   * <dt>{@link AgentInterval#WEEK WEEK}</dt>
   * <dd>The interval between invocations in weeks*</dd>
   * <dt>{@link AgentInterval#MONTH MONTH}</dt>
   * <dd>The interval between invocations in months*</dd>
   * <dt>{@link AgentInterval#EVENT}</dt>
   * <dd>An empty value</dd>
   * </dl>
   * <p>
   * Note: other than for {@link AgentInterval#MINUTES MINUTES}, the interval
   * value is not expressible
   * in Domino Designer and may not affect the behavior of the agent on the
   * server.
   * </p>
   *
   * @return an {@link OptionalInt} describing the interval, or an empty one if it
   *         is not applicable
   */
  OptionalInt getInterval();

  /**
   * Interval type for the agent
   *
   * @return the agent's interval type, or {@code AgentInterval#NONE} if not
   *         applicable
   */
  AgentInterval getIntervalType();
  
  /**
   * Retrieves information about the agent's last execution, if that information
   * exists.
   * 
   * @return an {@link Optional} describing the agent's last-run information,
   *         or an empty one if that data is not stored
   * @since 1.0.38
   */
  Optional<LastRunInfo> getLastRunInfo();

  /**
   * Retrieves the 1-based day of the month that the agent should run. This only
   * applies when the interval
   * type is {@link AgentInterval#MONTH MONTH}.
   *
   * @return an {@link OptionalInt} describing the day of the month to execute, or
   *         an empty one if
   *         the agent interval is not {@link AgentInterval#MONTH MONTH}
   */
  OptionalInt getRunDayOfMonth();

  /**
   * Retrieves the day of the week that the agent should run. This only applies
   * when the interval type
   * is {@link AgentInterval#WEEK WEEK}.
   *
   * @return an {@link Optional} describing the day of the week to execute, or an
   *         empty one if
   *         the agent interval is not {@link AgentInterval#WEEK WEEK}
   */
  Optional<DayOfWeek> getRunDayOfWeek();

  /**
   * Retrieves the local time of day when the agent should no longer be executed.
   * This only applies when the interval type is
   * {@link AgentInterval#MINUTES MINUTES}.
   * 
   * <p>Note: this value will also be empty if the agent is scheduled to run all day,
   * while {@link #getRunLocalTime()} will reflect midnight in this case.</p>
   *
   * @return an {@link Optional} describing the local end time of execution, or an
   *         empty one if the agent interval is not
   *         {@link AgentInterval#MINUTES MINUTES} or if the agent is scheduled to
   *         run all day
   */
  Optional<LocalTime> getRunEndLocalTime();

  /**
   * Retrieves the local time of day when the agent should be run or start
   * execution, if the
   * interval type is not {@link AgentInterval#NONE NONE} or
   * {@link AgentInterval#EVENT EVENT}.
   *
   * @return an {@link Optional} describing the local time to run, or an empty one
   *         if this value does not apply
   */
  Optional<LocalTime> getRunLocalTime();

  /**
   * Gets the server to run on. This value will be one of:
   * <ul>
   * <li>A server name when one is specified</li>
   * <li>A user name when set to "Local"</li>
   * <li>{@code "*"} when set to run on any server</li>
   * <li>{@code ""} when set to prompt for a server name when enabled</li>
   * </ul>
   *
   * @return configured server run location
   */
  String getRunLocation();

  /**
   * Gets the earliest date that the agent is able to run, if applicable
   *
   * @return a {@link Optional} representing the start date as a
   *         {@link DominoDateTime}, or an empty
   *         one if this is not applicable
   */
  Optional<DominoDateTime> getStartDate();

  /**
   * @return the trigger type for the agent, or {@code AgentTrigger#NONE} if the
   *         agent
   *         is unavailable
   */
  AgentTrigger getTrigger();
  
  /**
   * Whether or not the agent runs on weekends, only relevant for DAILY or
   * MORE_THAN_DAILY
   *
   * @return whether it runs on weekends. WEEKLY or MONTHLY returns true
   */
  boolean isRunOnWeekends();
  
  /**
   * Determines whether the agent should be run as the web user when accessed via
   * URL.
   * 
   * @return {@code true} if the agent should run as the current web user when
   *         accessed via URL; {@code false} to run as the signer or the on-behalf-
   *         of user
   * @since 1.0.35
   */
  boolean isRunAsWebUser();
  
  /**
   * Sets whether the agent should be run as the web user when accessed via
   * URL.
   * 
   * @param b {@code true} if the agent should run as the current web user when
   *         accessed via URL; {@code false} to run as the signer or the on-behalf-
   *         of user
   * @return this agent
   * @since 1.0.43
   */
  DesignAgent setRunAsWebUser(boolean b);
  
  /**
   * Retrieves the name of the user to run the agent on behalf of, if set.
   * 
   * @return an {@link Optional} describing the on-behalf-of user, or an empty one
   *         if this is unset
   * @since 1.0.35
   */
  Optional<String> getOnBehalfOfUser();
  
  /**
   * Sets the name of the user to run the agent on behalf of, if set.
   * 
   * @param user the on-behalf-of user or null/empty string to unset
   * @since 1.0.47
   */
  void setOnBehalfOfUser(String user);
  
  /**
   * Determines the security level to enforce when running this agent.
   * 
   * @return a {@link SecurityLevel} instance
   * @since 1.0.35
   */
  SecurityLevel getSecurityLevel();
  
  /**
   * Changes the security level to enforce when running this agent.
   * 
   * @param level new level
   * @return this agent
   * @since 1.0.43
   */
  DesignAgent setSecurityLevel(SecurityLevel level);
  
  /**
   * Determines whether the agent should store highlights when run.
   * 
   * @return {@code true} if the agent should store highlights;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isStoreHighlights();
  
  /**
   * Determines whether the agent search should be stored in the search bar menu.
   * 
   * @return {@code true} if the search should be stored;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isStoreSearch();
  
  /**
   * Sets whether the agent search should be stored in the search bar menu.
   * 
   * @param b true to store
   * @since 1.0.47
   */
  void setStoreSearch(boolean b);
  
  /**
   * Determines whether profiling should be enabled for this agent.
   * 
   * @return {@code true} if profiling is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isProfilingEnabled();
  
  /**
   * Sets whether profiling should be enabled for this agent.
   * 
   * @param b true to enable
   * @since 1.0.47
   */
  void setProfilingEnabled(boolean b);
  
  /**
   * Determines whether remote debugging should be enabled for this agent.
   * 
   * @return {@code true} if remote debugging is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isAllowRemoteDebugging();
  
  /**
   * Sets whether remote debugging should be enabled for this agent.
   * 
   * @param b true to enable
   * @since 1.0.47
   */
  void setAllowRemoteDebugging(boolean b);
  
  /**
   * Determines whether the agent is private to the user or not.
   * 
   * @return {@code true} if the agent is private;
   *         {@code false} if it is normally accessible
   * @since 1.0.35
   */
  boolean isPrivate();
  
  /**
   * Sets whether the agent is private to the user or not.
   * 
   * @param b true if private
   * @since 1.0.47
   */
  void setPrivate(boolean b);
  
  /**
   * Determines whether the agent should run in a background thread when invoked
   * from the client.
   * 
   * @return {@code true} to run in a background thread;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isRunInBackgroundInClient();
  
  /**
   * Sets whether the agent should run in a background thread when invoked
   * from the client.
   * 
   * @param b true to run in background
   * @since 1.0.47
   */
  void setRunInBackgroundInClient(boolean b);
  
  /**
   * Determines whether the agent is enabled to run according to its schedule or trigger.
   * 
   * @return {@code true} if the agent is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isEnabled();
  
  /**
   * Enables/disables the agent to run according to its schedule or trigger.
   * 
   * @param b true if enabled
   * @return this agent
   * @since 1.0.43
   */
  DesignAgent setEnabled(boolean b);
  
  /**
   * Retrieves the search terms used to select documents for agent processing, if provided.
   * 
   * @return a {@link List} containing {@link SimpleSearchTerm} subclass instances, or an
   *         empty list if no search criteria are provided
   * @since 1.0.38
   */
  List<? extends SimpleSearchTerm> getDocumentSelection();
  
  /**
   * Retrieves the broad document selection target for the agent.
   * 
   * @return a {@link AgentTarget} instance
   * @since 1.0.42
   */
  AgentTarget getTarget();

  /**
   * Changes the broad document selection target for the agent.
   * 
   * @param target a {@link AgentTarget} instance
   * @return this agent
   * @since 1.0.43
   */
  DesignAgent setTarget(AgentTarget target);
  
  DesignAgent setTrigger(AgentTrigger trigger);
  
}