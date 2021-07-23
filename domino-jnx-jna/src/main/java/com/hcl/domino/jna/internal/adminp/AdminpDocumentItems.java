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
package com.hcl.domino.jna.internal.adminp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;

public class AdminpDocumentItems {
	private Database m_dbProxy;
	private JNADocument m_adminReqProfileDoc;
	private List<String> m_itemNames;
	
	public AdminpDocumentItems(JNADatabase dbProxy) {
		m_dbProxy = dbProxy;
		m_adminReqProfileDoc = (JNADocument) m_dbProxy.createDocument();
		m_itemNames = new ArrayList<>();
	}
	
	public void add(String itemName, Object value) {
		if (m_itemNames.size()==10) {
			throw new IllegalArgumentException("List exceeds max allowed size");
		}
		
		m_adminReqProfileDoc.replaceItemValue(itemName, value);
		m_itemNames.add(itemName);
	}
	
	public boolean isMatch(Document doc) {
		for (String currItemName : m_itemNames) {
			Optional<Item> currDocItmOpt = doc.getFirstItem(currItemName);
			Optional<Item> currFilterItmOpt = m_adminReqProfileDoc.getFirstItem(currItemName);
			
			if (currDocItmOpt.isPresent() && currFilterItmOpt.isPresent()) {
				Item currDocItm = currDocItmOpt.get();
				Item currFilterItm = currFilterItmOpt.get();
				
				if (currFilterItm.getType()==ItemDataType.TYPE_NOTEREF_LIST) {
					if (currDocItm.getType()==ItemDataType.TYPE_NOTEREF_LIST) {
						List<?> currDocItmValues = doc.getItemValue(currItemName);
						List<?> currFilterItmValues = m_adminReqProfileDoc.getItemValue(currItemName);
						if (!currDocItmValues.equals(currFilterItmValues)) {
							return false;
						}
					}
					else {
						return false;
					}
				}
				else {
					List<?> currDocItmValues = doc.getItemValue(currItemName);
					List<?> currFilterItmValues = m_adminReqProfileDoc.getItemValue(currItemName);
					if (!currDocItmValues.equals(currFilterItmValues)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
}
