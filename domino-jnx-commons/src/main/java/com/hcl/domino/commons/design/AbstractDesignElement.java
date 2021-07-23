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

import java.util.Collection;

import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.misc.NotesConstants;

/**
 * @param <T> the {@link DesignElement} interface implemented by the class
 * @since 1.0.18
 */
public abstract class AbstractDesignElement<T extends DesignElement> implements DesignElement {
	private final Document doc;
	
	public AbstractDesignElement(Document doc) {
		this.doc = doc;
	}

	@Override
	public boolean isProhibitRefresh() {
		return getFlags().contains(NotesConstants.DESIGN_FLAG_PRESERVE);
	}

	@Override
	public void setProhibitRefresh(boolean prohibitRefresh) {
		setFlag(NotesConstants.DESIGN_FLAG_PRESERVE, prohibitRefresh);
	}

	@Override
	public boolean isHideFromWeb() {
		return getFlags().contains(NotesConstants.DESIGN_FLAG_HIDE_FROM_WEB);
	}

	@Override
	public void setHideFromWeb(boolean hideFromWeb) {
		setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_WEB, hideFromWeb);
	}

	@Override
	public boolean isHideFromNotes() {
		return getFlags().contains(NotesConstants.DESIGN_FLAG_HIDE_FROM_NOTES);
	}

	@Override
	public void setHideFromNotes(boolean hideFromNotes) {
		setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_NOTES, hideFromNotes);
	}

	@Override
	public boolean isHideFromMobile() {
		return getFlags().contains(NotesConstants.DESIGN_FLAG_HIDE_FROM_MOBILE);
	}

	@Override
	public void setHideFromMobile(boolean hideFromMobile) {
		setFlag(NotesConstants.DESIGN_FLAG_HIDE_FROM_MOBILE, hideFromMobile);
	}

	@Override
	public void sign() {
		doc.sign();
	}

	@Override
	public void sign(UserId id) {
		doc.sign(id, true);
	}

	@Override
	public boolean save() {
		doc.save();
		// TODO figure out if this should do something else or if the method signature should change
		return true;
	}

	@Override
	public Collection<String> getItemNames() {
		return doc.getItemNames();
	}

	@Override
	public String getDesignerVersion() {
		return doc.getAsText(DesignConstants.DESIGNER_VERSION, ' ');
	}
	
	@Override
	public String getComment() {
		return doc.getAsText(NotesConstants.FILTER_COMMENT_ITEM, ' ');
	}
	
	@Override
	public void setComment(String comment) {
		doc.replaceItemValue(NotesConstants.FILTER_COMMENT_ITEM, comment);
	}
	
	@Override
	public Document getDocument() {
		return doc;
	}
	
	// *******************************************************************************
	// * Implementation utility methods
	// *******************************************************************************
	
	public String getFlags() {
		return getDocument().getAsText(NotesConstants.DESIGN_FLAGS, ' ');
	}
	
	public void setFlags(String flags) {
		getDocument().replaceItemValue(NotesConstants.DESIGN_FLAGS, flags);
	}
	
	public void setFlag(String flagConstant, boolean value) {
		String flags = getFlags();
		if(value && !flags.contains(flagConstant)) {
			setFlags(flags + flagConstant);
		} else if(!value && flags.contains(flagConstant)) {
			setFlags(flags.replace(flagConstant, "")); //$NON-NLS-1$
		}
	}
	
	/**
	 * Initializes the default values for a newly-created design note, such as {@code "$Flags"}.
	 */
	public abstract void initializeNewDesignNote();
}
