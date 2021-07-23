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
package com.hcl.domino.commons.design.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat2;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.design.format.ViewTableFormat4;
import com.hcl.domino.richtext.records.CDResource;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class DominoViewFormat implements IAdaptable {
	private final List<DominoViewColumnFormat> columns = new ArrayList<>();
	
	private ViewTableFormat format1;
	private ViewTableFormat2 format2;
	private ViewTableFormat3 format3;
	private ViewTableFormat4 format4;
	private CDResource backgroundResource;
	
	public List<CollectionColumn> getColumns() {
		return Collections.unmodifiableList(columns);
	}
	
	public int getColumnCount() {
		ViewTableFormat format1 = Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
		return format1.getColumnCount();
	}
	
	public boolean isHierarchical() {
		return !getFlags1().contains(ViewTableFormat.Flag.FLATINDEX);
	}
	
	public boolean isConflict() {
		return getFlags1().contains(ViewTableFormat.Flag.CONFLICT);
	}

	public boolean isCollapsed() {
		return getFlags1().contains(ViewTableFormat.Flag.COLLAPSED);
	}
	
	public boolean isGotoTopOnOpen() {
		return getFlags1().contains(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN);
	}

	public boolean isGotoTopOnRefresh() {
		return getFlags1().contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH);
	}

	public boolean isGotoBottomOnOpen() {
		return getFlags1().contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN);
	}

	public boolean isGotoBottomOnRefresh() {
		return getFlags1().contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH);
	}

	public boolean isExtendLastColumn() {
		return getFlags1().contains(ViewTableFormat.Flag.EXTEND_LAST_COLUMN);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if(ViewTableFormat.class == clazz) {
			return (T)this.format1;
		} else if(ViewTableFormat2.class == clazz) {
			return (T)this.format2;
		} else if(ViewTableFormat3.class == clazz) {
			return (T)this.format3;
		} else if(ViewTableFormat4.class == clazz) {
			return (T)this.format4;
		}
		return null;
	}
	
	// *******************************************************************************
	// * Format-reader hooks
	// *******************************************************************************
	
	public void read(ViewTableFormat format1) {
		this.format1 = format1;
	}
	
	public void read(ViewTableFormat2 format2) {
		this.format2 = format2;
	}
	
	public void read(ViewTableFormat3 format3) {
		this.format3 = format3;
	}
	
	public void read(ViewTableFormat4 format4) {
		this.format4 = format4;
	}
	
	public void readBackgroundResource(CDResource resource) {
		this.backgroundResource = resource;
	}
	
	public DominoViewColumnFormat addColumn() {
		DominoViewColumnFormat col = new DominoViewColumnFormat(columns.size());
		columns.add(col);
		return col;
	}
	
	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************
	
	private Set<ViewTableFormat.Flag> getFlags1() {
		ViewTableFormat format1 = Objects.requireNonNull(this.format1, "VIEW_TABLE_FORMAT not read");
		Set<ViewTableFormat.Flag> flags = format1.getFlags();
		return flags;
	}
}
