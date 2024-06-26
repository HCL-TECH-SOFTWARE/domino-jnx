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
package com.hcl.domino.jnx.vertx.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.NoCrossCertificateException;

/**
 * Custom  vertx Json Serializer for Document Object in Designs
 *
 * @since 1.0.32
 */
public class DesignDocumentSerializer extends JsonSerializer<Document> {
	public static final String UNID = "unid"; //$NON-NLS-1$
	public static final String SIGNER = "signer"; //$NON-NLS-1$

	@Override
	public void serialize(Document value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStringField(UNID, value.getUNID());
		try {
	    String signer = value.getSigner();
		  gen.writeStringField(SIGNER, signer);
		} catch(NoCrossCertificateException e) {
		  // signer can't be verified - skip silently
		}
		gen.writeEndObject();
	}
}
