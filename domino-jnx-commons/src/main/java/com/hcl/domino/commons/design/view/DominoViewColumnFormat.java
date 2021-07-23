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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.ViewColumnFormat;
import com.hcl.domino.design.format.ViewColumnFormat2;
import com.hcl.domino.design.format.ViewColumnFormat3;
import com.hcl.domino.design.format.ViewColumnFormat4;
import com.hcl.domino.design.format.ViewColumnFormat5;
import com.hcl.domino.design.format.ViewColumnFormat6;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.richtext.records.CDResource;

/**
 * @author Jesse Gallagher
 * @since 1.0.27
 */
public class DominoViewColumnFormat implements IAdaptable, CollectionColumn {
	private final int index;
	private int columnValuesIndex;
	private ViewColumnFormat format1;
	private ViewColumnFormat2 format2;
	private ViewColumnFormat3 format3;
	private ViewColumnFormat4 format4;
	private ViewColumnFormat5 format5;
	private ViewColumnFormat6 format6;
	private byte[] hideWhenFormula;
	private CDResource twistie;
	private String sharedColumnName;
	
	public DominoViewColumnFormat(int index) {
		this.index = index;
	}

	@Override
	public String getItemName() {
		return getFormat1().getItemName();
	}

	@Override
	public String getTitle() {
		return getFormat1().getTitle();
	}
	
	@Override
	public int getPosition() {
		return this.index;
	}

	@Override
	public int getColumnValuesIndex() {
		return this.columnValuesIndex;
	}

	@Override
	public boolean isConstant() {
		return getFormat1().getConstantValueLength() > 0;
	}

	@Override
	public String getFormula() {
		return getFormat1().getFormula();
	}
	
	@Override
	public SortConfiguration getSortConfiguration() {
		return new SortConfigurationImpl(this);
	}

	@Override
	public boolean isHidden() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.Hidden);
	}

	@Override
	public boolean isResponse() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.Response);
	}

	@Override
	public boolean isIcon() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.Icon);
	}

	@Override
	public boolean isResize() {
		return !getFormat1().getFlags().contains(ViewColumnFormat.Flag.NoResize);
	}

	@Override
	public boolean isShowTwistie() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.Twistie);
	}
	
	@Override
	public boolean isUseHideWhen() {
		return getFormat2().getFlags().contains(ViewColumnFormat2.Flag3.HideWhenFormula);
	}
	
	@Override
	public String getHideWhenFormula() {
		byte[] compiled = this.hideWhenFormula;
		if(compiled == null || compiled.length == 0) {
			return ""; //$NON-NLS-1$
		} else {
			return FormulaCompiler.get().decompile(compiled);
		}
	}
	
	@Override
	public TotalType getTotalType() {
		switch(getFormat1().getTotalType()) {
		case AVG_PER_CHILD:
			return TotalType.AveragePerSubcategory;
		case AVG_PER_ENTRY:
			return TotalType.Average;
		case PCT_OVERALL:
			return TotalType.Percent;
		case PCT_PARENT:
			return TotalType.PercentOfParentCategory;
		case TOTAL:
			return TotalType.Total;
		case NONE:
		default:
			return TotalType.None;
		}
	}
	
	@Override
	public ViewColumnFormat.ListDelimiter getListDisplayDelimiter() {
		return getFormat1().getListDelimiter();
	}

	@Override
	public boolean isHideDetailRows() {
		return getFormat1().getFlags().contains(ViewColumnFormat.Flag.HideDetail);
	}
	
	@Override
	public int getDisplayWidth() {
		return getFormat1().getDisplayWidth();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> clazz) {
		if(ViewColumnFormat.class == clazz) {
			return (T)this.format1;
		} else if(ViewColumnFormat2.class == clazz) {
			return (T)this.format2;
		} else if(ViewColumnFormat3.class == clazz) {
			return (T)this.format3;
		} else if(ViewColumnFormat4.class == clazz) {
			return (T)this.format4;
		} else if(ViewColumnFormat5.class == clazz) {
			return (T)this.format5;
		} else if(ViewColumnFormat6.class == clazz) {
			return (T)this.format6;
		}
		return null;
	}
	
	// *******************************************************************************
	// * Format-reader hooks
	// *******************************************************************************
	
	public void read(ViewColumnFormat format1) {
		this.format1 = format1;
	}
	
	public void read(ViewColumnFormat2 format2) {
		this.format2 = format2;
	}
	
	public void read(ViewColumnFormat3 format3) {
		this.format3 = format3;
	}
	
	public void read(ViewColumnFormat4 format4) {
		this.format4 = format4;
	}
	
	public void read(ViewColumnFormat5 format5) {
		this.format5 = format5;
	}
	
	public void read(ViewColumnFormat6 format6) {
		this.format6 = format6;
	}
	
	public void readColumnValuesIndex(int columnValuesIndex) {
		this.columnValuesIndex = columnValuesIndex;
	}
	
	public void readHideWhenFormula(byte[] formula) {
		this.hideWhenFormula = formula;
	}
	
	public void readTwistie(CDResource resource) {
		this.twistie = resource;
	}
	
	public void readSharedColumnName(String name) {
		this.sharedColumnName = name;
	}
	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************
	
	private ViewColumnFormat getFormat1() {
		return Objects.requireNonNull(this.format1, "VIEW_COLUMN_FORMAT not read");
	}
	private ViewColumnFormat2 getFormat2() {
		return Objects.requireNonNull(this.format2, "VIEW_COLUMN_FORMAT2 not read");
	}
}
