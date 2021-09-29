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

  /**
   * This function formats a raw block of LotusScript source code by rearranging
   * the Declarations and the Options into the appropriate sections, and adding
   * header comments that the Designer IDE uses for rendering the script
   * (e.g. <code>'++LotusScript Development Environment:2:5:(Options):0:74</code>).<br>
   * This function supports LotusScript in the Script Libraries, Agents, Forms, Pages,
   * Subforms, Views, Actions, Fields or Database Scripts.<br>
   * <br>
   * The <code>nameOfContextClass</code> parameter controls how the LS code is
   * formatted, e.g. the method might add binding code for NOTESUIDOCUMENT if
   * form code is formatted.<br>
   * <br>
   * See this table for possible values:<br>
   * <br>
   * <table border="1" cellpadding="0" cellspacing="0">
   * <tr valign="top"><th>If the LotusScript was contained in:</th><th>Specify the following text as <code>nameOfContextClass</code>:</th></tr>
   * <tr valign="top"><td>Agent<br>Webservice<br>Script libraries<br>Globals</td><td><i>Empty string</i></td></tr>
   * <tr valign="top"><td>Form<br>Page<br>Subform</td><td>NOTESUIDOCUMENT</td></tr>
   * <tr valign="top"><td>View<br>Folder</td><td>NOTESUIVIEW</td></tr>
   * <tr valign="top"><td>Button<br>Action hotspot<br>Action<br>Area</td><td>BUTTON</td></tr>
   * <tr valign="top"><td>Database script</td><td>NOTESUIDATABASE</td></tr>
   * <tr valign="top"><td>Field</td><td>FIELD</td></tr>
   * </table>
   * 
   * @param code code to format
   * @param nameOfContextClass see table above
   * @return pair of formatted code and any warnings/errors collected during LS parsing
   */
  Pair<String,String> formatLSForDesigner(String code, String nameOfContextClass);
  
}
