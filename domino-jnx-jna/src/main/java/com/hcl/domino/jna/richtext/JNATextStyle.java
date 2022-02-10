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
package com.hcl.domino.jna.richtext;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.structs.NotesCompoundStyleStruct;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.TextStyle;

/**
 * Container for paragraph style attributes used in richtext items
 * 
 * @author Karsten Lehmann
 */
public class JNATextStyle implements TextStyle {
	private String m_styleName;
	
	/** paragraph justification type */
	private short m_justifyMode;
	/** Line spacing */
	private short m_lineSpacing;
	/** # units above paragraph */
	private short m_paragraphSpacingBefore;
	/** # units below paragraph */
	private short m_paragraphSpacingAfter;
	/** leftmost margin in twips */
	private short m_leftMargin;
	/** rightmost margin in twips */
	private short m_rightMargin;
	/** leftmost margin on first line */
	private short m_firstLineLeftMargin;
	/** # tab stops in table */
	private short m_tabs;
	/**
	 * table of tab stops<br>
	 * C type : signed short[20]
	 */
	private short[] m_tab = new short[20];
	/** paragraph attribute flags */
	private short m_flags;

	public JNATextStyle(String styleName) {
		Objects.requireNonNull(styleName, "Style name cannot be null");
		
		m_styleName = styleName;
		NotesCompoundStyleStruct style = NotesCompoundStyleStruct.newInstance();
		NotesCAPI.get().CompoundTextInitStyle(style);
		style.read();
		m_justifyMode = style.JustifyMode;
		m_lineSpacing = style.LineSpacing;
		m_paragraphSpacingBefore = style.ParagraphSpacingBefore;
		m_paragraphSpacingAfter = style.ParagraphSpacingAfter;
		m_leftMargin = style.LeftMargin;
		m_rightMargin = style.RightMargin;
		m_firstLineLeftMargin = style.FirstLineLeftMargin;
		m_tabs = style.Tabs;
		m_tab = style.Tab==null ? null : style.Tab.clone();
		m_flags = style.Flags;
		//unlink hide flags so that we can set hide when for preview
		m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_UNLINK) & 0xffff);
	}
	
	@Override
	public String getName() {
		return m_styleName;
	}
	
	@Override
	public TextStyle setAlign(Justify align) {
		m_justifyMode = align.getConstant();
		return this;
	}
	
	@Override
	public Justify getAlign() {
		switch (m_justifyMode) {
		case NotesConstants.JUSTIFY_LEFT:
			return Justify.LEFT;
		case NotesConstants.JUSTIFY_RIGHT:
			return Justify.RIGHT;
		case NotesConstants.JUSTIFY_BLOCK:
			return Justify.BLOCK;
		case NotesConstants.JUSTIFY_CENTER:
			return Justify.CENTER;
		case NotesConstants.JUSTIFY_NONE:
			return Justify.NONE;
		default:
			return Justify.LEFT;
		}
	}
	
	@Override
	public int getLineSpacing() {
		return m_lineSpacing & 0xffff;
	}
	
	@Override
	public JNATextStyle setLineSpacing(int spacing) {
		if (spacing<0 || spacing>65535) {
			throw new IllegalArgumentException("Value must be between 0 and 65535");
		}
		m_lineSpacing = (short) (spacing & 0xffff);
		return this;
	}
	
	@Override
	public int getParagraphSpacingBefore() {
		return m_paragraphSpacingBefore & 0xffff;
	}
	
	@Override
	public TextStyle setParagraphSpacingBefore(int spacing) {
		if (spacing<0 || spacing>65535) {
			throw new IllegalArgumentException("Value must be between 0 and 65535");
		}
		m_paragraphSpacingBefore = (short) (spacing & 0xffff);
		return this;
	}

	@Override
	public int getParagraphSpacingAfter() {
		return m_paragraphSpacingAfter& 0xffff;
	}

	@Override
	public TextStyle setParagraphSpacingAfter(int spacing) {
		if (spacing<0 || spacing>65535) {
			throw new IllegalArgumentException("Value must be between 0 and 65535");
		}
		m_paragraphSpacingAfter = (short) (spacing & 0xffff);
		return this;
	}

	@Override
	public double getLeftMargin() {
		 //there are 72 * 20 TWIPS to an inch
		return m_leftMargin / NotesConstants.ONEINCH;
	}
	
	@Override
	public TextStyle setLeftMargin(double margin) {
		double result = margin * NotesConstants.ONEINCH;
		if (result < 0 || result > 65535) {
			throw new IllegalArgumentException(MessageFormat.format("Value must be between 0 and {0}", (65535/NotesConstants.ONEINCH)));
		}
		m_leftMargin = (short) result;
		return this;
	}
	
	@Override
	public double getRightMargin() {
		 //there are 72 * 20 TWIPS to an inch
		return m_rightMargin / NotesConstants.ONEINCH;
	}
	
	@Override
	public TextStyle setRightMargin(double margin) {
		double result = margin * NotesConstants.ONEINCH;
		if (result < 0 || result > 65535) {
			throw new IllegalArgumentException(MessageFormat.format("Value must be between 0 and {0}", (65535/NotesConstants.ONEINCH)));
		}
		m_rightMargin = (short) result;
		return this;
	}

	@Override
	public double getFirstLineLeftMargin() {
		 //there are 72 * 20 TWIPS to an inch
		return m_firstLineLeftMargin / NotesConstants.ONEINCH;
	}
	
	@Override
	public TextStyle setFirstLineLeftMargin(double margin) {
		double result = margin * NotesConstants.ONEINCH;
		if (result < 0 || result > 65535) {
			throw new IllegalArgumentException(MessageFormat.format("Value must be between 0 and {0}", (65535/NotesConstants.ONEINCH)));
		}
		m_firstLineLeftMargin = (short) result;
		return this;
	}

	@Override
	public int getTabsInTable() {
		return m_tabs & 0xffff;
	}
	
	@Override
	public TextStyle setTabsInTable(int tabs) {
		if (tabs<0 || tabs>65535) {
			throw new IllegalArgumentException("Value must be between 0 and 65535");
		}
		m_tabs = (short) (tabs & 0xffff);
		return this;
	}
	
	@Override
	public TextStyle setTabPositions(short[] tabPos) {
		m_tab = new short[20];
		for (int i=0; i<20; i++) {
			if (tabPos.length>=i) {
				m_tab[i] = tabPos[i];
			}
			else {
				m_tab[i] = tabPos[tabPos.length-1];
			}
		}
		return this;
	}
	
	@Override
	public short[] getTabPositions() {
		return m_tab.clone();
	}
	
	@Override
	public TextStyle setPaginateBefore(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_PAGINATE_BEFORE) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_PAGINATE_BEFORE) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isPaginateBefore() {
		return (m_flags & NotesConstants.PABFLAG_PAGINATE_BEFORE) == NotesConstants.PABFLAG_PAGINATE_BEFORE;
	}
	
	@Override
	public TextStyle setKeepWithNext(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_KEEP_WITH_NEXT) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_KEEP_WITH_NEXT) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isKeepWithNext() {
		return (m_flags & NotesConstants.PABFLAG_KEEP_WITH_NEXT) == NotesConstants.PABFLAG_KEEP_WITH_NEXT;
	}

	@Override
	public TextStyle setKeepTogether(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_KEEP_TOGETHER) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_KEEP_TOGETHER) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isKeepTogether() {
		return (m_flags & NotesConstants.PABFLAG_KEEP_TOGETHER) == NotesConstants.PABFLAG_KEEP_TOGETHER;
	}

	@Override
	public TextStyle setHideReadOnly(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_RO) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_RO) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideReadOnly() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_RO) == NotesConstants.PABFLAG_HIDE_RO;
	}
	
	@Override
	public TextStyle setHideReadWrite(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_RW) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_RW) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideReadWrite() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_RW) == NotesConstants.PABFLAG_HIDE_RW;
	}
	
	@Override
	public TextStyle setHideWhenPrinting(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_PR) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_PR) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideWhenPrinting() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_PR) == NotesConstants.PABFLAG_HIDE_PR;
	}
	
	@Override
	public TextStyle setHideWhenCopied(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_CO) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_CO) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideWhenCopied() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_CO) == NotesConstants.PABFLAG_HIDE_CO;
	}

	@Override
	public TextStyle setHideWhenPreviewed(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_PV) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_PV) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideWhenEditedInPreview() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_PV) == NotesConstants.PABFLAG_HIDE_PV;
	}

	@Override
	public TextStyle setHideWhenEditedInPreview(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_HIDE_PVE) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_HIDE_PVE) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isHideWhenPreviewed() {
		return (m_flags & NotesConstants.PABFLAG_HIDE_PVE) == NotesConstants.PABFLAG_HIDE_PVE;
	}

	@Override
	public TextStyle setDisplayAsNumberedList(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_NUMBEREDLIST) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_NUMBEREDLIST) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isDisplayAsNumberedList() {
		return (m_flags & NotesConstants.PABFLAG_NUMBEREDLIST) == NotesConstants.PABFLAG_NUMBEREDLIST;
	}

	@Override
	public TextStyle setDisplayAsBulletList(boolean b) {
		if (b) {
			m_flags = (short) ((m_flags | NotesConstants.PABFLAG_BULLET) & 0xffff);
		}
		else {
			m_flags = (short) ((m_flags & ~NotesConstants.PABFLAG_BULLET) & 0xffff);
		}
		return this;
	}

	@Override
	public boolean isDisplayAsBulletList() {
		return (m_flags & NotesConstants.PABFLAG_BULLET) == NotesConstants.PABFLAG_BULLET;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(m_firstLineLeftMargin, m_flags, m_justifyMode, m_leftMargin, m_lineSpacing, m_paragraphSpacingAfter, m_paragraphSpacingBefore, m_rightMargin, m_styleName, Arrays.hashCode(m_tab),
				m_tabs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JNATextStyle other = (JNATextStyle) obj;
		if (m_firstLineLeftMargin != other.m_firstLineLeftMargin) {
			return false;
		}
		if (m_flags != other.m_flags) {
			return false;
		}
		if (m_justifyMode != other.m_justifyMode) {
			return false;
		}
		if (m_leftMargin != other.m_leftMargin) {
			return false;
		}
		if (m_lineSpacing != other.m_lineSpacing) {
			return false;
		}
		if (m_paragraphSpacingAfter != other.m_paragraphSpacingAfter) {
			return false;
		}
		if (m_paragraphSpacingBefore != other.m_paragraphSpacingBefore) {
			return false;
		}
		if (m_rightMargin != other.m_rightMargin) {
			return false;
		}
		if (!Objects.equals(m_styleName, other.m_styleName)) {
			return false;
		}
		if (!Arrays.equals(m_tab, other.m_tab)) {
			return false;
		}
		if (m_tabs != other.m_tabs) {
			return false;
		}
		return true;
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> clazz) {
		if (clazz==NotesCompoundStyleStruct.class) {
			NotesCompoundStyleStruct styleStruct = NotesCompoundStyleStruct.newInstance();
			NotesCAPI.get().CompoundTextInitStyle(styleStruct);
			styleStruct.read();
			styleStruct.JustifyMode = m_justifyMode;
			styleStruct.LineSpacing = m_lineSpacing;
			styleStruct.ParagraphSpacingBefore = m_paragraphSpacingBefore;
			styleStruct.ParagraphSpacingAfter = m_paragraphSpacingAfter;
			styleStruct.LeftMargin = m_leftMargin;
			styleStruct.RightMargin = m_rightMargin;
			styleStruct.FirstLineLeftMargin = m_firstLineLeftMargin;
			styleStruct.Tabs = m_tabs;
			styleStruct.Tab = m_tab==null ? null : m_tab.clone();
			styleStruct.Flags = m_flags;
			styleStruct.write();
			return (T) styleStruct;
		} else {
			return null;
		}
	}
}
