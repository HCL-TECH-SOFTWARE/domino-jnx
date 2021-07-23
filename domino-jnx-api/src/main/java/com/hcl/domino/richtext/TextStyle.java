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
package com.hcl.domino.richtext;

import com.hcl.domino.data.IAdaptable;

public interface TextStyle extends IAdaptable {
	public enum Justify {
		/** flush left, ragged right */
		LEFT ((short) 0),
		/** flush right, ragged left */
		RIGHT((short) 1),
		/** full block justification */
		BLOCK((short) 2),
		/** centered */
		CENTER((short) 3),
		/** no line wrapping AT ALL (except hard CRs) */
		NONE((short) 4);
		
		private short m_constant;
		
		Justify(short constant) {
			m_constant = constant;
		}
		
		public short getConstant() {
			return m_constant;
		}
	}

	String getName();
	
	TextStyle setAlign(Justify align);

	Justify getAlign();
	
	int getLineSpacing();
	
	TextStyle setLineSpacing(int spacing);
	
	int getParagraphSpacingBefore();
	
	TextStyle setParagraphSpacingBefore(int spacing);
	
	int getParagraphSpacingAfter();
	
	TextStyle setParagraphSpacingAfter(int spacing);
	
	double getLeftMargin();
	
	TextStyle setLeftMargin(double margin);
	
	double getRightMargin();
	
	TextStyle setRightMargin(double margin);
	
	double getFirstLineLeftMargin();
	
	TextStyle setFirstLineLeftMargin(double margin);
	
	int getTabsInTable();
	
	TextStyle setTabsInTable(int tabs);
	
	TextStyle setTabPositions(short[] tabPos);
	
	short[] getTabPositions();
	
	TextStyle setPaginateBefore(boolean b);
	
	boolean isPaginateBefore();
	
	TextStyle setKeepWithNext(boolean b);
	
	boolean isKeepWithNext();
	
	TextStyle setKeepTogether(boolean b);
	
	boolean isKeepTogether();
	
	TextStyle setHideReadOnly(boolean b);
	
	boolean isHideReadOnly();
	
	TextStyle setHideReadWrite(boolean b);
	
	boolean isHideReadWrite();

	TextStyle setHideWhenPrinting(boolean b);
	
	boolean isHideWhenPrinting();
	
	TextStyle setHideWhenCopied(boolean b);
	
	boolean isHideWhenCopied();
	
	TextStyle setHideWhenPreviewed(boolean b);
	
	boolean isHideWhenEditedInPreview();
	
	TextStyle setHideWhenEditedInPreview(boolean b);
	
	boolean isHideWhenPreviewed();
	
	TextStyle setDisplayAsNumberedList(boolean b);
	
	boolean isDisplayAsNumberedList();
	
	TextStyle setDisplayAsBulletList(boolean b);
	
	boolean isDisplayAsBulletList();
	
}
