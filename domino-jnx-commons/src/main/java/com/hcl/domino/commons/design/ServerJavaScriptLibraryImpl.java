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
package com.hcl.domino.commons.design;

import java.nio.charset.Charset;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.ServerJavaScriptLibrary;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.process.GetJavaScriptDataProcessor;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
public class ServerJavaScriptLibraryImpl extends AbstractScriptLibrary<ServerJavaScriptLibrary> implements ServerJavaScriptLibrary {

	public ServerJavaScriptLibraryImpl(Document doc) {
		super(doc);
	}

	@Override
	public String getScript() {
		// There appears to always be a trailing null
		byte[] data = GetJavaScriptDataProcessor.instance.apply(getDocument().getRichTextItem(NotesConstants.SERVER_JAVASCRIPTLIBRARY_CODE));
		return new String(data, 0, data.length-1, Charset.forName("LMBCS")); //$NON-NLS-1$
	}
}
