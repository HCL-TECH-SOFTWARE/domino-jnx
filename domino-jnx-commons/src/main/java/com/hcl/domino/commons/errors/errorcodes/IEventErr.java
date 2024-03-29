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

public interface IEventErr extends IGlobalErr {

  @ErrorText(text = "Warning: Cannot record event - cannot keep up with event occurrence rate!\nDecrease logging levels or increase the event pool size by setting EVENT_POOL_SIZE in NOTES.INI. Default size is %,d bytes. Maximum size is %,d bytes.\r\n")
  short ERR_EVENT_EXCEED_MEM = IGlobalErr.PKG_EVENT + 1;
  @ErrorText(text = "A queue with that name already exists.")
  short ERR_EVENT_DUPL_QUEUE = IGlobalErr.PKG_EVENT + 2;
  @ErrorText(text = "Not a known event queue.")
  short ERR_EVENT_NOT_Q = IGlobalErr.PKG_EVENT + 3;
  @ErrorText(text = "Cannot enqueue event - quota exceeded!")
  short ERR_EVENT_EXCEED_QUOTA = IGlobalErr.PKG_EVENT + 4;
  @ErrorText(text = "No entries in this queue")
  short ERR_EVTQUEUE_EMPTY = IGlobalErr.PKG_EVENT + 5;
  @ErrorText(text = "No such queue")
  short ERR_EVENT_NO_SUCH_QUEUE = IGlobalErr.PKG_EVENT + 6;
  @ErrorText(text = "Can only access your queue.")
  short ERR_EVENT_NOT_YOUR_Q = IGlobalErr.PKG_EVENT + 7;
  @ErrorText(text = "Wrong version of event package.")
  short ERR_EVENT_WRONG_VERSION = IGlobalErr.PKG_EVENT + 8;
  @ErrorText(text = "Invalid severity encountered.")
  short ERR_EVENT_BAD_SEVERITY = IGlobalErr.PKG_EVENT + 9;
  @ErrorText(text = "Event support not initialized.")
  short ERR_EVENT_NOT_INITIALIZED = IGlobalErr.PKG_EVENT + 10;
  @ErrorText(text = "Insufficient memory - event pool is full. You can increase it's size via the NOTES.INI setting EVENT_POOL_SIZE.")
  short ERR_EVENT_POOLFULL = IGlobalErr.PKG_EVENT + 11;
  @ErrorText(text = "Shutdown requested")
  short ERR_EVENT_SHUTDOWN = IGlobalErr.PKG_EVENT + 12;
  @ErrorText(text = "Invalid monitor method encountered.")
  short ERR_EVENT_BAD_MONITOR_METHOD = IGlobalErr.PKG_EVENT + 13;
  @ErrorText(text = "Invalid type encountered.")
  short ERR_EVENT_BAD_TYPE = IGlobalErr.PKG_EVENT + 14;
  @ErrorText(text = "The report database could not be opened.")
  short ERR_EVENT_REPORT_DB_NOT_OPEN = IGlobalErr.PKG_EVENT + 17;
  @ErrorText(text = "The server access view does not exist. Update the Statistics Report template.")
  short ERR_EVENT_NO_SRV_ACCESS_VIEW = IGlobalErr.PKG_EVENT + 18;
  @ErrorText(text = "The Statistics Collector is not running on the proxy server.")
  short ERR_REMOTE_COLLECTOR_QUEUE = IGlobalErr.PKG_EVENT + 19;
  @ErrorText(text = "Task timed out waiting for Monitoring Configuration database to be created. Please restart task.")
  short ERR_CONFIG_DB_TIMEOUT = IGlobalErr.PKG_EVENT + 20;
  @ErrorText(text = "Upgrading or creating the Monitoring Configuration database...")
  short ERR_ADDIN_CREATE_CONFIG = IGlobalErr.PKG_EVENT + 21;
  @ErrorText(text = "Cannot create the Monitoring Configuration database.")
  short ERR_ADDIN_NO_CONFIG = IGlobalErr.PKG_EVENT + 22;
  @ErrorText(text = "Domino Domain Monitoring functions cannot be executed when the Event task is not loaded.")
  short ERR_EVENT_NOT_LOADED = IGlobalErr.PKG_EVENT + 23;
  @ErrorText(text = "A DDM report document could not be opened.")
  short ERR_DDM_REPORT_DOC_PROBLEM = IGlobalErr.PKG_EVENT + 24;
  @ErrorText(text = "A DDM report document (NoteID 0x%X) could not be opened.")
  short ERR_DDM_REPORT_DOC_PROBLEM_EXT = IGlobalErr.PKG_EVENT + 25;
  @ErrorText(text = "Do you want to delete the selected groups?")
  short ERR_ADDIN_DELETE_GROUP = IGlobalErr.PKG_EVENT + 26;
  @ErrorText(text = "The remote collection server must be R5 or greater.")
  short ERR_REMOTE_COLLECTOR_R4 = IGlobalErr.PKG_EVENT + 27;
  @ErrorText(text = "Creating the Server Health Monitoring database...")
  short ERR_ADDIN_CREATE_REDZONE = IGlobalErr.PKG_EVENT + 28;
  @ErrorText(text = "Cannot create the Server Health Monitoring database.")
  short ERR_ADDIN_NO_REDZONE = IGlobalErr.PKG_EVENT + 29;
  @ErrorText(text = "Updating the Server Health Monitoring database design and configuration documents.")
  short ERR_ADDIN_UPDATE_REDZONE = IGlobalErr.PKG_EVENT + 30;
  @ErrorText(text = "Event: Error opening domain monitoring database %p")
  short ERR_OPEN_DDM = IGlobalErr.PKG_EVENT + 31;
  @ErrorText(text = "Event: Error loading domain monitoring configuration view '%s'")
  short ERR_DDM_LOADING_CFG_VIEW_EVENT = IGlobalErr.PKG_EVENT + 32;
  @ErrorText(text = "Event: Error opening note with note ID 0x%04X")
  short ERR_DDM_LOADING_CFG_DOC_EVENT = IGlobalErr.PKG_EVENT + 33;
  @ErrorText(text = "Event: Error loading DDM configuration")
  short ERR_DDM_LOADING_CFG_EVENT = IGlobalErr.PKG_EVENT + 34;
  @ErrorText(text = "Event: Error loading specified library %s")
  short ERR_DDM_LIB_LOAD_EVENT = IGlobalErr.PKG_EVENT + 35;
  @ErrorText(text = "Event: Error loading domain monitoring event information from database %p")
  short ERR_LOADING_DDM = IGlobalErr.PKG_EVENT + 36;
  @ErrorText(text = "Event: Error opening domain monitoring event document (Note ID 0x%04.04X) in database %p")
  short ERR_DDM_OPEN_NOTE = IGlobalErr.PKG_EVENT + 37;
  @ErrorText(text = "Domino Domain Monitoring Database is not yet opened or created. Details will be excluded from some events until the database is available.")
  short ERR_DDM_NOT_READY_FOR_BUSINESS = IGlobalErr.PKG_EVENT + 38;
  @ErrorText(text = "Events are being generated that should include an addin name, but do not - '%s' (0x%X) : '%s' (0x%X)")
  short ERR_MISSING_ADDIN_NAME = IGlobalErr.PKG_EVENT + 39;
  @ErrorText(text = "The provided Disk Spindle Information is incomplete")
  short ERR_RM_DISK_SPINDLE_INFO_INCOMPLETE = IGlobalErr.PKG_EVENT + 40;
  @ErrorText(text = "The provided Disk Spindle Information includes disks that do not exist.")
  short ERR_RM_DISK_SPINDLE_INFO_EXCESSIVE = IGlobalErr.PKG_EVENT + 41;
  @ErrorText(text = "Disk Spindle Information has been provided in the Directory")
  short ERR_RM_DISK_SPINDLE_INFO_OK = IGlobalErr.PKG_EVENT + 42;
  @ErrorText(text = "Disk Spindle Information was not provided in the Directory")
  short ERR_RM_DISK_SPINDLE_INFO_NOT_PROVIDED = IGlobalErr.PKG_EVENT + 43;
  @ErrorText(text = "Error loading domain monitoring view '%s'")
  short ERR_DDM_LOADING_VIEW_EVENT = IGlobalErr.PKG_EVENT + 44;
  @ErrorText(text = "Preexisting PUID found %s")
  short ERR_DDM_PREEXISTING_PUID = IGlobalErr.PKG_EVENT + 45;
  @ErrorText(text = "%d duplicate PUIDs found")
  short ERR_DDM_DUPLICATE_PUID = IGlobalErr.PKG_EVENT + 46;
  @ErrorText(text = "Events are being generated with a mixed severity of normal and non-normal - '%s' (%s0x%X Sev=%d) : '%s' (%s0x%X Sev=%d)")
  short ERR_NORMAL_NONNORMAL_SEV_MIX = IGlobalErr.PKG_EVENT + 47;
  @ErrorText(text = "Logical disk activity (%s) exceeds configured thresholds.")
  short ERR_RM_DISK_UTIL = IGlobalErr.PKG_EVENT2 + 00;
  @ErrorText(text = "Memory Utilization exceeds the configured thresholds.")
  short ERR_RM_MEMORY_UTIL = IGlobalErr.PKG_EVENT2 + 1;
  @ErrorText(text = "Network adapter activity (%s) exceeds configured thresholds.")
  short ERR_RM_NETWORK_UTIL = IGlobalErr.PKG_EVENT2 + 2;
  @ErrorText(text = "CPU utilization exceeds configured thresholds.")
  short ERR_RM_CPU_UTIL = IGlobalErr.PKG_EVENT2 + 3;
  @ErrorText(text = "Add in Monitoring pool is full")
  short ERR_ADDINMON_POOL_FULL = IGlobalErr.PKG_EVENT2 + 4;
  @ErrorText(text = "Server %a is no longer running")
  short ERR_ADMIN_MONITOR_SERVER_DOWN = IGlobalErr.PKG_EVENT2 + 5;
  @ErrorText(text = "Server %a has come back up")
  short ERR_ADMIN_MONITOR_SERVER_UP = IGlobalErr.PKG_EVENT2 + 6;
  @ErrorText(text = "Server task %s on %a reported %s error. (%e)")
  short ERR_ADMIN_MONITOR_SERVER_TASK_ERROR = IGlobalErr.PKG_EVENT2 + 7;
  @ErrorText(text = "Server task %s on %a is no longer responding")
  short ERR_ADMIN_MONITOR_SERVER_TASK_TIMEOUT = IGlobalErr.PKG_EVENT2 + 8;
  @ErrorText(text = "Server task %s on %a is responding")
  short ERR_ADMIN_MONITOR_SERVER_TASK_RESTART = IGlobalErr.PKG_EVENT2 + 9;
  @ErrorText(text = "Server task %s on %a is running")
  short ERR_ADMIN_MONITOR_SERVER_TASK_UP = IGlobalErr.PKG_EVENT2 + 10;
  @ErrorText(text = "Server task %s on %a is no longer running")
  short ERR_ADMIN_MONITOR_SERVER_TASK_DOWN = IGlobalErr.PKG_EVENT2 + 11;
  @ErrorText(text = "Task %s is not configured to run on server %a")
  short ERR_ADMIN_MONITOR_SERVER_TASK_NOT_CONFIGURED = IGlobalErr.PKG_EVENT2 + 12;
  @ErrorText(text = "Upgrade mail file error")
  short ERR_UPGRADE_MAIL_ERROR = IGlobalErr.PKG_EVENT2 + 13;
  @ErrorText(text = "Platform statistics are disabled")
  short ERR_RM_DISABLED_PLATFORM_STATS = IGlobalErr.PKG_EVENT2 + 14;
  @ErrorText(text = "Disk counters are disabled")
  short ERR_RM_DISABLED_DISK_COUNTERS_WIN = IGlobalErr.PKG_EVENT2 + 15;
  @ErrorText(text = "Domino data files and transaction logging use the same disk")
  short ERR_RM_TRANS_SAME_AS_DATA = IGlobalErr.PKG_EVENT2 + 16;
  @ErrorText(text = "Domino data files and transaction logging use the same auxiliary storage pool")
  short ERR_RM_TRANS_SAME_AS_DATA_ISERIES = IGlobalErr.PKG_EVENT2 + 17;
  @ErrorText(text = "Domino data files and Domino executables use the same disk")
  short ERR_RM_BIN_SAME_AS_DATA = IGlobalErr.PKG_EVENT2 + 18;
  @ErrorText(text = "Domino data files and Domino executables use the same auxiliary storage pool")
  short ERR_RM_BIN_SAME_AS_DATA_ISERIES = IGlobalErr.PKG_EVENT2 + 19;
  @ErrorText(text = "Logical disk activity exceeds configured thresholds.")
  short ERR_RM_DISK_UTIL_NEED_SPINDLE_COUNT = IGlobalErr.PKG_EVENT2 + 20;
  @ErrorText(text = "Network counters are disabled")
  short ERR_RM_DISABLED_NET_COUNTERS_WIN = IGlobalErr.PKG_EVENT2 + 21;
  @ErrorText(text = "Too much detail data reported by DDM probe.  Probe results will be truncated.")
  short ERR_DETAILDOC_TOO_LARGE = IGlobalErr.PKG_EVENT2 + 22;
  @ErrorText(text = "Logical disk activity (%s) no longer exceeds configured thresholds.")
  short ERR_RM_DISK_CLEAR = IGlobalErr.PKG_EVENT2 + 23;
  @ErrorText(text = "Memory Utilization no longer exceeds the configured thresholds.")
  short ERR_RM_MEMORY_CLEAR = IGlobalErr.PKG_EVENT2 + 24;
  @ErrorText(text = "Network adapter activity (%s) no longer exceeds configured thresholds.")
  short ERR_RM_NETWORK_CLEAR = IGlobalErr.PKG_EVENT2 + 25;
  @ErrorText(text = "CPU utilization no longer exceeds configured thresholds.")
  short ERR_RM_CPU_CLEAR = IGlobalErr.PKG_EVENT2 + 26;
  @ErrorText(text = "Problem refreshing DDM target server information")
  short ERR_DDM_TARGET_REFRESH_ERROR = IGlobalErr.PKG_EVENT2 + 27;
  @ErrorText(text = "%s received the following memory allocation error: ")
  short ERR_PROBE_MEM_ALLOC = IGlobalErr.PKG_EVENT2 + 28;
  @ErrorText(text = "Event: Unable to create database %s since template file %s cannot be found.")
  short ERR_TEMPLATE_NOT_FOUND = IGlobalErr.PKG_EVENT2 + 29;
  @ErrorText(text = "Platform statistics are enabled")
  short ERR_RM_DISABLED_PLATFORM_STATS_CLEAR = IGlobalErr.PKG_EVENT2 + 30;
  @ErrorText(text = "Disk counters are enabled")
  short ERR_RM_DISABLED_DISK_COUNTERS_WIN_CLEAR = IGlobalErr.PKG_EVENT2 + 31;
  @ErrorText(text = "If an event of any type and of severity Fatal, Failure or Warning (High) occurs, log notification to %s.")
  short ERR_ADDIN_SETUP_EVENTS = IGlobalErr.PKG_EVENT3 + 29;
  @ErrorText(text = "Generate a Security event of Warning (High) severity if the ACL for database %s changes.  In addition, notify %a by mail.")
  short ERR_ADDIN_SETUP_ACL = IGlobalErr.PKG_EVENT3 + 30;
  @ErrorText(text = "Generate a Replication event of Warning (High) severity if the database %s on any server has not replicated in 24 hours with ANY server. In addition, notify %a via mail.")
  short ERR_ADDIN_SETUP_REPL = IGlobalErr.PKG_EVENT3 + 31;

}
