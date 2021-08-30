package com.hcl.domino.design;

import java.util.List;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.DesignElement.NamedDesignElement;

public interface Outline extends NamedDesignElement {
  
  public List<IAdaptable> getSitemapList();
  
  public void setSitemapList(String sitemapList);

}