package it.com.hcl.domino.test.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Database;
import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.design.DbDesign;
import com.hcl.domino.design.View;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestPrivateViews extends AbstractNotesRuntimeTest {

  /**
   * This method create two temporary databases: dbData that will be used to store
   * documents and dbView that we use to create a view.<br>
   * The view is opened via {@link Database#openCollection(Database, int)} which tells
   * NIF to read the view data from another database. So the view acts like a
   * private view. Opening such a view in the Notes user interface destroys the view
   * index, so the entries will magically disappear.<br>
   * Please note that based on our tests, read access writes will not be transferred
   * to the view, so it only represents what the current database owner is allowed to set.
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testCreatePrivateView() throws Exception {
    withTempDb((dbData) -> {
      withTempDb((dbView) -> {
        //**********************************
        //test initial view index creation:
        //**********************************

        Map<String,Integer> sampleDocUnidsAndNoteIds = new HashMap<>();

        //create some sample docs in dbData
        
        generateNABPersons(dbData, 150)
        .stream()
        .forEach((p) -> {
          sampleDocUnidsAndNoteIds.put(p.getValue1(), p.getValue2());
        });

        //create a new view in dbView

        DbDesign design = dbView.getDesign();
        View view = design.createView("Testview")
            .addColumn("Lastname", "Lastname", (col) -> {
              col.setFormula("Lastname");
              col.getSortConfiguration()
              .setSorted(true)
              .setSortedDescending(false);
            })
            .addColumn("Firstname", "Firstname", (col) -> {
              col.setFormula("Firstname");
              col.getSortConfiguration()
              .setSorted(true)
              .setSortedDescending(false);
            });
        view.save();

        //and let it pull the data from dbData

        DominoCollection collection = dbView.openCollection(view.getNoteID(), dbData).get();

        //let's see what the view contains now
        Map<String,Integer> viewEntryUnidsAndNoteIDs = new HashMap<>();
        {
          collection
          .query()
          .readColumnValues()
          .readUNID()
          .collectEntries(0, Integer.MAX_VALUE)
          .forEach((e) -> {
            viewEntryUnidsAndNoteIDs.put(e.getUNID(), e.getNoteID());
            
            String firstName = e.get("firstname", String.class, "");
            assertFalse(firstName.isEmpty());
            
            String lastName = e.get("lastname", String.class, "");
            assertFalse(lastName.isEmpty());
            
          });
        }

        //all sample docs found?
        assertEquals(sampleDocUnidsAndNoteIds, viewEntryUnidsAndNoteIDs);

        //*******************************
        //test incremental view update:
        //*******************************

        //we produce a few more docs in dbData
        Map<String,Integer> moreSampleDocUnidsAndNoteIds = new HashMap<>();

        generateNABPersons(dbData, 3)
        .forEach((e) -> {
          moreSampleDocUnidsAndNoteIds.put(e.getValue1(), e.getValue2());
        });

        //let the collection pull the incremental changes:
        collection.refresh();

        //and make sure the view contains the old and new sample docs
        
        Map<String,Integer> viewEntryUnidsAndNoteIDs2 = new HashMap<>();
        {
          collection
          .query()
          .readColumnValues()
          .readUNID()
          .collectEntries(0, Integer.MAX_VALUE)
          .forEach((e) -> {
            viewEntryUnidsAndNoteIDs2.put(e.getUNID(), e.getNoteID());
            
            String firstName = e.get("firstname", String.class, "");
            assertFalse(firstName.isEmpty());
            
            String lastName = e.get("lastname", String.class, "");
            assertFalse(lastName.isEmpty());
          });
        }
        
        Map<String,Integer> allSampleDocUnidsAndNoteIds = new HashMap<>();
        allSampleDocUnidsAndNoteIds.putAll(sampleDocUnidsAndNoteIds);
        allSampleDocUnidsAndNoteIds.putAll(moreSampleDocUnidsAndNoteIds);
        
        assertEquals(sampleDocUnidsAndNoteIds.size() + moreSampleDocUnidsAndNoteIds.size(), allSampleDocUnidsAndNoteIds.size());
        
        assertEquals(viewEntryUnidsAndNoteIDs2, allSampleDocUnidsAndNoteIds);
      });

    });
  }
}
