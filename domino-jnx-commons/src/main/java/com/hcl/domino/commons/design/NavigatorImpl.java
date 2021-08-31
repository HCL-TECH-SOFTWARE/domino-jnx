package com.hcl.domino.commons.design;

import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Navigator;
import com.hcl.domino.richtext.records.RecordType;
import com.hcl.domino.richtext.records.RichTextRecord;

public class NavigatorImpl extends AbstractDesignElement<Navigator> implements Navigator, IDefaultNamedDesignElement {

  public NavigatorImpl(Document doc) {
    super(doc);
  }
  
  public List<RichTextRecord<?>> getViewMapDataset() {
    return getDocument().getRichTextItem(DesignConstants.NAVIGATOR_VIEWMAP_DATASET_ITEM, RecordType.Area.TYPE_VIEWMAP);
  }

  public List<RichTextRecord<?>> getViewMapLayout() {
    return getDocument().getRichTextItem(DesignConstants.NAVIGATOR_VIEWMAP_LAYOUT_ITEM, RecordType.Area.TYPE_VIEWMAP);
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("G3w"); //$NON-NLS-1$
  }


}
