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
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.JavaScriptLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
import com.hcl.domino.design.ServerJavaScriptLibrary;
import com.hcl.domino.design.agent.DesignFormulaAgent;
import com.hcl.domino.design.agent.DesignImportedJavaAgent;
import com.hcl.domino.design.agent.DesignJavaAgent;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.design.agent.DesignSimpleActionAgent;

/**
 * Adds language JSON attributes when serializing design agents
 * 
 * @author Karsten Lehmann
 */
class AgentOrLibraryLanguageSerializer extends BeanSerializerBase {
  private static final String PROP_LANGUAGE = "language"; //$NON-NLS-1$
  private static final long serialVersionUID = 1L;

  AgentOrLibraryLanguageSerializer(BeanSerializerBase source) {
      super(source);
  }

  AgentOrLibraryLanguageSerializer(AgentOrLibraryLanguageSerializer source, 
          ObjectIdWriter objectIdWriter) {
      super(source, objectIdWriter);
  }

  AgentOrLibraryLanguageSerializer(AgentOrLibraryLanguageSerializer src, Set<String> toIgnore) {
    super(src, toIgnore);
  }
  
  public AgentOrLibraryLanguageSerializer(AgentOrLibraryLanguageSerializer src,
      ObjectIdWriter objectIdWriter, Object filterId) {
    super(src, objectIdWriter, filterId);
  }

  public BeanSerializerBase withObjectIdWriter(
          ObjectIdWriter objectIdWriter) {
      return new AgentOrLibraryLanguageSerializer(this, objectIdWriter);
  }

  public void serialize(Object bean, JsonGenerator jgen,
          SerializerProvider provider) throws IOException,
          JsonGenerationException {           
    
      jgen.writeStartObject();     
      
      serializeFields(bean, jgen, provider);
      
      if (bean instanceof DesignJavaAgent || bean instanceof JavaLibrary) {
        jgen.writeStringField(PROP_LANGUAGE, "JAVA");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignLotusScriptAgent || bean instanceof LotusScriptLibrary) {
        jgen.writeStringField(PROP_LANGUAGE, "LOTUSSCRIPT");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignFormulaAgent) {
        jgen.writeStringField(PROP_LANGUAGE, "FORMULA");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignImportedJavaAgent) {
        jgen.writeStringField(PROP_LANGUAGE, "IMPORTED_JAVA");  //$NON-NLS-1$
        
        DesignImportedJavaAgent importedJavaAgent = (DesignImportedJavaAgent) bean;
        jgen.writeObjectFieldStart("files"); //$NON-NLS-1$
        List<String> files = importedJavaAgent.getFilenames();
        
        for(String jar : files) {
          Optional<InputStream> content = importedJavaAgent.getFile(jar);
          jgen.writeFieldName(jar);
          OptInputStreamToBase64Serializer.INSTANCE.serialize(content, jgen, provider);
        }
        jgen.writeEndObject();
      }
      else if (bean instanceof DesignSimpleActionAgent) {
        jgen.writeStringField(PROP_LANGUAGE, "SIMPLE_ACTION");  //$NON-NLS-1$
      }
      else if (bean instanceof JavaScriptLibrary) {
        jgen.writeStringField(PROP_LANGUAGE, "JAVASCRIPT");  //$NON-NLS-1$
      }
      else if (bean instanceof ServerJavaScriptLibrary) {
        jgen.writeStringField(PROP_LANGUAGE, "SERVERJAVASCRIPT");  //$NON-NLS-1$
      }
      
      jgen.writeEndObject();
  }

  @Override
  protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
    return new AgentOrLibraryLanguageSerializer(this, toIgnore);
  }

  @Override
  protected BeanSerializerBase asArraySerializer() {
    return this;
  }

  @Override
  public BeanSerializerBase withFilterId(Object filterId) {
    return new AgentOrLibraryLanguageSerializer(this, _objectIdWriter, filterId);
  }
}