package com.hcl.domino.commons.design;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Outline;

public class OutlineImpl extends AbstractNamedDesignElement<Outline> implements Outline {

  public OutlineImpl(Document doc) {
    super(doc);
  }

  @Override
  public String getSitemapList() {
    return getDocument().get(DesignConstants.OUTLINE_SITEMAPLIST_ITEM, String.class, "");
  }

  @Override
  public void setSitemapList(String sitemapList) {
    //throw new NotYetImplementedException();
  }

  @Override
  public void initializeNewDesignNote() {
    this.setFlags("m");
  }

}
