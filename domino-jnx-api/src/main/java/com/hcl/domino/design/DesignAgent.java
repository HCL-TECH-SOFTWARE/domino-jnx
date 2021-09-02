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
package com.hcl.domino.design;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.design.DesignElement.NamedDesignElement;
import com.hcl.domino.design.agent.AgentContent;
import com.hcl.domino.design.agent.AgentInterval;
import com.hcl.domino.design.agent.AgentTrigger;

/**
 * Access to a database design. Search for design, database as constructor
 * parameter
 *
 * @author t.b.d
 */
public interface DesignAgent extends NamedDesignElement, DesignElement.ReadersRestrictedElement {
  enum AgentLanguage {
    LS, FORMULA, JAVA, IMPORTED_JAVA, SIMPLE_ACTION
  }
  enum SecurityLevel {
    RESTRICTED,
    UNRESTRICTED,
    UNRESTRICTED_FULLADMIN
  }

  /**
   * Retrieves the action content of the agent. The actual type of object depends
   * on the
   * {@link AgentLanguage language} of the agent.
   *
   * @return an {@link AgentContent} instance representing the content of the
   *         agent
   */
  AgentContent getAgentContent();

  /**
   * Language the agent is written using
   *
   * @return an {@link AgentLanguage} instance
   */
  AgentLanguage getAgentLanguage();

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
   * Analyses the last run log to work out the number of seconds for last run
   * duration
   *
   * @return an {@link OptionalLong} describing the number of seconds between
   *         start and end
   *         of last run time, or an empty one if it has no run record
   */
  OptionalLong getLastRunDuration();

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
   * This only applies
   * when the interval type is {@link AgentInterval#MINUTES MINUTES}.
   *
   * @return an {@link Optional} describing the local end time of execution, or an
   *         empty one if
   *         the agent interval is not {@link AgentInterval#MINUTES MINUTES}
   */
  Optional<LocalTime> getRunEndLocalTime();

  /**
   * Retrieves the local time of day when the agent should be run or start
   * execution, if the
   * interval type is not {@link AgentInterval#NONE NONE} or
   * {@link AgentInterval#EVENT EVENT}.
   *
   * @return an {@link Optional} describing the local time to run, or an empty one
   *         if
   *         this value does not apply
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
   * Gets the last run log for the agent. If the agent has not run since last log,
   * it is a blank String.
   *
   * @return String containing run log
   */
  String getRunLog();

  /**
   * Splits the run log into a String for each line
   *
   * @return List of run log messages
   */
  List<String> getRunLogAsList();

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
   * Language the agent should parse as
   *
   * @param lang the agent language
   * @return this {@code Agent}
   */
  DesignAgent initializeAgentLanguage(AgentLanguage lang);

  /**
   * Analyses the last run log to work out if the last run exceeded the max run
   * time for agents. Combine with
   * {@link #getLastRunDuration()} to work out what that max run time setting is.
   *
   * @return whether the last run time exceeded max run time for agents. If there
   *         is no run log, this will also be false
   */
  boolean isLastRunExceededTimeLimit();

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
   * Retrieves the name of the user to run the agent on behalf of, if set.
   * 
   * @return an {@link Optional} describing the on-behalf-of user, or an empty one
   *         if this is unset
   * @since 1.0.35
   */
  Optional<String> getOnBehalfOfUser();
  
  /**
   * Determines the security level to enforce when running this agent.
   * 
   * @return a {@link SecurityLevel} instance
   * @since 1.0.35
   */
  SecurityLevel getSecurityLevel();
  
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
   * Determines whether profiling should be enabled for this agent.
   * 
   * @return {@code true} if profiling is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isProfilingEnabled();
  
  /**
   * Determines whether remote debugging should be enabled for this agent.
   * 
   * @return {@code true} if remote debugging is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isAllowRemoteDebugging();
  
  /**
   * Determines whether the agent is private to the user or not.
   * 
   * @return {@code true} if the agent is private;
   *         {@code false} if it is normally accessible
   * @since 1.0.35
   */
  boolean isPrivate();
  
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
   * Determines whether the agent is enabled to run according to its schedule or trigger.
   * 
   * @return {@code true} if the agent is enabled;
   *         {@code false} otherwise
   * @since 1.0.35
   */
  boolean isEnabled();
}