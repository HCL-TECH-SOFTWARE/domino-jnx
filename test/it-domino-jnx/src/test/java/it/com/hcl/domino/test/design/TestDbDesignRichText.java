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
package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.Form;
import com.hcl.domino.richtext.records.CDAltText;
import com.hcl.domino.richtext.records.CDAreaElement;
import com.hcl.domino.richtext.records.CDBoxSize;
import com.hcl.domino.richtext.records.CDIDName;
import com.hcl.domino.richtext.records.CDLayer;
import com.hcl.domino.richtext.records.CDMapElement;
import com.hcl.domino.richtext.records.CDPositioning;
import com.hcl.domino.richtext.records.CDStyleName;
import com.hcl.domino.richtext.records.CDTextEffect;
import com.hcl.domino.richtext.records.CDVerticalAlign;
import com.hcl.domino.richtext.structures.CDPoint;
import com.hcl.domino.richtext.structures.CDRect;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDbDesignRichText extends AbstractDesignTest {

  private static String dbPath;

  private Database database;

  @BeforeEach
  public void initDesignDb() throws IOException, URISyntaxException {
    if (this.database == null) {
      final DominoClient client = this.getClient();
      if (dbPath == null) {
        this.database = AbstractNotesRuntimeTest.createTempDb(client);
        
        Acl acl = this.database.getACL();
        Optional<AclEntry> entry = acl.getEntry(client.getEffectiveUserName());
        if(entry.isPresent()) {
          acl.updateEntry(client.getEffectiveUserName(), null, null, Arrays.asList("[Admin]"), null);
        } else {
          acl.addEntry(client.getEffectiveUserName(), AclLevel.MANAGER, Arrays.asList("[Admin]"), EnumSet.allOf(AclFlag.class));
        }
        acl.save();
        
        dbPath = this.database.getAbsoluteFilePath();
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignRichText", this.database);
      } else {
        this.database = client.openDatabase("", dbPath);
      }
    }
  }
  
  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + dbPath + ": " + t);
    }
  }
  
  @Test
  public void testHotspotCircle() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDAreaElement circleArea = form.getBody()
        .stream()
        .filter(CDAreaElement.class::isInstance)
        .map(CDAreaElement.class::cast)
        .filter(val -> val.getShape().orElse(null) == CDAreaElement.Shape.CIRCLE)
        .findFirst()
        .get();
    
    assertEquals(CDAreaElement.Shape.CIRCLE, circleArea.getShape().get());
    assertEquals(0, circleArea.getTabIndex());
    
    //get circle data
    Optional<CDRect> circlePoints = circleArea.getCircle();
    
    assertEquals(387, circlePoints.get().getLeft());
    assertEquals(2,circlePoints.get().getTop());
    assertEquals(564, circlePoints.get().getRight());
    assertEquals(179, circlePoints.get().getBottom());
  }
  
  @Test
  public void testHotspotRect() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDAreaElement rectArea = form.getBody()
        .stream()
        .filter(CDAreaElement.class::isInstance)
        .map(CDAreaElement.class::cast)
        .filter(val -> val.getShape().orElse(null) == CDAreaElement.Shape.RECTANGLE)
        .findFirst()
        .get();
    
    assertEquals(CDAreaElement.Shape.RECTANGLE, rectArea.getShape().get());
    assertEquals(1, rectArea.getTabIndex());
    
    //get rectangle data
    Optional<CDRect> rectPoints = rectArea.getRectangle();
    
    assertEquals(7, rectPoints.get().getLeft());
    assertEquals(280,rectPoints.get().getTop());
    assertEquals(923, rectPoints.get().getRight());
    assertEquals(379, rectPoints.get().getBottom());
  }
  
  @Test
  public void testHotspotPolygon() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDAreaElement polygonArea = form.getBody()
        .stream()
        .filter(CDAreaElement.class::isInstance)
        .map(CDAreaElement.class::cast)
        .filter(val -> val.getShape().orElse(null) == CDAreaElement.Shape.POLYGON)
        .findFirst()
        .get();
    
    assertEquals(CDAreaElement.Shape.POLYGON, polygonArea.getShape().get());
    assertEquals(2, polygonArea.getTabIndex());
    
    //get polygon data
    Optional<List<CDPoint>> polygonPoints = polygonArea.getPolygon();
    
    assertEquals(24, polygonPoints.get().get(0).getX());
    assertEquals(436,polygonPoints.get().get(0).getY());
    assertEquals(657, polygonPoints.get().get(1).getX());
    assertEquals(607,polygonPoints.get().get(1).getY());
    assertEquals(790, polygonPoints.get().get(2).getX());
    assertEquals(442,polygonPoints.get().get(2).getY());
  }
  
  @Test
  public void testHotspotDefault() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDAreaElement defaultArea = form.getBody()
        .stream()
        .filter(CDAreaElement.class::isInstance)
        .map(CDAreaElement.class::cast)
        .filter(val -> val.getShape().orElse(null) == CDAreaElement.Shape.DEFAULT)
        .findFirst()
        .get();
    
    assertEquals(CDAreaElement.Shape.DEFAULT, defaultArea.getShape().get());
    assertEquals(3, defaultArea.getTabIndex());
  }
  
  @Test
  public void testImageMap() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDMapElement mapElement = form.getBody()
        .stream()
        .filter(CDMapElement.class::isInstance)
        .map(CDMapElement.class::cast)
        .findFirst()
        .get();
    
    assertEquals(41, mapElement.getLastDefaultRegionID());
    assertEquals(2, mapElement.getLastCircleRegionID());
    assertEquals(60, mapElement.getLastRectRegionID());
    assertEquals(1, mapElement.getLastPolyRegionID());
  }
  
  @Test
  public void testStyle() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDStyleName style = form.getBody()
        .stream()
        .filter(CDStyleName.class::isInstance)
        .map(CDStyleName.class::cast)
        .findFirst()
        .get();
    
    assertEquals(true, style.getFlags().contains(CDStyleName.Flag.INCYCLE));
    assertEquals(true, style.getFlags().contains(CDStyleName.Flag.PERMANENT));
    assertEquals("Headline", style.getStyleName());
  }
  
  @Test
  public void testAltText() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestAreaHotspot").get();
    
    CDAltText altText = form.getBody()
        .stream()
        .filter(CDAltText.class::isInstance)
        .map(CDAltText.class::cast)
        .findFirst()
        .get();
    
    assertEquals("ImageAltText", altText.getAltText().trim());
  }
  
  @Test
  public void testLayer() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("TestLayer").get();
    
    //get CDLayer
    CDLayer layer = form.getBody()
        .stream()
        .filter(CDLayer.class::isInstance)
        .map(CDLayer.class::cast)
        .findFirst()
        .get();
    
    assertNotNull(layer);
    
    //get ID
    CDIDName idName = form.getBody()
        .stream()
        .filter(CDIDName.class::isInstance)
        .map(CDIDName.class::cast)
        .findFirst()
        .get();
    
    assertEquals("LayerId", idName.getID());
    
    //get CDPOSITING
    CDPositioning position = form.getBody()
        .stream()
        .filter(CDPositioning.class::isInstance)
        .map(CDPositioning.class::cast)
        .findFirst()
        .get();
    
    assertEquals(96, position.getLeft().getLength());
    assertEquals(0, position.getBottom().getLength());
    
  //get CDBOXSIZE
    CDBoxSize box = form.getBody()
        .stream()
        .filter(CDBoxSize.class::isInstance)
        .map(CDBoxSize.class::cast)
        .findFirst()
        .get();
    
    assertEquals(500, box.getWidth().getLength());
    assertEquals(200, box.getHeight().getLength());
  }
  
  @Test
  public void testTextEffect() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("ppage layout A").get();
    
    CDTextEffect textEffect = form.getBody()
        .stream()
        .filter(CDTextEffect.class::isInstance)
        .map(CDTextEffect.class::cast)
        .findFirst()
        .get();
    
    assertEquals(true, textEffect.getFontStyle().isShadow());
  }
    
  @Test
  public void testVerticalAlign() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("VerticalAlign").get();
    
    CDVerticalAlign verticalAlign = form.getBody()
        .stream()
        .filter(CDVerticalAlign.class::isInstance)
        .map(CDVerticalAlign.class::cast)
        .findFirst()
        .get();
    
    assertEquals(CDVerticalAlign.Alignment.BOTTOM, verticalAlign.getAlignment().get());
  }
}
