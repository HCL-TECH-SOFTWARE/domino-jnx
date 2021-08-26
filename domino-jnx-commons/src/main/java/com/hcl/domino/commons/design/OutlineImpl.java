package com.hcl.domino.commons.design;

import java.util.List;
import com.hcl.domino.commons.design.outline.DominoOutlineFormat;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Outline;

public class OutlineImpl extends AbstractDesignElement<Outline> implements Outline, IDefaultNamedDesignElement {

  public OutlineImpl(Document doc) {
    super(doc);
  }
  
  @Override
  public List<IAdaptable> getSitemapList() {
    return ((DominoOutlineFormat) getDocument().getItemValue(DesignConstants.OUTLINE_SITEMAPLIST_ITEM).get(0)).getOutlineEntries();
  }

  @Override
  public void setSitemapList(String sitemapList) {
    //throw new NotYetImplementedException();
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("m"); //$NON-NLS-1$
  }

}
