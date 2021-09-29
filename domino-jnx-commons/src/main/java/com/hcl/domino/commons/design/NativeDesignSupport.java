/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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
 */package com.hcl.domino.commons.design;

import com.hcl.domino.misc.JNXServiceFinder;
import com.hcl.domino.misc.Pair;

/**
 * This service interface represents an implementation-contributed service that
 * provides design APIs
 *
 * @author Karsten Lehmann
 * @since 1.0.43
 */
public interface NativeDesignSupport {
  static NativeDesignSupport get() {
    return JNXServiceFinder.findRequiredService(NativeDesignSupport.class, NativeDesignSupport.class.getClassLoader());
  }

  /** context string for agent code */
  String CTX_AGENT = ""; //$NON-NLS-1$
  /** context string for form code */
  String CTX_FORM = "NOTESUIDOCUMENT"; //$NON-NLS-1$
  /** context string for page code */
  String CTX_PAGE = "NOTESUIDOCUMENT"; //$NON-NLS-1$
  /** context string for subform code */
  String CTX_SUBFORM = "NOTESUIDOCUMENT"; //$NON-NLS-1$
  /** context string for view code */
  String CTX_VIEW = "NOTESUIVIEW"; //$NON-NLS-1$
  /** context string for action code */
  String CTX_ACTION = "BUTTON"; //$NON-NLS-1$
  /** context string for field code */
  String CTX_FIELD = "FIELD"; //$NON-NLS-1$
  /** context string for database script code */
  String CTX_DBSCRIPT = "NOTESUIDATABASE"; //$NON-NLS-1$
  
  /**
   * This function formats a raw block of LotusScript source code by rearranging
   * the Declarations and the Options into the appropriate sections, and adding
   * header comments that the Designer IDE uses for rendering the script
   * (e.g. <code>'++LotusScript Development Environment:2:5:(Options):0:74</code>).<br>
   * This function supports LotusScript in the Script Libraries, Agents, Forms, Pages,
   * Subforms, Views, Actions, Fields or Database Scripts.
   * 
   * @param code code to format
   * @param nameOfContextClass e.g. {@link #CTX_AGENT} for agents (empty string), {@link #CTX_FORM} for forms (NOTESUIDOCUMENT) etc.
   * @return pair of formatted code and any warnings/errors collected during LS parsing
   */
  Pair<String,String> formatLSForDesigner(String code, String nameOfContextClass);
  
}
