package it.com.hcl.domino.test.design;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.hcl.domino.data.CollectionColumn;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.dbdirectory.DirectorySearchQuery.SearchFlag;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.View;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignV1Discuss extends AbstractDesignTest {
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
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testV1discuss", this.database);
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
  public void testMainView() {
    DbDesign design = database.getDesign();
    
    View view = design.getView("Main View").get();
    assertNotNull(view);
    assertEquals("Main View ", view.getTitle());
    assertTrue(view.getAliases().isEmpty());
    
    List<CollectionColumn> columns = view.getColumns();
    assertEquals(5, columns.size());
    
    {
      CollectionColumn col = columns.get(0);
      assertEquals("#", col.getTitle());
      assertEquals(23, col.getDisplayWidth());
      assertEquals("@DocNumber", col.getFormula());
      assertTrue(col.isResizable());
      assertFalse(col.isResponsesOnly());
    }
    {
      CollectionColumn col = columns.get(1);
      assertEquals("Date", col.getTitle());
      assertEquals(48, col.getDisplayWidth());
      assertEquals("@Created", col.getFormula());
      assertTrue(col.isResizable());
      assertFalse(col.isResponsesOnly());
      
      CollectionColumn.SortConfiguration sort = col.getSortConfiguration();
      assertTrue(sort.isSorted());
      assertFalse(sort.isSortedDescending());
      assertFalse(sort.isCategory());
    }
    {
      CollectionColumn col = columns.get(2);
      assertEquals("Rsp", col.getTitle());
      assertEquals(24, col.getDisplayWidth());
      assertEquals("@DocDescendants(\"\"; \"%\")", col.getFormula());
      assertTrue(col.isResizable());
      assertFalse(col.isResponsesOnly());
      
      CollectionColumn.SortConfiguration sort = col.getSortConfiguration();
      assertFalse(sort.isSorted());
      assertFalse(sort.isSortedDescending());
      assertFalse(sort.isCategory());
    }
    {
      CollectionColumn col = columns.get(3);
      assertEquals("", col.getTitle());
      assertEquals(8, col.getDisplayWidth());
      assertEquals("DEFAULT From := @Author;\n"
          + "@V2If(Subject != \"\"; Subject + \"  \"; \"\") + \"(\" + From + \")\"", col.getFormula());
      assertTrue(col.isResizable());
      assertTrue(col.isResponsesOnly());
      
      CollectionColumn.SortConfiguration sort = col.getSortConfiguration();
      assertFalse(sort.isSorted());
      assertFalse(sort.isSortedDescending());
      assertFalse(sort.isCategory());
    }
    {
      CollectionColumn col = columns.get(4);
      assertEquals("Topic", col.getTitle());
      assertEquals(640, col.getDisplayWidth());
      assertEquals("DEFAULT From := @Author;\n"
          + "Subject + \"   (\" + From + \")\"", col.getFormula());
      assertTrue(col.isResizable());
      assertFalse(col.isResponsesOnly());
      
      CollectionColumn.SortConfiguration sort = col.getSortConfiguration();
      assertFalse(sort.isSorted());
      assertFalse(sort.isSortedDescending());
      assertFalse(sort.isCategory());
    }
  }
}
