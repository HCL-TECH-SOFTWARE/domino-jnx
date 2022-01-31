/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
