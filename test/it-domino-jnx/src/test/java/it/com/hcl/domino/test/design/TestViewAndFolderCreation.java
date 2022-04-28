package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntry.SpecialValue;
import com.hcl.domino.data.CollectionSearchQuery.CollectionEntryProcessor;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.data.DominoCollection.Direction;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.Folder;
import com.hcl.domino.design.View;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestViewAndFolderCreation extends AbstractNotesRuntimeTest {

  private void dumpCollection(DominoCollection collection) {
    List<String> entryList = collection.query()
    .readUNID()
    .readColumnValues()
    .readSpecialValues(SpecialValue.INDEXPOSITION)
    .build(0, Integer.MAX_VALUE, new CollectionEntryProcessor<List<String>>() {

      @Override
      public List<String> start() {
        return new ArrayList<>();
      }

      @Override
      public Action entryRead(List<String> result, CollectionEntry entry) {
        result.add(entry.getUNID() + "\t" + "indentlevel="+entry.getIndentLevel()+ "\t" + new HashMap<>(entry));
        return Action.Continue;
      }

      @Override
      public List<String> end(List<String> result) {
        return result;
      }
    });
    
    System.out.println(entryList.stream().collect(Collectors.joining("\n")));
  }
  
  @Test
  public void testCreateFlatView() throws Exception {
    withTempDb((db) -> {
      DominoClient client = getClient();
      DbDesign design = db.getDesign();
      View viewPeople = design.createView("People");
      viewPeople.setSelectionFormula("Form=\"Person\"");
      
      //two columns: lastname (ascending) / firstname (ascending)
      
      viewPeople.addColumn("Lastname_title", "itm_lastname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Lastname");
        col
        .getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false)
        //can be resorted in descending order; uses firstname column as secondary sorting (ascending)
        .setResortDescending(true)
        .setSecondaryResort(true)
        .setSecondaryResortDescending(false)
        .setSecondResortColumnIndex(1);
      });
      
      viewPeople.addColumn("Firstname_title", "itm_firstname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Firstname");
        col
        .getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false);
        
      });
      
      viewPeople.save();
      
      String unid1;
      {
        unid1 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "Peter")
            .save()
            .getUNID();
      }

      String unid2;
      {
        unid2 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "John")
            .save()
            .getUNID();
      }

      String unid3;
      {
        unid3 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Abbott")
            .replaceItemValue("Firstname", "Georg")
            .save()
            .getUNID();
      }

      String unid4;
      {
        unid4 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Abert")
            .replaceItemValue("Firstname", "Martin")
            .save()
            .getUNID();
      }

      String unid5;
      {
        unid5 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "Reginald")
            .save()
            .getUNID();
      }

      //create doc with different form to check if selection formula is working
      String unid6;
      {
        unid6 = db
            .createDocument()
            .replaceItemValue("Form", "OtherForm")
            .replaceItemValue("Lastname", "Diner")
            .replaceItemValue("Firstname", "Paul")
            .save()
            .getUNID();
      }

      DominoCollection collection = viewPeople.getCollection();
//      dumpCollection(collection);
      
      List<String> unidsFromView = collection
          .getAllIds(true, false)
          .stream()
          .map(db::toUNID)
          .collect(Collectors.toList());
      assertEquals(5, unidsFromView.size());
      
      assertEquals(Arrays.asList(
          unid3, // Abbott, Georg
          unid4, // Abert, Martin
          unid2, // Miller, John
          unid1, // Miller, Peter,
          unid5 // Miller, Reginald
          ), unidsFromView);
      
//      System.out.println("***");
      
      collection.resortView("itm_lastname", Direction.Descending);
//      dumpCollection(collection);

      List<String> unidsFromResortedView = collection
          .getAllIds(true, false)
          .stream()
          .map(db::toUNID)
          .collect(Collectors.toList());

      assertEquals(5, unidsFromResortedView.size());

      assertEquals(Arrays.asList(
          unid2, // Miller, John
          unid1, // Miller, Peter
          unid5, // Miller, Reginald
          unid4, // Abert, Martin
          unid3 // Abbott, Georg
          ), unidsFromResortedView);
      
    });
  }

  @Test
  public void testCreateCategorizedView() throws Exception {
    withTempDb((db) -> {
      DominoClient client = getClient();
      DbDesign design = db.getDesign();
      View viewPeople = design.createView("People");
      viewPeople.setSelectionFormula("Form=\"Person\"");
      
      //three columns: first letter of lastname (ascending) / lastname (ascending) / firstname (ascending)
      
      viewPeople.addColumn("Lastname_category", "itm_lastnamecat", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("@Left(Lastname;1)");
        col.getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false)
        .setCategory(true)
        .setSortPermuted(true);
      });

      viewPeople.addColumn("Lastname_title", "itm_lastname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Lastname");
        col.getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false)
        .setCategory(true)
        .setSortPermuted(true);
      });

      viewPeople.addColumn("Firstname_title", "itm_firstname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Firstname");
        col
        .getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false);
        
      });
      
      viewPeople.save();
      
      String unid1;
      {
        unid1 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "Peter")
            .save()
            .getUNID();
      }

      String unid2;
      {
        unid2 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "John")
            .save()
            .getUNID();
      }

      String unid3;
      {
        unid3 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Abbott")
            .replaceItemValue("Firstname", "Georg")
            .save()
            .getUNID();
      }

      DominoCollection collection = viewPeople.getCollection();
