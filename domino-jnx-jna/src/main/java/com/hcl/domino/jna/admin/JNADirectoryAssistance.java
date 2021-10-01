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
package com.hcl.domino.jna.admin;

import com.hcl.domino.admin.DirectoryAssistance;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.structs.CreateDAConfigStruct;
import com.hcl.domino.jna.internal.structs.DirectoryAssistanceStruct;
import com.hcl.domino.jna.internal.structs.EnableDisableDAStruct;
import com.hcl.domino.jna.internal.structs.UpdateDAConfigStruct;

public class JNADirectoryAssistance implements DirectoryAssistance {
  private final String serverName;
  private final String dirAssistDBName;

  public JNADirectoryAssistance(String serverName, String dirAssistDBName) {
    this.serverName = serverName;
    this.dirAssistDBName = dirAssistDBName;
  }

  @Override
  public void createDAConfig(boolean updateServerDoc, String domainName, String companyName, short searchOrder, String hostName,
      short ldapVendor, String userName, String password, String dnSearch, boolean useSSL, short port, boolean acceptExpiredCerts,
      boolean verifyRemoteSrvCert, short timeout, short maxEntriesReturned) {
    DirectoryAssistanceStruct daConfigStruct = new DirectoryAssistanceStruct();
    NotesStringUtils.toLMBCS(this.serverName, true, daConfigStruct.szServerName);
    NotesStringUtils.toLMBCS(this.dirAssistDBName, true, daConfigStruct.szDirAssistDBName);
    NotesStringUtils.toLMBCS(domainName, true, daConfigStruct.szDomainName);
    NotesStringUtils.toLMBCS(companyName, true, daConfigStruct.szCompanyName);
    NotesStringUtils.toLMBCS(hostName, true, daConfigStruct.szHostName);
    NotesStringUtils.toLMBCS(userName, true, daConfigStruct.szUserName);
    NotesStringUtils.toLMBCS(password, true, daConfigStruct.szPassword);

    daConfigStruct.wLDAPVendor = ldapVendor;
    daConfigStruct.bUseSSL = useSSL;
    daConfigStruct.wPort = port;
    daConfigStruct.write();

    CreateDAConfigStruct daConfig = new CreateDAConfigStruct();
    daConfig.bUpdateServerDoc = updateServerDoc;
    daConfig.wSearchOrder = searchOrder ;
    NotesStringUtils.toLMBCS(dnSearch, true, daConfig.szDNSearch);
    daConfig.bAcceptExpiredCertificates = acceptExpiredCerts;
    daConfig.bVerifyRemoteSrvCert = verifyRemoteSrvCert;
    daConfig.wTimeout = timeout ;
    daConfig.wMaxEntriesReturned = maxEntriesReturned ;
    daConfig.daStruct = daConfigStruct;
    daConfig.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().CreateDAConfiguration(daConfig));
  }

  @Override
  public void enableDisableDA(String docUnid, boolean enableDomain) {

    EnableDisableDAStruct daConfig  = new EnableDisableDAStruct();

    NotesStringUtils.toLMBCS(serverName, true, daConfig.szServerName);
    NotesStringUtils.toLMBCS(dirAssistDBName, true, daConfig.szDirAssistDBName);
    NotesStringUtils.toLMBCS(docUnid, true, daConfig.szDocUNID);
    daConfig.bEnableDomain = enableDomain;

    daConfig.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().EnableDisableDADomain(daConfig));
  }

  @Override
  public void updateDAConfig(String docUNID, String domainName, String companyName, String hostName, short ldapVendor,
      String userName, String password, boolean useSSL, short port) {
    DirectoryAssistanceStruct daConfigStruct = new DirectoryAssistanceStruct(serverName.getBytes(), dirAssistDBName.getBytes(), domainName.getBytes(), companyName.getBytes(),
        hostName.getBytes(), ldapVendor, userName.getBytes(), password.getBytes(), useSSL, port);

    UpdateDAConfigStruct daConfig = new UpdateDAConfigStruct(docUNID.getBytes(), daConfigStruct);

    daConfig.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().UpdateDAConfiguration(daConfig));
  }

}
