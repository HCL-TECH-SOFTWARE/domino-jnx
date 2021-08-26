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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hcl.domino.data.Document;

/**
 * Custom  vertx Json Serializer for Document Object in Designs
 *
 * @since 1.0.32
 */
public class DocumentToUnidSerializer extends JsonSerializer<Document> {
	private static String UNID = "unid";

	@Override
	public void serialize(Document value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStringField(UNID, value.getUNID());
		gen.writeEndObject();
	}
}
