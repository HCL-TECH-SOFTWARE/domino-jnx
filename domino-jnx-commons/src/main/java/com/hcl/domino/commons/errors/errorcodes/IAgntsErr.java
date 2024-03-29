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
package com.hcl.domino.commons.errors.errorcodes;

import com.hcl.domino.commons.errors.ErrorText;

public interface IAgntsErr extends IGlobalErr {

  @ErrorText(text = "Unable to load saved search; search is corrupt.")
  short ERR_QUERY_BADDATATYPE = IGlobalErr.PKG_AGENTS + 1;
  @ErrorText(text = "Unable to load saved search; data is corrupt.")
  short ERR_QUERY_BADDATA = IGlobalErr.PKG_AGENTS + 2;
  @ErrorText(text = "This search cannot be recognized by this version of IBM Notes.")
  short ERR_QUERY_UNKNOWNTERM = IGlobalErr.PKG_AGENTS + 3;
  @ErrorText(text = "This action cannot be recognized by this version of IBM Notes.")
  short ERR_ACTION_UNKNOWNTERM = IGlobalErr.PKG_AGENTS + 4;
  @ErrorText(text = "Unable to load saved action; action is corrupt.")
  short ERR_ACTION_BADDATA = IGlobalErr.PKG_AGENTS + 5;
  @ErrorText(text = "Unable to load agent; data is corrupt.")
  short ERR_ASSISTANT_BADDATA = IGlobalErr.PKG_AGENTS + 6;
  @ErrorText(text = "The agent could not reply to the document because the author of the document is unknown.")
  short ERR_ACTION_NONAMETOREPLYTO = IGlobalErr.PKG_AGENTS + 10;
  @ErrorText(text = "Unable to send mail to %s, no match found in Name & Address Book(s)")
  short ERR_ACTION_NO_MATCH = IGlobalErr.PKG_AGENTS + 12;
  @ErrorText(text = "Unable to send mail to %s, multiple matches found in Name & Address Book(s)")
  short ERR_ACTION_AMBIG_MATCH = IGlobalErr.PKG_AGENTS + 13;
  @ErrorText(text = "Saved search is invalid.")
  short ERR_QUERY_INVALIDSETTINGS = IGlobalErr.PKG_AGENTS + 18;
  @ErrorText(text = "Unable to load saved action; action is corrupt.")
  short ERR_ACTION_BADDATATYPE = IGlobalErr.PKG_AGENTS + 19;
  @ErrorText(text = "This agent cannot be run manually.  It will only be run when documents are pasted into the database.")
  short ERR_INVALID_RUNNOW = IGlobalErr.PKG_AGENTS + 45;
  @ErrorText(text = "Programmability Restrictions")
  short STR_AGENT_PROG_RESTRICTIONS = IGlobalErr.PKG_AGENTS + 54;
  @ErrorText(text = "(must have a user name to open db with access control)")
  short ERR_ASSIST_NO_USER = IGlobalErr.PKG_AGENTS + 58;
  @ErrorText(text = "%a is not authorized to delete document %lx")
  short ERR_LOG_NODELETEACCESS = IGlobalErr.PKG_AGENTS + 59;
  @ErrorText(text = "%a is not authorized to modify document %lx")
  short ERR_LOG_NOMODIFYACCESS = IGlobalErr.PKG_AGENTS + 60;
  @ErrorText(text = "Unable to edit LotusScript agent; action list is corrupt")
  short ERR_BAD_LS_AGENT = IGlobalErr.PKG_AGENTS + 61;
  @ErrorText(text = "Could not find agent run data object")
  short ERR_ASSIST_NORUNOBJECT = IGlobalErr.PKG_AGENTS + 68;
  @ErrorText(text = "Could not execute formula when composing mail: no document found")
  short ERR_ASSIST_NOSAMPLENOTE = IGlobalErr.PKG_AGENTS + 70;
  @ErrorText(text = "View has too many columns")
  short ERR_ASSIST_TOOMANYCOLUMNS = IGlobalErr.PKG_AGENTS + 73;
  @ErrorText(text = "Bad view format")
  short ERR_ASSIST_BADVIEWFORMAT = IGlobalErr.PKG_AGENTS + 74;
  @ErrorText(text = "The server is running a previous version of IBM Notes that does not support private agents")
  short ERR_ACCESS_OLDVERSION = IGlobalErr.PKG_AGENTS + 83;
  @ErrorText(text = "Cannot create a formula which references a rich text field")
  short ERR_CANNOT_QUERY_RICHTEXT = IGlobalErr.PKG_AGENTS + 86;
  @ErrorText(text = "You must save the document before this action can be performed")
  short ERR_DOCUMENT_NOT_SAVED = IGlobalErr.PKG_AGENTS + 87;
  @ErrorText(text = "Append is only valid for text fields.")
  short ERR_MODIFYFIELD_APPEND = IGlobalErr.PKG_AGENTS + 90;
  @ErrorText(text = "Execution time limit exceeded by Agent '%s' in database '%p'. Agent signer '%a'.")
  short ERR_ASSISTANT_TIMEOUT = IGlobalErr.PKG_AGENTS + 91;
  @ErrorText(text = "No document has been selected.")
  short ERR_AGENTS_NODOCUMENT = IGlobalErr.PKG_AGENTS + 92;
  @ErrorText(text = "Plain text cannot be entered into a simple actions field.")
  short ERR_ACTION_TEXTNOTSUPPORTED = IGlobalErr.PKG_AGENTS + 102;
  @ErrorText(text = "@Command and other UI functions are not allowed with this search type; please select 'None' as your runtime target.")
  short ERR_AGENT_NOUICOMMANDS = IGlobalErr.PKG_AGENTS + 105;
  @ErrorText(text = "Unable to find folder or view")
  short ERR_QUERY_FOLDERNOTFOUND = IGlobalErr.PKG_AGENTS + 106;
  @ErrorText(text = "Unable to send mail; no match found in Name & Address Book(s)")
  short ERR_MAIL_NO_MATCH = IGlobalErr.PKG_AGENTS + 115;
  @ErrorText(text = "Unable to send mail; multiple matches found in Name & Address Book(s)")
  short ERR_MAIL_AMBIGUOUS_MATCH = IGlobalErr.PKG_AGENTS + 116;
  @ErrorText(text = "Agent run context must be of extended type to use this call")
  short ERR_AGENT_RUNCTX_EXTENDED = IGlobalErr.PKG_AGENTS + 117;
  @ErrorText(text = "Run context cannot be used with more than one agent at the same time")
  short ERR_AGENT_NOMULT_AGENTRUN = IGlobalErr.PKG_AGENTS + 118;
  @ErrorText(text = "Unknown redirection type")
  short ERR_AGENT_UNKNOWN_REDIR = IGlobalErr.PKG_AGENTS + 119;
  @ErrorText(text = "Unsupported trigger and search in the background or embedded agent")
  short ERR_AGENT_UI_TRIGGER = IGlobalErr.PKG_AGENTS + 120;
  @ErrorText(text = "Error validating user's agent execution access")
  short ERR_AMGR_RUN_ACCESS_ERROR = IGlobalErr.PKG_AGENTS + 121;
  @ErrorText(text = "Warning: in agent '%s' in database '%p' signed by '%a' calling script library '%s'. The rights of the agent have been lowered to the rights of the script library signer '%a'. %s")
  short ERR_AGENT_LIBRARY_LOWER_RIGHTS = IGlobalErr.PKG_AGENTS + 122;
  @ErrorText(text = "Security Error in Agent '%s' in database '%p' signed by '%a' calling script library '%s' signed by '%a'. The signer of the script library loaded via 'Execute' cannot have lower rights than the agent signer. %s")
  short ERR_AGENT_SCIPTLIB_FINISHEDUSE = IGlobalErr.PKG_AGENTS + 123;
  @ErrorText(text = "Cannot open databases on machines other than the server running your program")
  short ERR_AMGR_DBOPEN_NOTLOCAL = IGlobalErr.PKG_AGENT1 + 14;
  @ErrorText(text = "'Before mail arrives' agents cannot run other agents")
  short STR_LOG_SYNCH_NO_EMBED = IGlobalErr.PKG_AGENTS2 + 0;
  @ErrorText(text = "AMgr: Agent '%s' will not run. It is intended to run on %s")
  short ERR_AMGR_WRONG_SERVER = IGlobalErr.PKG_AGENTS2 + 2;
  @ErrorText(text = "Error in agent security configuration. Please contact your administrator.  Server log contains additional details.")
  short ERR_AGENT_SEC_CONFIGURATION = IGlobalErr.PKG_AGENTS2 + 3;
  @ErrorText(text = "Document set for JIT encryption and no public key available.")
  short ERR_AMGR_NOPUBKEY = IGlobalErr.PKG_AGENTS2 + 4;
  @ErrorText(text = "Document set for MIME format and an error occurred during sending or conversion.")
  short ERR_AMGR_NOMIMESENT = IGlobalErr.PKG_AGENTS2 + 5;
  @ErrorText(text = "Invalid operation on folder '%s' in 'Before mail delivery' agent. Invalid operation(s) ignored.")
  short STR_LOG_SYNCH_INVALIDOP = IGlobalErr.PKG_AGENTS2 + 6;
  @ErrorText(text = "Error processing your request for %s ")
  short ERR_AGENT_CONSOLE = IGlobalErr.PKG_AGENTS2 + 7;
  @ErrorText(text = "Private agents cannot be saved by server-based agents")
  short ERR_AGENT_SAVE_NOPRIVATE = IGlobalErr.PKG_AGENTS3 + 8;
  @ErrorText(text = "Effective users of the saved agent and the saving agent must match")
  short ERR_AGENT_SAVE_NOMATCH = IGlobalErr.PKG_AGENTS3 + 9;
  @ErrorText(text = "The agent being saved contains a conflicting 'On behalf' value")
  short ERR_AGENT_SAVE_DIFFONBEHALF = IGlobalErr.PKG_AGENTS3 + 10;
  @ErrorText(text = "An agent invoked via RunOnServer method does not support 'run as we web user' flag")
  short ERR_AGENT_NO_RUNASWEB = IGlobalErr.PKG_AGENTS3 + 11;
  @ErrorText(text = "This agent contains an illegally added 'On behalf' attribute.  To make the agent valid, please remove it.")
  short ERR_AGENT_WRONGVERSION = IGlobalErr.PKG_AGENTS3 + 12;
  @ErrorText(text = "This version of IBM Notes does not support agents of this version")
  short ERR_AGENT_WRONGVERSION_GENERIC = IGlobalErr.PKG_AGENTS3 + 13;
  @ErrorText(text = "Invalid invoker category is used.  This category is reserved for Notes core.")
  short ERR_AGENT_INVALID_INVOKER = IGlobalErr.PKG_AGENTS3 + 14;
  @ErrorText(text = "Saving agent and saved agent have incompatible settings of 'run as web user' flag. ")
  short ERR_AGENT_INVALID_WEBUSER = IGlobalErr.PKG_AGENTS3 + 15;
  @ErrorText(text = "Run Simple and Formula agents")
  short STR_AGENT_SIMPLE_FORMULA = IGlobalErr.PKG_AGENTS3 + 16;
  @ErrorText(text = "Users without rights to sign 'On Behalf' agents cannot sign agents that run as web user unless web user is agent signer.")
  short ERR_AGENT_RESTRICTED_WEBUSER2 = IGlobalErr.PKG_AGENTS3 + 17;
  @ErrorText(text = "Users without rights to sign 'On Behalf' agents can only run agents on their own behalf.")
  short ERR_AGENT_RESTRICTED_ONBEHALF = IGlobalErr.PKG_AGENTS3 + 18;
  @ErrorText(text = "Full text operations on database '%p' which is not full text indexed.  This is extremely inefficient.")
  short STR_AGENT_NOFULLTEXTINDEX = IGlobalErr.PKG_AGENTS3 + 19;
  @ErrorText(text = "Agent '%s' has been corrupted.  Significant fields have been excluded from the signature.")
  short ERR_AGENT_CORRUPTE_SIGN = IGlobalErr.PKG_AGENTS3 + 20;
  @ErrorText(text = "Error in Agent '%s' in database '%p' signed by '%a' calling script library '%s'. Script library signer '%a' does not have proper rights. %s")
  short ERR_AGENT_SCRIPTLIBRARY = IGlobalErr.PKG_AGENTS3 + 21;
  @ErrorText(text = "Agent '%s' contains invalidly modified 'Run as web user' flag. Examine and resave the agent.")
  short ERR_AGENT_BADWEDUSER_CONSOLE = IGlobalErr.PKG_AGENTS3 + 22;
  @ErrorText(text = "Agent contains invalidly modified 'Run as web user' flag. Examine and resave the agent.")
  short ERR_AGENT_BADWEDUSER = IGlobalErr.PKG_AGENTS3 + 23;
  @ErrorText(text = "Cannot create a formula which references an Outline")
  short ERR_CANNOT_QUERY_SITEMAP = IGlobalErr.PKG_AGENTS3 + 24;
  @ErrorText(text = "A runtime error will occur if this agent requires user interaction. Interactive agents cannot be run in a background client thread. Do you wish to save?")
  short ERR_BACKGROUNDTHREAD_UICOMMAND_CONFLICT = IGlobalErr.PKG_AGENTS3 + 25;
  @ErrorText(text = "Warning: INI variable is used to suppress expansion of personal agent restrictions list ")
  short STR_AGENT_SUPRESS_SIMPLESECURITY = IGlobalErr.PKG_AGENTS3 + 26;
  @ErrorText(text = "Agent '%s': User ('%a') does not have rights to run agents in 'Full Administrator' mode ")
  short ERR_AGENT_RESTRICTED_FULLADMIN = IGlobalErr.PKG_AGENTS3 + 27;
  @ErrorText(text = "Error in Agent '%s' in database '%p' signed by '%a' calling script library '%s'. Script library signature is corrupted.")
  short ERR_AGENT_SCRIPTSIG = IGlobalErr.PKG_AGENTS3 + 28;
  @ErrorText(text = "%ld minute(s) have elapsed since start of agent '%s' in database '%p'. Threshold level %ld minute(s). Agent Owner: '%a'. ")
  short ERR_AGENT_DDM_LONG_AMGR = IGlobalErr.PKG_AGENTS3 + 29;
  @ErrorText(text = "Start of execution for agent '%s' in database '%p' is behind schedule by %ld minutes(s). Threshold level %ld minutes(s). Agent Owner: '%a'.")
  short ERR_AGENT_DDM_BEHINDSCHEDULE = IGlobalErr.PKG_AGENTS3 + 30;
  @ErrorText(text = "%s memory usage by agent '%s' in database '%p'. Threshold level %s. Agent Owner: '%a'.")
  short ERR_AGENT_DDM_MEMORYHOG = IGlobalErr.PKG_AGENTS3 + 31;
  @ErrorText(text = "%ld seconds CPU usage by agent '%s' in database '%p'. Threshold level %ld seconds. Agent Owner: '%a'.")
  short ERR_AGENT_DDM_CPUHOG = IGlobalErr.PKG_AGENTS3 + 32;
  @ErrorText(text = "Error validating execution rights for agent '%s' in database '%p'. Agent signer '%a', effective user '%a'. %s")
  short ERR_AGENT_DDM_NOACCESS = IGlobalErr.PKG_AGENTS3 + 33;
  @ErrorText(text = "Examine '%s' field in the Server Record.")
  short STR_WRONG_FIELD = IGlobalErr.PKG_AGENTS3 + 34;
  @ErrorText(text = "Agent signer, '%a', does not have access to this server.")
  short STR_AGENT_ACCESS_SERVER = IGlobalErr.PKG_AGENTS3 + 35;
  @ErrorText(text = "Agent execution time limit exceeded.")
  short MSG_AMGR_RUN_TIMEOUT = IGlobalErr.PKG_AGENTS3 + 36;
  @ErrorText(text = "Agent '%s' in '%p' disabled during Design Update from template '%s'. Agent signer '%a'.")
  short MSG_AGENT_DESIGNUPDATE_DISABLE = IGlobalErr.PKG_AGENTS3 + 37;
  @ErrorText(text = "%ld minute(s) have elapsed since start of agent '%s' in database '%p'. Threshold level %ld minute(s). Agent Owner: '%a'.")
  short ERR_AGENT_DDM_LONG_HTTP = IGlobalErr.PKG_AGENTS3 + 38;
  @ErrorText(text = "Sign Script Libraries")
  short STR_AGENT_SIGN_SCRIPTLIB = IGlobalErr.PKG_AGENTS3 + 39;
  @ErrorText(text = "Daily Clearing Event Issued")
  short STR_AGENT_DDM_CLEAR = IGlobalErr.PKG_AGENTS3 + 40;
  @ErrorText(text = "Out of memory, agent probe cannot run")
  short ERR_AGENT_DDM_OUTOFMEMORY = IGlobalErr.PKG_AGENTS3 + 41;
  @ErrorText(text = "Agent '%s' not found in database '%p'")
  short ERR_AGENT_NOT_FOUND = IGlobalErr.PKG_AGENTS3 + 42;
  @ErrorText(text = "Agent '%s' in '%p' could not be kept enabled during design update from template '%s'. Agent signer '%a'.")
  short ERR_AGENT_DESIGNUPDATE_ENABLE = IGlobalErr.PKG_AGENTS3 + 43;
  @ErrorText(text = "Enabled status can be preserved only on the server where the agent is scheduled to run or, for mail agents, on the home mail server. Enabled status cannot be preserved if '-Any server-' is specified for the agent.")
  short ERR_AGENT_DESIGNUPDATE_SERVER = IGlobalErr.PKG_AGENTS3 + 44;
  @ErrorText(text = "Enabled status can be preserved only for agents that were saved with Notes Domino release 6 or later.")
  short ERR_AGENT_DESIGNUPDATE_VERSION = IGlobalErr.PKG_AGENTS3 + 45;
  @ErrorText(text = "Server '%a' does not have rights to sign 'Out of office' agent on server '%a'. Please add '%a' to 'Sign agents to run on behalf of someone else' field in Programmability Section of Domino Directory")
  short ERR_AGENT_OOO_SERVER_RIGHTS = IGlobalErr.PKG_AGENTS3 + 46;
  @ErrorText(text = "'Out of office' agent for user '%a' is signed by '%a' who is not listed in 'Sign agents to run on behalf of someone else' field in Programmability Section of Domino Directory. Please validate the agent and sign it with an id that has this right")
  short ERR_AGENT_OOO_SIGNER_RIGHTS = IGlobalErr.PKG_AGENTS3 + 47;

}
