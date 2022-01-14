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
package com.hcl.domino.security;

/**
 * Enum of available ECL capabilities
 */
public enum ECLCapability {
  /** Access files (read/write/export/import) */
  AccessFiles((short) 0x0008, false),

  /** Access current db's docs/db */
  AccessCurrentDatabase((short) 0x0010, false),

  /** Access environ vars (get/set) */
  AccessEnvironmentVars((short) 0x0080, false),

  /** Access non-notes dbs (@DB with non "","Notes" first arg) */
  AccessNonNotesDatabases((short) 0x0100, false),

  /** Access "code" in external systems (LS, DLLS, DDE) */
  AccessExternalSystems((short) 0x0200, false),

  /** Access external programs (OLE/SendMsg/Launch) */
  AccessExternalPrograms((short) 0x0400, false),

  /** Send mail (@MailSend) */
  SendMail((short) 0x0800, false),

  /** Access ECL */
  AccessECL((short) 0x1000, false),

  /** Read access to other databases */
  ReadAccessOtherDatabases((short) 0x2000, false),

  /** Write access to other databases */
  WriteAccessOtherDatabases((short) 0x4000, false),

  /** Ability to export data (copy/print, etc) */
  ExportData((short) (0x8000 & 0xffff), false),

  // extended flags

  /** Access network programatically */
  AccessNetwork((short) 0x0001, true),

  /** Property Broker Get */
  PropertyBrokerGet((short) 0x0002, true),

  /** Property Broker Put */
  PropertyBrokerPut((short) 0x0004, true),

  /** Widget configuration */
  WidgetConfiguration((short) 0x0008, true),

  /** access to load Java */
  LoadJava((short) 0x0010, true);

  private int m_flag;
  private boolean m_isWorkstationECL;

  ECLCapability(final int flag) {
    this.m_flag = flag;
  }

  ECLCapability(final short flag, final boolean isWorkstationACL) {
    this.m_flag = flag & 0xffff;
    this.m_isWorkstationECL = isWorkstationACL;
  }

  public int getValue() {
    return this.m_flag;
  }

  /**
   * Method to check if the capability is only available in the Notes Client
   *
   * @return true if workstation ECL
   */
  public boolean isWorkstationECL() {
    return this.m_isWorkstationECL;
  }

}
