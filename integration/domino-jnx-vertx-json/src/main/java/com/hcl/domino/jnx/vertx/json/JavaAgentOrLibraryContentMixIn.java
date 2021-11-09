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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class JavaAgentOrLibraryContentMixIn {
  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getObjectAttachment();
  
  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getResourcesAttachment();

  @JsonSerialize(using = OptInputStreamToBase64Serializer.class) abstract Optional<InputStream> getSourceAttachment();
  
  @JsonSerialize(using = JavaContentEmbeddedJarsSerializer.class) abstract List<String> getEmbeddedJars();
  
  @JsonIgnore abstract List<String> getFile(String file);

}
