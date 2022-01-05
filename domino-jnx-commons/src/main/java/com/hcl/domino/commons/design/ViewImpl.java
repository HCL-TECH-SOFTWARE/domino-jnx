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

import java.util.List;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Formula;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.View;
import com.hcl.domino.design.simplesearch.SimpleSearchTerm;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.records.RecordType;

/**
 * @since 1.0.18
 */
public class ViewImpl extends AbstractCollectionDesignElement<View> implements View {

  public ViewImpl(final Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("Y"); //$NON-NLS-1$
  }

  @Override
  public String getSelectionFormula() {
    return getDocument().get(DesignConstants.VIEW_FORMULA_ITEM, String.class, ""); //$NON-NLS-1$
  }

  @Override
  public View setSelectionFormula(final String selectionFormula) {
    Formula formula = getDocument().getParentDatabase().getParentDominoClient().createFormula(selectionFormula);
    getDocument().replaceItemValue(DesignConstants.VIEW_FORMULA_ITEM, formula);
    return this;
  }
  
  @Override
  public List<? extends SimpleSearchTerm> getDocumentSelection() {
    return DesignUtil.toSimpleSearch(getDocument().getRichTextItem(NotesConstants.VIEW_SELQUERY_ITEM, RecordType.Area.TYPE_QUERY));
  }

  @Override
  public View setFormulaClass(String formulaClass) {
    getDocument().replaceItemValue(DesignConstants.VIEW_CLASSES_ITEM, formulaClass);
    return this;
  }
  
  @Override
  public String getFormulaClass() {
    return getDocument().getAsText(DesignConstants.VIEW_CLASSES_ITEM, ' ');
  }
}
