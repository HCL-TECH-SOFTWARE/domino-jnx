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
package com.hcl.domino.jnx.example.swt.doc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jnx.example.swt.bean.DominoContextBean;
import com.hcl.domino.jnx.example.swt.info.AbstractInfoPane;

public class ItemInfoPane extends AbstractInfoPane {

  public ItemInfoPane(Composite parent, Item item) {
    super(parent, item.getName());
    
    DominoContextBean.submit(client -> {
      String name = item.getName();
      ItemDataType type = item.getType();
      int len = item.getValueLength();
      String val = item.getValue().toString();
      
      getDisplay().asyncExec(() -> {

        info("Name", name);
        info("Type", type);
        info("Size", len);
        
        Text value = new Text(this, SWT.NONE | SWT.MULTI | SWT.WRAP);
        value.setEditable(false);
        value.setText(val);
        value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        
        layout();
      });
    });
  }

}
