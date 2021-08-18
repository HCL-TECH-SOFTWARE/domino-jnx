package com.hcl.domino.design;

import com.hcl.domino.design.DesignElement.NamedDesignElement;

public interface Outline extends NamedDesignElement {
  
  public Object getSitemapList();
  
  public void setSitemapList(String sitemapList);

}