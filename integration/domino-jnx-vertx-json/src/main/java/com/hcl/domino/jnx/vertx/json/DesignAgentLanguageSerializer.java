package com.hcl.domino.jnx.vertx.json;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.hcl.domino.design.JavaLibrary;
import com.hcl.domino.design.LotusScriptLibrary;
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
class DesignAgentLanguageSerializer extends BeanSerializerBase {
  private static final String JSON_AGENT_LANGUAGE = "agentLanguage"; //$NON-NLS-1$
  private static final long serialVersionUID = 1L;

  DesignAgentLanguageSerializer(BeanSerializerBase source) {
      super(source);
  }

  DesignAgentLanguageSerializer(DesignAgentLanguageSerializer source, 
          ObjectIdWriter objectIdWriter) {
      super(source, objectIdWriter);
  }

  DesignAgentLanguageSerializer(DesignAgentLanguageSerializer src, Set<String> toIgnore) {
    super(src, toIgnore);
  }
  
  public DesignAgentLanguageSerializer(DesignAgentLanguageSerializer src,
      ObjectIdWriter objectIdWriter, Object filterId) {
    super(src, objectIdWriter, filterId);
  }

  public BeanSerializerBase withObjectIdWriter(
          ObjectIdWriter objectIdWriter) {
      return new DesignAgentLanguageSerializer(this, objectIdWriter);
  }

  public void serialize(Object bean, JsonGenerator jgen,
          SerializerProvider provider) throws IOException,
          JsonGenerationException {           
    
      jgen.writeStartObject();     
      
      serializeFields(bean, jgen, provider);
      
      if (bean instanceof DesignJavaAgent || bean instanceof JavaLibrary) {
        jgen.writeStringField(JSON_AGENT_LANGUAGE, "JAVA");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignLotusScriptAgent || bean instanceof LotusScriptLibrary) {
        jgen.writeStringField(JSON_AGENT_LANGUAGE, "LS");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignFormulaAgent) {
        jgen.writeStringField(JSON_AGENT_LANGUAGE, "FORMULA");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignImportedJavaAgent) {
        jgen.writeStringField(JSON_AGENT_LANGUAGE, "IMPORTED_JAVA");  //$NON-NLS-1$
      }
      else if (bean instanceof DesignSimpleActionAgent) {
        jgen.writeStringField(JSON_AGENT_LANGUAGE, "SIMPLE_ACTION");  //$NON-NLS-1$
      }
      
      jgen.writeEndObject();
  }

  @Override
  protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
    return new DesignAgentLanguageSerializer(this, toIgnore);
  }

  @Override
  protected BeanSerializerBase asArraySerializer() {
    return this;
  }

  @Override
  public BeanSerializerBase withFilterId(Object filterId) {
    return new DesignAgentLanguageSerializer(this, _objectIdWriter, filterId);
  }
}