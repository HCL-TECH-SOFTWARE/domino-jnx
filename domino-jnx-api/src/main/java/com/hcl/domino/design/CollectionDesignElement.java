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
package com.hcl.domino.design;

import java.util.List;

import com.hcl.domino.data.CollectionColumn;

/**
 * Describes a collection design element, i.e. a view or folder
 */
public interface CollectionDesignElement extends DesignElement.NamedDesignElement, DesignElement.XPageAlternativeElement {

	public enum OnOpen {
		GOTO_LAST_OPENED,
		GOTO_TOP,
		GOTO_BOTTOM
	}
    public enum OnRefresh {
		DISPLAY_INDICATOR,
		REFRESH_DISPLAY,
		REFRESH_FROM_TOP,
		REFRESH_FROM_BOTTOM
	}

	CollectionDesignElement addColumn();

	List<CollectionColumn> getColumns();

	CollectionDesignElement removeColumn(CollectionColumn column);

	CollectionDesignElement swapColumns(int a, int b);

	CollectionDesignElement swapColumns(CollectionColumn a, CollectionColumn b);

	com.hcl.domino.data.DominoCollection getCollection();

	OnOpen getOnOpenUISetting();
	
	OnRefresh getOnRefreshUISetting();

	CollectionDesignElement setOnRefreshUISetting(OnRefresh onRefreshUISetting);
	
	boolean isAllowCustomizations();
    
}