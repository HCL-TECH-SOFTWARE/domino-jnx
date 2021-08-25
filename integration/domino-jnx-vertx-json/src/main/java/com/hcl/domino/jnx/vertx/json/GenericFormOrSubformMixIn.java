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
 */
package com.hcl.domino.jnx.vertx.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.SubformReference;
import com.hcl.domino.richtext.FormField;

/**
 * vertx Json Serializer Mixin for View Object in Designs
 *
 * @since 1.0.32
 */
public abstract class GenericFormOrSubformMixIn {
  
  @JsonIgnore abstract List<String> getExplicitSubformRecursive();
  @JsonIgnore abstract List<FormField> getFields();
  @JsonIgnore abstract List<SubformReference> getSubforms();
  @JsonSerialize(using = DocumentToUnidSerializer.class) abstract Document getDocument();
  
}