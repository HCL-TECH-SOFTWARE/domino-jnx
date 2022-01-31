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
package com.hcl.domino.commons.html;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.hcl.domino.misc.CNativeEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * This enumerates the commands supported in Domino urls
 *
 * @author Karsten Lehmann
 */
public enum CommandId implements CNativeEnum {
  UNKNOWN(NotesConstants.kUnknownCmdId),
  OPENSERVER(NotesConstants.kOpenServerCmdId),
  OPENDATABASE(NotesConstants.kOpenDatabaseCmdId),
  OPENVIEW(NotesConstants.kOpenViewCmdId),
  OPENDOCUMENT(NotesConstants.kOpenDocumentCmdId),
  OPENELEMENT(NotesConstants.kOpenElementCmdId),
  OPENFORM(NotesConstants.kOpenFormCmdId),
  OPENAGENT(NotesConstants.kOpenAgentCmdId),
  OPENNAVIGATOR(NotesConstants.kOpenNavigatorCmdId),
  OPENICON(NotesConstants.kOpenIconCmdId),
  OPENABOUT(NotesConstants.kOpenAboutCmdId),
  OPENHELP(NotesConstants.kOpenHelpCmdId),
  CREATEDOCUMENT(NotesConstants.kCreateDocumentCmdId),
  SAVEDOCUMENT(NotesConstants.kSaveDocumentCmdId),
  EDITDOCUMENT(NotesConstants.kEditDocumentCmdId),
  DELETEDOCUMENT(NotesConstants.kDeleteDocumentCmdId),
  SEARCHVIEW(NotesConstants.kSearchViewCmdId),
  SEARCHSITE(NotesConstants.kSearchSiteCmdId),
  NAVIGATE(NotesConstants.kNavigateCmdId),
  READFORM(NotesConstants.kReadFormCmdId),
  REQUESTCERT(NotesConstants.kRequestCertCmdId),
  READDESIGN(NotesConstants.kReadDesignCmdId),
  READVIEWENTRIES(NotesConstants.kReadViewEntriesCmdId),
  READENTRIES(NotesConstants.kReadEntriesCmdId),
  OPENPAGE(NotesConstants.kOpenPageCmdId),
  OPENFRAMESET(NotesConstants.kOpenFrameSetCmdId),
  /** OpenField command for Java applet(s) and HAPI */
  OPENFIELD(NotesConstants.kOpenFieldCmdId),
  SEARCHDOMAIN(NotesConstants.kSearchDomainCmdId),
  DELETEDOCUMENTS(NotesConstants.kDeleteDocumentsCmdId),
  LOGINUSER(NotesConstants.kLoginUserCmdId),
  LOGOUTUSER(NotesConstants.kLogoutUserCmdId),
  OPENIMAGERESOURCE(NotesConstants.kOpenImageResourceCmdId),
  OPENIMAGE(NotesConstants.kOpenImageCmdId),
  COPYTOFOLDER(NotesConstants.kCopyToFolderCmdId),
  MOVETOFOLDER(NotesConstants.kMoveToFolderCmdId),
  REMOVEFROMFOLDER(NotesConstants.kRemoveFromFolderCmdId),
  UNDELETEDOCUMENTS(NotesConstants.kUndeleteDocumentsCmdId),
  REDIRECT(NotesConstants.kRedirectCmdId),
  GETORBCOOKIE(NotesConstants.kGetOrbCookieCmdId),
  OPENCSSRESOURCE(NotesConstants.kOpenCssResourceCmdId),
  OPENFILERESOURCE(NotesConstants.kOpenFileResourceCmdId),
  OPENJAVASCRIPTLIB(NotesConstants.kOpenJavascriptLibCmdId),
  UNIMPLEMENTED(NotesConstants.kUnImplemented_01),
  CHANGEPASSWORD(NotesConstants.kChangePasswordCmdId),
  OPENPREFERENCES(NotesConstants.kOpenPreferencesCmdId),
  OPENWEBSERVICE(NotesConstants.kOpenWebServiceCmdId),
  WSDL(NotesConstants.kWsdlCmdId),
  GETIMAGE(NotesConstants.kGetImageCmdId);

  private static Map<Integer, CommandId> idsByIntValue = new HashMap<>();

  static {
    for (final CommandId currId : CommandId.values()) {
      CommandId.idsByIntValue.put(currId.m_val, currId);
    }
  }

  public static CommandId getCommandId(final int intVal) {
    final CommandId type = CommandId.idsByIntValue.get(intVal);
    if (type == null) {
      throw new IllegalArgumentException(MessageFormat.format("Unknown int value: {0}", intVal));
    }
    return type;
  }

  int m_val;

  CommandId(final int type) {
    this.m_val = type;
  }

  @Override
  public long getLongValue() {
    return this.m_val;
  }

  @Override
  public Integer getValue() {
    return this.m_val;
  }
}