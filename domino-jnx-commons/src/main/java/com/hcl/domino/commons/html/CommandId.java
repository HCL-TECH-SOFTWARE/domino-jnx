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

	int m_val;

	private static Map<Integer, CommandId> idsByIntValue = new HashMap<>();

	static {
		for (CommandId currId : CommandId.values()) {
			idsByIntValue.put(currId.m_val, currId);
		}
	}

	CommandId(int type) {
		m_val = type;
	}

	@Override
	public Integer getValue() {
		return m_val;
	}

	public static CommandId getCommandId(int intVal) {
		CommandId type = idsByIntValue.get(intVal);
		if (type==null) {
			throw new IllegalArgumentException(MessageFormat.format("Unknown int value: {0}", intVal));
		}
		return type;
	}

	@Override
	public long getLongValue() {
		return m_val;
	}
};