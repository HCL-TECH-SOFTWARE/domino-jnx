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
import com.hcl.domino.design.Outline;
import com.hcl.domino.design.OutlineEntry;
import com.hcl.domino.richtext.records.CDResource;
import com.hcl.domino.richtext.records.CDResource.ResourceClass;
import com.hcl.domino.richtext.records.CDResource.Type;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;
import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestDbDesignOutline extends AbstractDesignTest {

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
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignOutline", this.database);
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
  
  @SuppressWarnings("unchecked")
  @Test
  public void testOutline() throws IOException {
    DbDesign design = this.database.getDesign();
    Outline outline = design.getOutline("TestOutline").get();

    List<OutlineEntry> outlineEntries = outline.getSitemapList();

    Optional<OutlineEntry> outlineEntry = outlineEntries
        .stream()
        .map(OutlineEntry.class::cast)
        .filter(val -> ((String)val.getTitle().get()).contains("Action Entry9"))
        .findAny();

    CDResource resource = ((List<CDResource>)(outlineEntry.get()
        .getOnclickData().get())).get(0);

    assertEquals(Type.ACTION, 
        resource.getResourceType().get());
    assertEquals(EnumSet.of(CDResource.Flag.FORMULA), 
        resource.getFlags());
    assertEquals("@Accessed", 
        resource.getNamedElementFormulas().get().get(0));
    assertEquals("1-17.gif", 
        ((List<CDResource>)outlineEntry.get()
            .getImageData().get())
            .get(0).getNamedElement().get());
    assertEquals("Alias Entry9", ((String)outlineEntry.get()
        .getAlias().get()).trim());

    outlineEntry = outlineEntries
        .stream()
        .map(OutlineEntry.class::cast)
        .filter(val -> ((String)val.getTitle().get()).contains("Named Entry11"))
        .findAny();

    assertEquals(Type.NAMEDELEMENT, 
        ((List<CDResource>)(outlineEntry.get().getOnclickData().get()))
            .get(0).getResourceType().get());
    assertEquals(ResourceClass.PAGE, 
        ((List<CDResource>)(outlineEntry.get().getOnclickData().get()))
            .get(0).getResourceClass().get());
    assertEquals("30-41.gif", 
        ((List<CDResource>)outlineEntry.get().getImageData().get())
            .get(0).getNamedElement().get());
    assertEquals("Alias Entry11", ((String)outlineEntry.get()
        .getAlias().get()).trim());

    outlineEntry = outlineEntries
        .stream()
        .map(OutlineEntry.class::cast)
        .filter(val -> ((String)val.getTitle().get()).contains("Target Frame Entry"))
        .findAny();

    assertEquals(Type.NAMEDELEMENT, 
        ((List<CDResource>)(outlineEntry.get().getOnclickData().get()))
            .get(0).getResourceType().get());
    assertEquals(ResourceClass.FRAMESET, 
        ((List<CDResource>)(outlineEntry.get().getOnclickData().get()))
            .get(0).getResourceClass().get());
    assertEquals("all.gif", 
        ((List<CDResource>)outlineEntry.get().getImageData().get())
            .get(0).getNamedElement().get());
    assertEquals("target alias", ((String)outlineEntry.get()
        .getAlias().get()).trim());
    assertEquals("wMainFrameset", ((String)outlineEntry.get()
        .getTargetFrame().get()).trim());
  }
}
