package com.hcl.domino.commons.design;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hcl.domino.design.DesignElement;
import com.hcl.domino.misc.NotesConstants;

public interface IDefaultNamedDesignElement extends DesignElement.NamedDesignElement {
  @Override
  default List<String> getAliases() {
    final String title = this.getDocument().getAsText(NotesConstants.FIELD_TITLE, '|');
    final int barIndex = title.indexOf('|');
    if (barIndex < 0) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(title.substring(barIndex + 1).split("\\|")); //$NON-NLS-1$
    }
  }

  @Override
  default String getTitle() {
    final String title = this.getDocument().getAsText(NotesConstants.FIELD_TITLE, '|');
    final int barIndex = title.indexOf('|');
    if (barIndex < 0) {
      return title;
    } else {
      return title.substring(0, barIndex);
    }
  }

  @Override
  default void setTitle(final String... title) {
    this.getDocument().replaceItemValue(NotesConstants.FIELD_TITLE, title);
  }
}
