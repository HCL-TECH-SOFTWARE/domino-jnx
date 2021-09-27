package com.hcl.domino.jna.admin;

import com.hcl.domino.admin.DirectoryAssistance;
import com.hcl.domino.commons.util.NotesErrorUtils;
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
    DirectoryAssistanceStruct daConfigStruct = new DirectoryAssistanceStruct(serverName.getBytes(), dirAssistDBName.getBytes(), domainName.getBytes(), companyName.getBytes(),
        hostName.getBytes(), ldapVendor, userName.getBytes(), password.getBytes(), useSSL, port);

    CreateDAConfigStruct daConfig = new CreateDAConfigStruct(updateServerDoc, searchOrder, dnSearch.getBytes(), acceptExpiredCerts,
        verifyRemoteSrvCert, timeout, maxEntriesReturned, daConfigStruct);

    daConfig.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().CreateDAConfig(daConfig));
  }

  @Override
  public void enableDisableDA(String docUnid, boolean enableDomain) {
    EnableDisableDAStruct daConfig  = new EnableDisableDAStruct(serverName.getBytes(), dirAssistDBName.getBytes(), docUnid.getBytes(), enableDomain);

    daConfig.write();
    System.out.println("The value returned from the call is "+ NotesCAPI.get().EnableDisableDADomain(daConfig));
    NotesErrorUtils.checkResult(NotesCAPI.get().EnableDisableDADomain(daConfig));
  }

  @Override
  public void updateDAConfig(String docUNID, String domainName, String companyName, String hostName, short ldapVendor,
      String userName, String password, boolean useSSL, short port) {
    DirectoryAssistanceStruct daConfigStruct = new DirectoryAssistanceStruct(serverName.getBytes(), dirAssistDBName.getBytes(), domainName.getBytes(), companyName.getBytes(),
        hostName.getBytes(), ldapVendor, userName.getBytes(), password.getBytes(), useSSL, port);

    UpdateDAConfigStruct daConfig = new UpdateDAConfigStruct(docUNID.getBytes(), daConfigStruct);

    daConfig.write();
    NotesErrorUtils.checkResult(NotesCAPI.get().UpdateDAConfig(daConfig));
  }

}
