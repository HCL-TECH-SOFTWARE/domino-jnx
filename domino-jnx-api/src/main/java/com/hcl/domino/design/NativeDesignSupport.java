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
package com.hcl.domino.design;

import java.nio.ByteBuffer;
import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
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
   * <table border="1">
   * <caption>Values for nameOfContextClass</caption>
   * <tr><th>If the LotusScript was contained in:</th><th>Specify the following text as <code>nameOfContextClass</code>:</th></tr>
   * <tr><td>Agent<br>Webservice<br>Script libraries<br>Globals</td><td><i>Empty string</i></td></tr>
   * <tr><td>Form<br>Page<br>Subform</td><td>NOTESUIDOCUMENT</td></tr>
   * <tr><td>View<br>Folder</td><td>NOTESUIVIEW</td></tr>
   * <tr><td>Button<br>Action hotspot<br>Action<br>Area</td><td>BUTTON</td></tr>
   * <tr><td>Database script</td><td>NOTESUIDATABASE</td></tr>
   * <tr><td>Field</td><td>FIELD</td></tr>
   * </table>
   * 
   * @param code code to format
   * @param nameOfContextClass see table above
   * @return pair of formatted code and any warnings/errors collected during LS parsing
   */
  Pair<String,String> formatLSForDesigner(String code, String nameOfContextClass);
  
  /**
   * Converts a Java string to LMBCS and splits it into chunks of the specified
   * max length.
   * 
   * @param txt text to convert and split
   * @param addNull true to add a null terminator
   * @param replaceLinebreaks true to replace linebreaks with \0
   * @param chunkSize max size of chunks (might be less for LMBCS with multibyte sequences), must be greater than 4
   * @return list of chunks
   */
  List<ByteBuffer> splitAsLMBCS(String txt, boolean addNull, boolean replaceLinebreaks, int chunkSize);

  /**
   * Switches between compatible item data types that all store the item value in
   * CD record format, e.g. between {@link ItemDataType#TYPE_COMPOSITE} and
   * {@link ItemDataType#TYPE_ACTION}.
   * 
   * @param doc document
   * @param item item to change
   * @param newType new item type
   */
  void setCDRecordItemType(Document doc, Item item, ItemDataType newType);
  
  /**
   * Creates the item $AssistRunInfo in an agent design document with an attached
   * database object that contains ODS_ASSISTRUNOBJECTHEADER / ODS_ASSISTRUNOBJECTENTRY
   * structures.
   * 
   * @param doc document to create item $AssistRunInfo
   */
  void initAgentRunInfo(Document doc);
  
}
