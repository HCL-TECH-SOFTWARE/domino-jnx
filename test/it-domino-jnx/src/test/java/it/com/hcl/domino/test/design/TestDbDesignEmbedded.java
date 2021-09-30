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
package it.com.hcl.domino.test.design;

import static it.com.hcl.domino.test.util.ITUtil.toLf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.hcl.domino.design.Page;
import com.hcl.domino.richtext.records.CDEmbeddedCalendarControl;
import com.hcl.domino.richtext.records.CDEmbeddedEditControl;
import com.hcl.domino.richtext.records.CDEmbeddedExtraInfo;
import com.hcl.domino.richtext.records.CDEmbeddedOutline;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControl;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControlExtra;
import com.hcl.domino.richtext.records.CDEmbeddedView;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignEmbedded extends AbstractDesignTest {

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
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignEmbedded", this.database);
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
  public void testEmbeddedOutlinePage() throws IOException {
    DbDesign design = this.database.getDesign();
    Page page = design.getPage("BBCreate").get();
    
    CDEmbeddedOutline outline = page.getBody()
        .stream()
        .filter(CDEmbeddedOutline.class::isInstance)
        .map(CDEmbeddedOutline.class::cast)
        .findFirst()
        .get();
    
    assertEquals(360, outline.getSubLevelHorizontalOffset());
    assertEquals(288, outline.getSubLevelHeight());
    //top level color
    assertColorEquals(outline.getSelectionFontColors()[1], 255, 255, 255);
    //sub level color
    assertColorEquals(outline.getSelectionFontColors()[2], 255, 255, 255);
    //top level mouse color
    assertColorEquals(outline.getMouseFontColors()[1], 0, 0, 255);
    //sub level mouse color
    assertColorEquals(outline.getMouseFontColors()[2], 0, 0, 255);
    //name
    assertEquals("TestEmbeddedOutline", outline.getName());
    //target frame
    assertEquals("TestEmbeddedOutlineFrame", outline.getTargetFrame());
  }
  
  @Test
  public void testEmbeddedView() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("ppage layout B").get();
    
    // The embedded view will be immediately followed by another
    //   record that actually contains the name
    
    int viewIndex = -1;
    List<?> body = form.getBody();
    for(int i = 0; i < body.size(); i++) {
      if(body.get(i) instanceof CDEmbeddedView) {
        viewIndex = i;
        break;
      }
    }
    assertTrue(viewIndex > -1 && viewIndex < body.size());
    CDEmbeddedView view = (CDEmbeddedView)body.get(viewIndex);
    
    assertEquals(
      EnumSet.of(CDEmbeddedView.Flag.USEAPPLET_INBROWSER, CDEmbeddedView.Flag.SIMPLE_VIEW_SHOW_ACTION_BAR,
        CDEmbeddedView.Flag.SIMPLE_VIEW_SHOW_SELECTION_MARGIN, CDEmbeddedView.Flag.HASNAME),
      view.getFlags()
    );
    
    CDEmbeddedExtraInfo info = (CDEmbeddedExtraInfo)body.get(viewIndex+1);
    assertEquals("TestEmbeddedView", info.getName());
  }
  
  @Test
  public void testEmbeddedCalendar() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("ppage layout G").get();
    
    CDEmbeddedCalendarControl cal = form.getBody()
        .stream()
        .filter(CDEmbeddedCalendarControl.class::isInstance)
        .map(CDEmbeddedCalendarControl.class::cast)
        .findFirst()
        .get();
    
    assertEquals("TestTargetFrame", cal.getTargetFrameName());
    assertEquals(
      EnumSet.of(CDEmbeddedCalendarControl.Flag.NON_TRANSPARENT_BKGND, 
          CDEmbeddedCalendarControl.Flag.HASTARGETFRAME),
      cal.getFlags()
    );
  }
  
  @Test
  public void testEmbeddedScheduler() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("EmbeddedScheduler").get();
    
    CDEmbeddedSchedulerControl scheduler = form.getBody()
        .stream()
        .filter(CDEmbeddedSchedulerControl.class::isInstance)
        .map(CDEmbeddedSchedulerControl.class::cast)
        .findFirst()
        .get();
    
    assertEquals("EmbeddedSchedulerTargetFrame", scheduler.getTargetFrameName());
    assertEquals("\"Line1\nLine2\nLine3\"", toLf(scheduler.getReqPeopleItemsFormula().get()));
    //assertEquals(scheduler.getDisplayStartDTItemFormula(), "@Accessed");
    assertEquals("\"8-5\"", scheduler.getHrsPerDayItemFormula().get());
  }
  
  @Test
  public void testEmbeddedSchedulerExtra() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("EmbeddedScheduler").get();
    
    CDEmbeddedSchedulerControlExtra schedulerextra = form.getBody()
        .stream()
        .filter(CDEmbeddedSchedulerControlExtra.class::isInstance)
        .map(CDEmbeddedSchedulerControlExtra.class::cast)
        .findFirst()
        .get();
    
    assertEquals("@All", schedulerextra.getOptPeopleItemsFormula().get());
    assertEquals("\"Room1\nRoom2\"", toLf(schedulerextra.getReqRoomsItemsFormula().get()));
    assertEquals("EmbeddedSchedulerName", schedulerextra.getSchedulerName());
  }
  
  @Test
  public void testEmbeddedEditor() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("EmbeddedEditor").get();
    
    CDEmbeddedEditControl editor = form.getBody()
        .stream()
        .filter(CDEmbeddedEditControl.class::isInstance)
        .map(CDEmbeddedEditControl.class::cast)
        .findFirst()
        .get();
    
    assertEquals("EmbeddedEditorName", editor.getName());
  }
}
