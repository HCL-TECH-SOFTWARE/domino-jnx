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
package it.com.hcl.domino.test.vertx.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.hcl.domino.data.Database;
import com.hcl.domino.design.DesignAgent;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.Form;
import com.hcl.domino.design.Page;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializer;

import io.vertx.core.json.JsonObject;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

/**
 * @author Jesse Gallagher
 * @since 1.0.33
 */
@SuppressWarnings("nls")
public class TestVertxDesignJsonSerialization extends AbstractNotesRuntimeTest {
  // Run against a handful of common stock NTFs to get a spread of examples
  public static class StockTemplatesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
        "pernames.ntf",
        "bookmark.ntf",
        "log.ntf",
        "mailbox.ntf",
        "roamingdata.ntf",
        "autosave.ntf",
        "doclbs7.ntf",
        "headline.ntf",
        "busytime.ntf"
      ).map(Arguments::of);
    }
  }

  @ParameterizedTest
  @ArgumentsSource(StockTemplatesProvider.class)
  public void testViewSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getViews()
      .forEach(view -> {
        serializer.toJson(view);
      });
  }
  
  @Test
  public void testFormBasicSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesignForms", database -> {
      Form form = database.getDesign().getForm("Test LS Form").get();
      serializer.toJson(form);
    });
  }
  
  @Test
  public void testPageBasicSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesign", database -> {
      Page page = database.getDesign().getPage("Test Page").get();
      serializer.toJson(page);
    });
  }
  
  @ParameterizedTest
  @ArgumentsSource(StockTemplatesProvider.class)
  public void testFormSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getForms()
      .forEach(serializer::toJson);
  }
  
  @ParameterizedTest
  @ArgumentsSource(StockTemplatesProvider.class)
  public void testSubformSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getSubforms()
      .forEach(form -> {
        serializer.toJson(form);
      });
  }
  
  @Test
  public void testAgentRunInfoSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesignAgents", database -> {
      serializer.toJson(database.getDesign().getAgent("Test Java").get()).encodePrettily();
    });
  }
  
  @ParameterizedTest
  @ArgumentsSource(StockTemplatesProvider.class)
  public void testOutlineSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getOutlines()
      .forEach(outline -> {
        serializer.toJson(outline);
      });
  }
  
  @ParameterizedTest
  @ArgumentsSource(StockTemplatesProvider.class)
  public void testAgentSerialization(String dbName) {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    Database names = getClient().openDatabase(dbName);
    names.getDesign()
      .getAgents()
      .forEach(serializer::toJson);
  }
  
  @Test
  public void testResourceAgentsSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesignAgents", database -> {
      database.getDesign()
        .getAgents()
        .forEach(serializer::toJson);
      
      DesignAgent agent = database.getDesign()
          .getAgents()
          .findFirst()
          .get();
      agent.sign();
      // Check an individual agent to read the signer
      JsonObject json = serializer.toJson(agent);
      JsonObject doc = json.getJsonObject("document");
      assertEquals(database.getParentDominoClient().getIDUserName(), doc.getString("signer"));
    });
  }
  
  @Test
  public void testFormButtonSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesignForms", database -> {
      Form form = database.getDesign().getForm("Button Form").get();
      String json = serializer.toJson(form).toString();
      assertTrue(json.contains("@StatusBar(\\\"I am button output\\\")"));
    });
  }
  
  @Test
  public void testFolderSerialization() throws Exception {
    VertxJsonSerializer serializer = new VertxJsonSerializer();
    
    withResourceDxl("/dxl/testDbDesign", database -> {
      Folder folder = database.getDesign().getFolder("test folder").get();
      String json = serializer.toJson(folder).toString();
      assertTrue(json.contains("\"test folder\""));
    });
  }
}
