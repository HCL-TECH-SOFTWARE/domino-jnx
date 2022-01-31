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
package com.hcl.domino.jnx.example.swt.bean;

import com.hcl.domino.jnx.jsonb.DocumentJsonbSerializer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

@ApplicationScoped
public class JsonbBean {

  private Jsonb jsonb;

  @Produces
  public Jsonb getJsonb() {
    return this.jsonb;
  }

  @PostConstruct
  public void init() {
    this.jsonb = JsonbBuilder.newBuilder()
        .withConfig(
            new JsonbConfig()
                .withSerializers(DocumentJsonbSerializer.newSerializer())
                .withFormatting(true))
        .build();
  }

}
