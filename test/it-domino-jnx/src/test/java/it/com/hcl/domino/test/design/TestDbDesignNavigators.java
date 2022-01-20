package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.DesignType;
import com.hcl.domino.design.Navigator;
import com.hcl.domino.design.navigator.NavigatorFillStyle;
import com.hcl.domino.design.navigator.NavigatorLineStyle;
import com.hcl.domino.richtext.records.ViewmapActionRecord;
import com.hcl.domino.richtext.records.ViewmapBigDrawingObject;
import com.hcl.domino.richtext.records.ViewmapDatasetRecord;
import com.hcl.domino.richtext.records.ViewmapDrawingObject;
import com.hcl.domino.richtext.records.ViewmapHeaderRecord;
import com.hcl.domino.richtext.records.ViewmapPolygonRecordByte;
import com.hcl.domino.richtext.records.ViewmapRectRecord;
import com.hcl.domino.richtext.records.ViewmapRegionRecord;
import com.hcl.domino.richtext.records.ViewmapTextRecord;
import com.hcl.domino.security.Acl;
import com.hcl.domino.security.AclEntry;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestDbDesignNavigators extends AbstractDesignTest {
  public static final int EXPECTED_IMPORT_NAVIGATORS = 1;
  private static String dbPath;

  @AfterAll
  public static void termDesignDb() {
    try {
      Files.deleteIfExists(Paths.get(dbPath));
    } catch (final Throwable t) {
      System.err.println("Unable to delete database " + dbPath + ": " + t);
    }
  }

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
        AbstractNotesRuntimeTest.populateResourceDxl("/dxl/testDbDesignNavigators", this.database);
      } else {
        this.database = client.openDatabase("", dbPath);
      }
    }
  }
  
  @Test
  public void testNavigators() {
    DbDesign design = database.getDesign();
    List<Navigator> navs = design.getNavigators().collect(Collectors.toList());
    assertEquals(1, navs.size());
    assertInstanceOf(Navigator.class, navs.get(0));
  }
  
  @Test
  public void testTestNav() {
    DbDesign design = database.getDesign();
    Navigator nav = design.getNavigator("testnav").get();
    
    {
      List<?> imagemap = nav.getImageMap();
      assertTrue(imagemap.stream().anyMatch(ViewmapHeaderRecord.class::isInstance));
    }
    {
      List<?> dataset = nav.getDataSet();
      assertTrue(dataset.stream().anyMatch(ViewmapDatasetRecord.class::isInstance));
    }
    {
      List<?> layout = nav.getLayout();
      assertTrue(layout.stream().anyMatch(ViewmapHeaderRecord.class::isInstance));

      {
        ViewmapHeaderRecord header = (ViewmapHeaderRecord)layout.get(0);
        assertEquals(8, header.getVersion());
      }
      {
        ViewmapRectRecord rect = (ViewmapRectRecord)layout.get(1);
        
        ViewmapDrawingObject draw = rect.getDrawingObject();
        assertEquals(302, draw.getObjRect().getLeft());
        assertEquals(45, draw.getObjRect().getTop());
        assertEquals(377, draw.getObjRect().getRight());
        assertEquals(150, draw.getObjRect().getBottom());
        assertEquals(EnumSet.of(ViewmapDrawingObject.Flag.VISIBLE, ViewmapDrawingObject.Flag.SELECTABLE), draw.getFlags());
        assertEquals(5, draw.getFontID().getFontFace());

        assertEquals(StandardColors.RoyalPurple, rect.getLineColor().get());
        assertEquals(StandardColors.YellowGreen, rect.getFillForegroundColor().get());
        assertEquals(StandardColors.Cyan, rect.getFillBackgroundColor().get());
        assertEquals(NavigatorLineStyle.SOLID, rect.getLineStyle());
        assertEquals(NavigatorFillStyle.SOLID, rect.getFillStyle());
        
        assertEquals("NormalRect", rect.getName());
        assertEquals("I am a normal rectangle.", rect.getLabel());
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(2);
        assertEquals(StandardColors.DarkMagenta2, action.getHighlightOutlineColor().get());
        assertEquals(StandardColors.Heather, action.getHighlightFillColor().get());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(ViewmapActionRecord.Action.NONE, action.getClickAction().get());
        assertEquals(DesignType.SHARED, action.getActionDataDesignType());
      }
      {
        ViewmapRectRecord button = (ViewmapRectRecord)layout.get(3);
        
        ViewmapDrawingObject draw = button.getDrawingObject();
        assertEquals(330, draw.getObjRect().getLeft());
        assertEquals(281, draw.getObjRect().getTop());
        assertEquals(425, draw.getObjRect().getRight());
        assertEquals(332, draw.getObjRect().getBottom());
        assertEquals(EnumSet.of(ViewmapDrawingObject.Flag.VISIBLE, ViewmapDrawingObject.Flag.SELECTABLE, ViewmapDrawingObject.Flag.LOCKED), draw.getFlags());
        assertEquals(StandardFonts.UNICODE, draw.getFontID().getStandardFont().get());

        assertEquals(StandardColors.LightOlive, button.getLineColor().get());
        assertEquals(StandardColors.LightLavender, button.getFillForegroundColor().get());
        assertEquals(StandardColors.LightGray, button.getFillBackgroundColor().get());
        assertEquals(NavigatorLineStyle.SOLID, button.getLineStyle());
        assertEquals(3, button.getLineWidth());
        assertEquals(NavigatorFillStyle.SOLID, button.getFillStyle());
        
        assertEquals("Button10", button.getName());
        assertEquals("I am a button.", button.getLabel());
        
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(4);
        assertEquals(StandardColors.Lemon, action.getHighlightOutlineColor().get());
        assertEquals(StandardColors.LilacMist, action.getHighlightFillColor().get());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(8, action.getHighlightOutlineWidth());
        assertEquals(ViewmapActionRecord.Action.NONE, action.getClickAction().get());
        assertEquals(DesignType.SHARED, action.getActionDataDesignType());
      }
      {
        ViewmapTextRecord text = (ViewmapTextRecord)layout.get(5);
        
        ViewmapBigDrawingObject draw = text.getDrawingObject();
        assertEquals(533, draw.getObjRect().getLeft());
        assertEquals(64, draw.getObjRect().getTop());
        assertEquals(712, draw.getObjRect().getRight());
        assertEquals(95, draw.getObjRect().getBottom());
        assertEquals(EnumSet.of(ViewmapBigDrawingObject.Flag.VISIBLE, ViewmapBigDrawingObject.Flag.SELECTABLE, ViewmapBigDrawingObject.Flag.LOCKED), draw.getFlags());
        assertEquals(StandardFonts.TYPEWRITER, draw.getFontID().getStandardFont().get());

        assertEquals(StandardColors.Black, text.getLineColor().get());
        assertEquals(StandardColors.White, text.getFillForegroundColor().get());
        assertEquals(StandardColors.White, text.getFillBackgroundColor().get());
        assertEquals(NavigatorLineStyle.NONE, text.getLineStyle());
        assertEquals(1, text.getLineWidth());
        assertEquals(NavigatorFillStyle.TRANSPARENT, text.getFillStyle());
        
        assertEquals("Text11", text.getName());
        assertEquals("I think I am a text box.", text.getLabel());
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(6);
        assertEquals(StandardColors.ManganeseBlue, action.getHighlightOutlineColor().get());
        assertFalse(action.getHighlightFillColor().isPresent());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(2, action.getHighlightOutlineWidth());
        assertEquals(ViewmapActionRecord.Action.NONE, action.getClickAction().get());
        assertEquals(DesignType.SHARED, action.getActionDataDesignType());
      }
      {
        ViewmapRegionRecord rect = (ViewmapRegionRecord)layout.get(7);
        
        ViewmapDrawingObject draw = rect.getDrawingObject();
        assertEquals(152, draw.getObjRect().getLeft());
        assertEquals(73, draw.getObjRect().getTop());
        assertEquals(228, draw.getObjRect().getRight());
        assertEquals(173, draw.getObjRect().getBottom());
        assertEquals(EnumSet.of(ViewmapDrawingObject.Flag.VISIBLE, ViewmapDrawingObject.Flag.SELECTABLE), draw.getFlags());
        assertEquals(StandardFonts.SWISS, draw.getFontID().getStandardFont().get());

        assertEquals(StandardColors.Red, rect.getLineColor().get());
        assertEquals(NavigatorLineStyle.SOLID, rect.getLineStyle());
        assertEquals(2, rect.getLineWidth());
        assertEquals(NavigatorFillStyle.TRANSPARENT, rect.getFillStyle());
        
        assertEquals("HotspotRectangle1", rect.getName());
        assertEquals("", rect.getLabel());
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(8);
        assertEquals(StandardColors.Red, action.getHighlightOutlineColor().get());
        assertEquals(StandardColors.Black, action.getHighlightFillColor().get());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(ViewmapActionRecord.Action.RUNSCRIPT, action.getClickAction().get());
        assertTrue(action.getActionString().get().contains("I am a LotusScript rectangle"));
      }
      {
        ViewmapRegionRecord circle = (ViewmapRegionRecord)layout.get(9);
        
        ViewmapDrawingObject draw = circle.getDrawingObject();
        assertEquals(435, draw.getObjRect().getLeft());
        assertEquals(129, draw.getObjRect().getTop());
        assertEquals(553, draw.getObjRect().getRight());
        assertEquals(247, draw.getObjRect().getBottom());
        assertEquals(EnumSet.of(ViewmapDrawingObject.Flag.VISIBLE, ViewmapDrawingObject.Flag.SELECTABLE), draw.getFlags());
        assertEquals(StandardFonts.SWISS, draw.getFontID().getStandardFont().get());

        assertEquals(StandardColors.Red, circle.getLineColor().get());
        assertEquals(NavigatorLineStyle.SOLID, circle.getLineStyle());
        assertEquals(2, circle.getLineWidth());
        assertEquals(NavigatorFillStyle.TRANSPARENT, circle.getFillStyle());
        
        assertEquals("HotspotCircle1", circle.getName());
        assertEquals("", circle.getLabel());
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(10);
        assertEquals(StandardColors.Red, action.getHighlightOutlineColor().get());
        assertEquals(StandardColors.Black, action.getHighlightFillColor().get());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(ViewmapActionRecord.Action.RUNFORMULA, action.getClickAction().get());
        assertTrue(action.getActionFormula().get().equals("@StatusBar(\"I am a formula circle.\")"));
      }
      {
        ViewmapPolygonRecordByte poly = (ViewmapPolygonRecordByte)layout.get(11);
        assertArrayEquals(new int[] { 123, 231, 304, 248, 269 }, poly.getPoints());
        assertEquals("HotspotPolygon1", poly.getName());
      }
      {
        ViewmapActionRecord action = (ViewmapActionRecord)layout.get(12);
        assertEquals(StandardColors.Red, action.getHighlightOutlineColor().get());
        assertEquals(StandardColors.Black, action.getHighlightFillColor().get());
        assertEquals(NavigatorLineStyle.SOLID, action.getHighlightOutlineStyle());
        assertEquals(ViewmapActionRecord.Action.SWITCHVIEW, action.getClickAction().get());
        assertTrue(action.getActionString().get().equals("other alias"));
      }
    }
    
  }
}
