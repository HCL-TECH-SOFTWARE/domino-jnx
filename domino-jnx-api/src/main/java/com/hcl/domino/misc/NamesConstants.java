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
package com.hcl.domino.misc;

/**
 * API constants related to names.nsf document formats.
 *
 * @since 1.0.28
 */
public interface NamesConstants {
  String ALLOW_ACCESS_ITEM = "AllowAccess"; //$NON-NLS-1$
  String DENY_ACCESS_ITEM = "DenyAccess"; //$NON-NLS-1$
  String CREATE_FILE_ACCESS_ITEM = "CreateAccess"; //$NON-NLS-1$
  String CREATE_REPLICA_ACCESS_ITEM = "ReplicaAccess"; //$NON-NLS-1$
  String CREATE_TEMPLATE_ACCESS_ITEM = "TemplateAccess"; //$NON-NLS-1$
  String ADMIN_ACCESS_ITEM = "Administrator"; //$NON-NLS-1$
  String ALLOW_PASSTHRU_TARGET_ITEM = "PTTargets"; //$NON-NLS-1$
  String ALLOW_PASSTHRU_CLIENT_ITEM = "PTClients"; //$NON-NLS-1$
  String ALLOW_PASSTHRU_CALLER_ITEM = "PTCallers"; //$NON-NLS-1$
  String ALLOW_PASSTHRU_ACCESS_ITEM = "PTAccess"; //$NON-NLS-1$
  String ALLOW_RESTRICTED_LOTUSCRIPT_ITEM = "RestrictedList"; //$NON-NLS-1$
  String ALLOW_UNRESTRICTED_LOTUSCRIPT_ITEM = "UnrestrictedList"; //$NON-NLS-1$
  String ALLOW_ON_BEHALF_ITEM = "OnBehalfOfLst"; //$NON-NLS-1$
  String ALLOW_ON_BEHALF_INVOKER_ITEM = "OnBehalfOfInvokerLst"; //$NON-NLS-1$
  String ALLOW_PERSONAL_ITEM = "PrivateList"; //$NON-NLS-1$
  String ALLOW_LIBRARIES_ITEM = "LibsLst"; //$NON-NLS-1$
  String ALLOW_REMOTE_HNAMES_ITEM = "TrustedSrvrs"; //$NON-NLS-1$
  String ALLOW_MONITORS_ITEM = "AllowMonitors"; //$NON-NLS-1$
  String DENY_MONITORS_ITEM = "DenyMonitors"; //$NON-NLS-1$
  String FULL_ADMIN_ACCESS_ITEM = "FullAdmin"; //$NON-NLS-1$
  String DB_ADMIN_ACCESS_ITEM = "DBAdmin"; //$NON-NLS-1$
  String REMOTE_ADMIN_ACCESS_ITEM = "RemoteAdmin"; //$NON-NLS-1$
  String VO_ADMIN_ACCESS_ITEM = "VOAdmin"; //$NON-NLS-1$
  String WEB_ADMIN_ACCESS_ITEM = "BrowserAdminAccess"; //$NON-NLS-1$
  String NNTP_ADMIN_ACCESS_ITEM = "NNTP_Admin"; //$NON-NLS-1$
  String SYS_ADMIN_ACCESS_ITEM = "SysAdmin"; //$NON-NLS-1$
  String SYS_ADMIN_RES_ACCESS_ITEM = "ResSysAdmin"; //$NON-NLS-1$
  String SYS_ADMIN_RES_COMMANDS_ITEM = "ResSystemCmds"; //$NON-NLS-1$
  String TRAVELER_ALLOW_ACCESS_ITEM = "tsAllowAccess"; //$NON-NLS-1$
  String TRAVELER_DENY_ACCESS_ITEM = "tsDenyAccess"; //$NON-NLS-1$
}