//      dumpCollection(collection);
      
      List<CollectionEntry> entries = collection.query()
          .readColumnValues()
          .readUNID()
          .readSpecialValues(SpecialValue.CHILDCOUNT, SpecialValue.INDEXPOSITION)
          .collectEntries(0, Integer.MAX_VALUE);
      Iterator<CollectionEntry> entriesIt = entries.iterator();
      
      {
        CollectionEntry catEntryA = entriesIt.next();
        assertTrue(catEntryA.isCategory());
        catEntryA.get("itm_lastnamecat", String.class, "");
        
        assertEquals("A", catEntryA.get("itm_lastnamecat", String.class, ""));
        assertEquals("00000000000000000000000000000000", catEntryA.getUNID());
        assertEquals(1, catEntryA.getIndentLevel());
        assertEquals(1, catEntryA.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }
      
      {
        CollectionEntry catEntryAbbott = entriesIt.next();
        assertTrue(catEntryAbbott.isCategory());
        assertEquals("Abbott", catEntryAbbott.get("itm_lastname", String.class, ""));
        assertEquals("00000000000000000000000000000000", catEntryAbbott.getUNID());
        assertEquals(2, catEntryAbbott.getIndentLevel());
        assertEquals(1, catEntryAbbott.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }
      
      {
        CollectionEntry docEntryAbbott = entriesIt.next();
        assertTrue(docEntryAbbott.isDocument());
        assertEquals("Abbott", docEntryAbbott.get("itm_lastname", String.class, ""));
        assertEquals("Georg", docEntryAbbott.get("itm_firstname", String.class, ""));
        assertEquals(unid3, docEntryAbbott.getUNID());
        assertEquals(3, docEntryAbbott.getIndentLevel());
        assertEquals(0, docEntryAbbott.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }
      
      {
        CollectionEntry catEntryM = entriesIt.next();
        assertTrue(catEntryM.isCategory());
        assertEquals("M", catEntryM.get("itm_lastnamecat", String.class, ""));
        assertEquals("00000000000000000000000000000000", catEntryM.getUNID());
        assertEquals(1, catEntryM.getIndentLevel());
        assertEquals(1, catEntryM.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }
      
      {
        CollectionEntry catEntryMiller = entriesIt.next();
        assertTrue(catEntryMiller.isCategory());
        assertEquals("Miller", catEntryMiller.get("itm_lastname", String.class, ""));
        assertEquals("00000000000000000000000000000000", catEntryMiller.getUNID());
        assertEquals(2, catEntryMiller.getIndentLevel());
        assertEquals(2, catEntryMiller.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }

      {
        CollectionEntry docEntryMillerJohn = entriesIt.next();
        assertTrue(docEntryMillerJohn.isDocument());
        assertEquals("Miller", docEntryMillerJohn.get("itm_lastname", String.class, ""));
        assertEquals("John", docEntryMillerJohn.get("itm_firstname", String.class, ""));
        assertEquals(unid2, docEntryMillerJohn.getUNID());
        assertEquals(3, docEntryMillerJohn.getIndentLevel());
        assertEquals(0, docEntryMillerJohn.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }

      {
        CollectionEntry docEntryMillerPeter = entriesIt.next();
        assertTrue(docEntryMillerPeter.isDocument());
        assertEquals("Miller", docEntryMillerPeter.get("itm_lastname", String.class, ""));
        assertEquals("Peter", docEntryMillerPeter.get("itm_firstname", String.class, ""));
        assertEquals(unid1, docEntryMillerPeter.getUNID());
        assertEquals(3, docEntryMillerPeter.getIndentLevel());
        assertEquals(0, docEntryMillerPeter.getSpecialValue(SpecialValue.CHILDCOUNT, Integer.class, -1));
      }

      assertFalse(entriesIt.hasNext());
    });
  }

  @Test
  public void testCreateFolder() throws Exception {
    withTempDb((db) -> {
      DominoClient client = getClient();
      DbDesign design = db.getDesign();
      Folder folderPeople = design.createFolder("People");
      
      //two columns: lastname (ascending) / firstname (ascending)
      
      folderPeople.addColumn("Lastname_title", "itm_lastname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Lastname");
        col
        .getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false)
        .setResortDescending(true);
      });
      
      folderPeople.addColumn("Firstname_title", "itm_firstname", (col) -> {
        col.setDisplayWidth(15 * 8);
        col.setFormula("Firstname");
        col
        .getSortConfiguration()
        .setSorted(true)
        .setSortedDescending(false);
        
      });

      folderPeople.save();
      
      int id1;
      {
        id1 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "Peter")
            .save()
            .getNoteID();
      }

      int id2;
      {
        id2 = db
            .createDocument()
            .replaceItemValue("Form", "Person")
            .replaceItemValue("Lastname", "Miller")
            .replaceItemValue("Firstname", "John")
            .save()
            .getNoteID();
      }

      //create doc with different form to check if folder only contains what has been added
      int id3;
      {
        id3 = db
            .createDocument()
            .replaceItemValue("Form", "OtherForm")
            .replaceItemValue("Lastname", "Diner")
            .replaceItemValue("Firstname", "Paul")
            .save()
            .getNoteID();
      }

      db.addToFolder("People", Arrays.asList(id1, id2));
      
      DominoCollection collection = folderPeople.getCollection();
      
      List<Integer> idsFromView = collection
          .getAllIds(true, false)
          .stream()
          .collect(Collectors.toList());
      assertEquals(2, idsFromView.size());

      assertEquals(Arrays.asList(id2, id1), idsFromView);
      
      collection.resortView("itm_lastname", Direction.Descending);
      
      List<Integer> idsFromResortedView = collection
          .getAllIds(true, false)
          .stream()
          .collect(Collectors.toList());
      assertEquals(2, idsFromResortedView.size());

      assertEquals(Arrays.asList(id1, id2), idsFromResortedView);

    });
  }
}
