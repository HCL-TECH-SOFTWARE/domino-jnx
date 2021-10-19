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
package com.hcl.domino.jnx.example.swt.info;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractInfoPane extends Composite {

  public AbstractInfoPane(final Composite parent, final String title) {
    super(parent, SWT.NONE);

    this.setLayout(new GridLayout(2, false));

    if (title != null && !title.isEmpty()) {
      final Label titleLabel = new Label(this, SWT.NONE);
      titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
      final FontDescriptor titleFont = FontDescriptor.createFrom(titleLabel.getFont())
          .setStyle(SWT.BOLD)
          .increaseHeight(2);
      titleLabel.setFont(titleFont.createFont(titleLabel.getDisplay()));
      titleLabel.setText(title);

      final Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
      separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    }
  }

  protected void info(final String labelText, final Object value) {
    final Label label = new Label(this, SWT.NONE);
    label.setText(labelText + (labelText.endsWith(":") ? "" : ":")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final Text text = new Text(this, SWT.NONE);
    text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
    text.setEditable(false);
    text.setBackground(new Color(this.getDisplay(), 0, 0, 0, 0));
    text.clearSelection();
  }
}
