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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.design.view.DominoViewFormat;
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.DesignElement;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewTableFormat;
import com.hcl.domino.design.format.ViewTableFormat3;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;

/**
 * @param <T> the {@link DesignElement} interface implemented by the class
 * @since 1.0.18
 */
public abstract class AbstractCollectionDesignElement<T extends CollectionDesignElement> extends AbstractNamedDesignElement<T>
    implements CollectionDesignElement {
  private DominoViewFormat format;

  public AbstractCollectionDesignElement(final Document doc) {
    super(doc);
  }

  @Override
  public CollectionDesignElement addColumn() {
    throw new NotYetImplementedException();
  }

  @Override
  public DominoCollection getCollection() {
    throw new NotYetImplementedException();
  }

  @Override
  public List<CollectionColumn> getColumns() {
    return this.readViewFormat().getColumns();
  }

  @Override
  public OnOpen getOnOpenUISetting() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat format1 = format.getAdapter(ViewTableFormat.class);
    final Set<ViewTableFormat.Flag> flags = format1.getFlags();
    if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_OPEN)) {
      return OnOpen.GOTO_BOTTOM;
    } else if (flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_OPEN)) {
      return OnOpen.GOTO_TOP;
    } else {
      return OnOpen.GOTO_LAST_OPENED;
    }
  }

  @Override
  public OnRefresh getOnRefreshUISetting() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat format1 = format.getAdapter(ViewTableFormat.class);
    final Set<ViewTableFormat.Flag> flags = format1.getFlags();
    // Auto-refresh is denoted by both flags being present
    if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH) && flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH)) {
      return OnRefresh.REFRESH_DISPLAY;
    } else if (flags.contains(ViewTableFormat.Flag.GOTO_BOTTOM_ON_REFRESH)) {
      return OnRefresh.REFRESH_FROM_BOTTOM;
    } else if (flags.contains(ViewTableFormat.Flag.GOTO_TOP_ON_REFRESH)) {
      return OnRefresh.REFRESH_FROM_TOP;
    } else {
      return OnRefresh.DISPLAY_INDICATOR;
    }
  }

  @Override
  public Optional<String> getWebXPageAlternative() {
    final String val = this.getDocument().get(DesignConstants.XPAGE_ALTERNATE, String.class, null);
    if (val == null || val.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(val);
    }
  }

  @Override
  public boolean isAllowCustomizations() {
    final DominoViewFormat format = this.readViewFormat();
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if (format3 == null) {
      return false;
    } else {
      // It appears that this flag is inverted in practice
      return !format3.getFlags().contains(ViewTableFormat3.Flag.AllowCustomizations);
    }
  }

  @Override
  public CollectionDesignElement removeColumn(final CollectionColumn column) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement setOnRefreshUISetting(final OnRefresh onRefreshUISetting) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement swapColumns(final CollectionColumn a, final CollectionColumn b) {
    throw new NotYetImplementedException();
  }

  @Override
  public CollectionDesignElement swapColumns(final int a, final int b) {
    throw new NotYetImplementedException();
  }
  
  @Override
  public ClassicThemeBehavior getClassicThemeBehavior() {
    final ViewTableFormat3 format3 = format.getAdapter(ViewTableFormat3.class);
    if(format3 == null) {
      return ClassicThemeBehavior.USE_DATABASE_SETTING;
    } else {
      byte themeSetting = (byte)format3.getThemeSetting();
      return DominoEnumUtil.valueOf(ClassicThemeBehavior.class, themeSetting)
        .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
    }
  }
  
  @Override
  public Style getStyle() {
    return readViewFormat().getAdapter(ViewCalendarFormat.class) == null ? Style.STANDARD_OUTLINE : Style.CALENDAR;
  }
  
  @Override
  public boolean isDefaultCollection() {
    return getDocument().getDocumentClass().contains(DocumentClass.DEFAULT);
  }
  
  @Override
  public boolean isDefaultCollectionDesign() {
    return getDocument().getAsText(NotesConstants.DESIGN_FLAGS , ' ').contains(NotesConstants.DESIGN_FLAG_DEFAULT_DESIGN);
  }
  
  @Override
  public boolean isCollapseAllOnFirstOpen() {
    return readViewFormat()
        .getAdapter(ViewTableFormat.class)
        .getFlags()
        .contains(ViewTableFormat.Flag.COLLAPSED);
  }
  
  @Override
  public boolean isShowResponseDocumentsInHierarchy() {
    // NB: This method reflects Designer's UI, which is inverted from the actual storage
    return !readViewFormat()
        .getAdapter(ViewTableFormat.class)
        .getFlags()
        .contains(ViewTableFormat.Flag.FLATINDEX);
  }
  
  @Override
  public boolean isShowInViewMenu() {
    return !getDocument().getAsText(NotesConstants.DESIGN_FLAGS , ' ').contains(NotesConstants.DESIGN_FLAG_NO_MENU);
  }

  // *******************************************************************************
  // * Internal utility methods
  // *******************************************************************************

  private synchronized DominoViewFormat readViewFormat() {
    if (this.format == null) {
      final Document doc = this.getDocument();
      this.format = (DominoViewFormat) doc.getItemValue(DesignConstants.VIEW_VIEW_FORMAT_ITEM).get(0);
    }
    return this.format;
  }
}
