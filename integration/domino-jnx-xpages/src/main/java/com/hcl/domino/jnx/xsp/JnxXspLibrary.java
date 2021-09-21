package com.hcl.domino.jnx.xsp;

import com.hcl.domino.DominoProcess;
import com.hcl.domino.commons.util.DominoUtils;
import com.ibm.xsp.library.AbstractXspLibrary;

public class JnxXspLibrary extends AbstractXspLibrary {

  public JnxXspLibrary() {
    // On load, set the "don't call NotesInit" flags
    DominoUtils.setJavaProperty("jnx.noinit", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    DominoProcess.get().initializeProcess(new String[0]);
  }

  @Override
  public String getLibraryId() {
    return getClass().getPackage().getName();
  }
  
  @Override
  public String getPluginId() {
    return "com.hcl.domino.jnx.xsp"; //$NON-NLS-1$
  }

}
