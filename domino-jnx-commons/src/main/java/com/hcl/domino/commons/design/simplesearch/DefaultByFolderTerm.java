package com.hcl.domino.commons.design.simplesearch;

import com.hcl.domino.design.simplesearch.ByFolderTerm;

public class DefaultByFolderTerm implements ByFolderTerm {
  private final String folderName;
  private final boolean isPrivate;
  
  public DefaultByFolderTerm(String folderName, boolean isPrivate) {
    this.folderName = folderName;
    this.isPrivate = isPrivate;
  }

  @Override
  public boolean isPrivate() {
    return isPrivate;
  }

  @Override
  public String getFolderName() {
    return folderName;
  }

}
