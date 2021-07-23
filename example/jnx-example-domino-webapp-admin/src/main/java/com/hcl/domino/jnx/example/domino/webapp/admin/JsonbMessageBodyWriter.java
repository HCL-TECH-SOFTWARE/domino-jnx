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
package com.hcl.domino.jnx.example.domino.webapp.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class JsonbMessageBodyWriter implements MessageBodyWriter<Object> {

  private final Jsonb jsonb = JsonbBuilder.create();

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType) {
    return mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE) && !String.class.equals(type);
  }

  @Override
  public void writeTo(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
      throws IOException, WebApplicationException {
    this.jsonb.toJson(t, entityStream);
  }

}
