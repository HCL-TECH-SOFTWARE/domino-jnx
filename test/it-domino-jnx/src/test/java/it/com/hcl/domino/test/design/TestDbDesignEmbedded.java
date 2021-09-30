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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
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
import com.hcl.domino.richtext.records.CDEmbeddedOutline;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControl;
import com.hcl.domino.richtext.records.CDEmbeddedSchedulerControlExtra;
import com.hcl.domino.richtext.records.CDEmbeddedView;
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
    
    assertEquals(288, outline.getSubLevelHorizontalOffset());
    //top level color
    assertColorEquals(outline.getSelectionFontColors()[1], 255, 255, 255);
    //sub level color
    assertColorEquals(outline.getSelectionFontColors()[2], 255, 255, 255);
    //top level mouse color
    assertColorEquals(outline.getMouseFontColors()[1], 0, 0, 255);
    //sub level mouse color
    assertColorEquals(outline.getMouseFontColors()[2], 0, 0, 255);
    //name
    assertEquals(outline.getName(), "TestEmbeddedOutline");
    //target frame
    assertEquals(outline.getTargetFrame(), "TestEmbeddedOutlineFrame");
  }
  
  @Test
  public void testEmbeddedView() throws IOException {
    DbDesign design = this.database.getDesign();
    Form form = design.getForm("ppage layout B").get();
    
    CDEmbeddedView view = form.getBody()
        .stream()
        .filter(CDEmbeddedView.class::isInstance)
        .map(CDEmbeddedView.class::cast)
        .findFirst()
        .get();
    
    //assertEquals(view.getFlags(), 
    //    EnumSet.of(CDEmbeddedView.Flag.HASNAME, 
    //        CDEmbeddedView.Flag.SIMPLE_VIEW_SHOW_ACTION_BAR, 
    //        CDEmbeddedView.Flag.SIMPLE_VIEW_SHOW_SELECTION_MARGIN));
    //name length
    assertEquals(view.getNameLength(), new String("TestEmbeddedView").length());
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
    
    assertEquals(cal.getTargetFrameName(), "TestTargetFrame");
    assertEquals(cal.getFlags(),
        EnumSet.of(CDEmbeddedCalendarControl.Flag.NON_TRANSPARENT_BKGND, 
            CDEmbeddedCalendarControl.Flag.HASTARGETFRAME));
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
    
    assertEquals(scheduler.getTargetFrameName(), "EmbeddedSchedulerTargetFrame");
    assertEquals(scheduler.getReqPeopleItemsFormula(), "\"Line1\r\nLine2\r\nLine3\"");
    //assertEquals(scheduler.getDisplayStartDTItemFormula(), "@Accessed");
    assertEquals(scheduler.getHrsPerDayItemFormula(), "\"8-5\"");
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
    
    assertEquals(schedulerextra.getOptPeopleItemsFormula(), "@All");
    assertEquals(schedulerextra.getReqRoomsItemsFormula(), "\"Room1\r\nRoom2\"");
    assertEquals(schedulerextra.getSchedulerName(), "EmbeddedSchedulerName");
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
    
    assertEquals(editor.getName(), "EmbeddedEditorName");
  }
}
