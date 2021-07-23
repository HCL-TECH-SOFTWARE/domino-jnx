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
package com.hcl.domino.person;

import java.time.temporal.TemporalAccessor;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.misc.Ref;

/**
 * Out of Office information for a single user
 */
public interface OutOfOffice {
  public enum OOOType {
    AGENT, SERVICE
  }

  /**
   * This function returns time parameters that control OOO.
   *
   * @return away period
   */
  DominoDateRange getAwayPeriod();

  /**
   * OOO supports two sets of messages. They are called General message/subject
   * and
   * Special message/subject.<br>
   * This function returns the text of the general message.
   *
   * @return message
   */
  String getGeneralMessage();

  /**
   * OOO supports two sets of messages.<br>
   * <br>
   * They are called General message/subject and Special message/subject.<br>
   * This function gets the general subject.<br>
   * This is string that will appear as the subject line of the OOO notification.
   *
   * @return subject
   */
  String getGeneralSubject();

  Person getParentPerson();

  /**
   * This function returns the version (agent, service) and the state (disabled,
   * enabled)
   * of the out of office functionality.<br>
   * The version information can be used to show or hide UI elements that might
   * not be
   * supported for a given version.<br>
   * For example, the agent does not support durations of less than 1 day and some
   * clients might choose not to show the hours in the user interface.<br>
   * When you need to make {@link #getState(Ref, Ref)} as efficient as possible,
   * call
   * {@link Person#openOutOfOffice(String, boolean, Database)}
   * with the home mail server and the opened mail database.<br>
   * This function is read only and does not return an error if user ACL rights
   * are below Editor (which are required to turn on/off the Out of office
   * functionality).<br>
   * If {@link #getState(Ref, Ref)} is called immediately following
   * {@link #setEnabled(boolean)} it will
   * not reflect the state set by the {@link #setEnabled(boolean)}.<br>
   * To see the current state, start a new operation using
   * {@link Person#openOutOfOffice(String, boolean, Database)},
   * {@link OutOfOffice#getState(Ref, Ref)}.
   *
   * @param retType      returns the type of the OOO system (agent or service)
   * @param retIsEnabled returns whether the service is enabled for the user
   */
  void getState(Ref<OOOType> retType, Ref<Boolean> retIsEnabled);

  /**
   * Convenience method to read which kind of OOO system is used (agent or
   * service).
   * Calls {@link #getState(Ref, Ref)} internally.
   *
   * @return type
   */
  OOOType getType();

  /**
   * Convenience method to check whether the OOO functionality is enabled. Calls
   * {@link #getState(Ref, Ref)} internally.
   *
   * @return true if enabled
   */
  boolean isEnabled();

  /**
   * This function returns a flag which defines how to treat internet emails.<br>
   * This functional call is optional.<br>
   * If this flag is set to {@code true} OOO notifications will not be generated
   * for
   * email originating from the internet. The default for this flag is
   * {@code true}.
   *
   * @return true if excluded
   */
  boolean isExcludeInternet();

  /**
   * This function validates and sets the time parameters that control OOO.<br>
   * <br>
   * This information is required for enabling the OOO.<br>
   * If you want turn on OOO functionality for a given period of time the
   * sequence of calls needed is:<br>
   * {@link Person#openOutOfOffice(String, boolean, Database)},
   * {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)}, and
   * {@link #setEnabled(boolean)}.<br>
   * <br>
   * When you need to enable OOO (i.e. call it with <code>enabled</code> flag set
   * to {@code true})
   * you should call {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)}
   * prior to calling
   * {@link #setEnabled(boolean)}.<br>
   * <br>
   * If you need to change the length of the away period after OOO has already
   * been
   * enabled, the sequence of calls needed to perform this action is
   * {@link Person#openOutOfOffice(String, boolean, Database)},
   * {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)}.<br>
   * <br>
   * If the Domino server is configured to run an OOO agent, it can only be turned
   * on
   * for full days, the time portion of the date parameter will not be used.
   *
   * @param tdStartAway This is date and time when Out of office will begin.
   * @param tdEndAway   This is date and time when Out of office will end.
   */
  void setAwayPeriod(TemporalAccessor tdStartAway, TemporalAccessor tdEndAway);

  /**
   * This function changes the state of the OOO functionality as indicated by
   * the <code>enabled</code> variable.<br>
   * If the OOO functionality is already in the state indicated by the
   * <code>enabled</code> flag, this function does nothing.<br>
   * <br>
   * When you need to enable OOO (i.e. call it with <code>enabled</code> flag set
   * to {@code true}) you should call
   * {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)}
   * prior to calling {@link #setEnabled(boolean)}.<br>
   * <br>
   * If {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)} is not called,
   * {@link #setEnabled(boolean)} will use the previous value for start and
   * end.<br>
   * <br>
   * If they are in the past then the OOO functionality will not be enabled.<br>
   * When you need to disable OOO (i.e. call it with <code>enabled</code> set to
   * {@code false})
   * {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)} does not need to
   * be called.<br>
   * <br>
   * When {@link #setEnabled(boolean)} is called with the <code>enabled</code> set
   * to {@code false} it means you want to disable OOO immediately.<br>
   * If you don’t want to disable OOO functionality immediately, but rather you
   * just want to change the time when OOO should stop operating, the sequence
   * of calls is: {@link Person#openOutOfOffice(String, boolean, Database)},
   * {@link #setAwayPeriod(TemporalAccessor, TemporalAccessor)}.<br>
   * If OOO is configured to run as a service and {@link #setEnabled(boolean)} is
   * used to disable, the OOO service will be auto-disabled immediately.<br>
   * <br>
   * The summary report will be generated on the first email received after the
   * disable has been requested, or if no messages are received it will
   * generated during the nightly router maintenance.<br>
   * <br>
   * If OOO is configured as an agent, the user will receive a summary report
   * and a request to disable the agent on the next scheduled run of the agent
   * will occur.
   *
   * @param enabled true to enable
   */
  void setEnabled(boolean enabled);

  /**
   * This function sets a flag which defines how to treat internet emails.<br>
   * <br>
   * This functional call is optional.<br>
   * If this flag is set to {@code true} OOO notifications will not be generated
   * for
   * email originating from the internet.<br>
   * The default for this flag is {@code true}.
   *
   * @param exclude true to exclude
   */
  void setExcludeInternet(boolean exclude);

  /**
   * OOO supports two sets of notification messages.<br>
   * They are called General message/subject and Special message/subject.<br>
   * The following text is always appended to the body of the message, where
   * the "Message subject" is obtained from the message which caused the
   * notification to be generated.<br>
   * <i>"Note: This is an automated response to your message "Message subject"
   * sent on 2/12/2009 10:12:17 AM. This is the only notification you will receive
   * while this person is away."</i>
   *
   * @param msg message, max 65535 bytes LMBCS encoded (WORD datatype for length)
   */
  void setGeneralMessage(String msg);

  /**
   * OOO supports two sets of notification messages.<br>
   * <br>
   * They are called General message/subject and Special message/subject.<br>
   * The rest of the people will receive the general message/subject message.<br>
   * This function sets the general subject.<br>
   * If this field is not specified in by this API call, the value defined
   * using Notes Client will be used, otherwise the default for this field
   * is the following text <i>AUTO: Katherine Smith is out of the office
   * (returning 02/23/2009 10:12:17 AM)</i>.
   *
   * @param subject           string that will appear as the subject line of the
   *                          OOO notification
   * @param displayReturnDate Boolean which controls whether (“returning
   *                          &lt;date&gt;”) appears on the subject line
   */
  void setGeneralSubject(String subject, boolean displayReturnDate);

}