package com.hcl.domino.commons.design;

import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.SharedField;
import com.hcl.domino.misc.NotesConstants;

public class SharedFieldImpl extends AbstractNamedDesignElement<SharedField> implements SharedField {

  public SharedFieldImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {

  }

  @Override
  public List<?> getFieldBody() {
    return DesignUtil.encapsulateRichTextBody(getDocument(), NotesConstants.ITEM_NAME_TEMPLATE);
  }

  @Override
  public String getLotusScript() {
    StringBuilder result = new StringBuilder();
    getDocument().forEachItem("$$" + getTitle(), (item, loop) -> { //$NON-NLS-1$
      result.append(item.get(String.class, "")); //$NON-NLS-1$
    });
    return result.toString();
  }
}
