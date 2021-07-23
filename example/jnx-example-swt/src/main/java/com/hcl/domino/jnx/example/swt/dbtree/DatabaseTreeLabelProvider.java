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
package com.hcl.domino.jnx.example.swt.dbtree;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class DatabaseTreeLabelProvider extends LabelProvider implements ILabelProvider, ITableColorProvider {

	@Override
	public String getText(Object element) {
		return String.valueOf(element);
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof DBListTreeNode) {
			return ((DBListTreeNode)element).getImage();
		}
		return super.getImage(element);
	}
	
	@Override
	public Color getBackground(Object var1, int var2) {
		return null;
	}

	@Override
	public Color getForeground(Object var1, int var2) {
		return null;
	}

}
