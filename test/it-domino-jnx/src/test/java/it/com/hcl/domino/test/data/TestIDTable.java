package it.com.hcl.domino.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.IDTable;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

public class TestIDTable extends AbstractNotesRuntimeTest {

  /**
   * Tests two different ways of reading the note ids from an IDTable:
   * via IDEnumerate and IDScan. Both must return the same note ids.<br>
   * <br>
   * We had a bug in the past where the note ids where not added correctly into the IDTable
   * via IDInsertRange, so that category note ids got added <i>before</i> documents note ids instead
   * of being added <i>after</i> document note ids.<br>
   * These note ids then did get returned via IDEnumerate,
   * but not via IDScan, because IDScan stopped searching for the note ids too early.
   * 
   * @throws Exception in case of errors
   */
  @Test
  public void testIDTableInsertion() throws Exception {
    final DominoClient client = this.getClient();

    //negative note ids are category note ids
    int[] noteIdsToInsert = new int[] {-2147478912, 299462, 193030, 192518, -2147478908, -2147478904, 192330, 229642, 248586, 4618, -2147478900, 238094, 248590, -2147478896, 192402, 242898, -2147478892, 192538, -2147478888, 232794, -2147478884, -2147478880, 193382, 193958, 242790, 192554, 194858, 194026, 192622, 224494, 238130, 194038, 283698, -2147478916};
    
    List<Integer> noteIdsToInsertAsList = Arrays
        .stream(noteIdsToInsert)
        .mapToObj((id) -> { return Integer.valueOf(id); })
        .collect(Collectors.toList());
    
    IDTable idTable = client.createIDTable();
    idTable.addAll(noteIdsToInsertAsList);

    //uses IDEnumerate to read the note ids:
    List<Integer> idsViaToArray = Arrays.stream(idTable.toArray(new Integer[idTable.size()])).collect(Collectors.toList());
    
    //uses IDScan to read the note ids:
    List<Integer> idsViaIterator = StreamSupport.stream(idTable.spliterator(), false).collect(Collectors.toList());
    
    assertEquals(idsViaToArray, idsViaIterator);
    
    assertEquals(noteIdsToInsertAsList.size(), idsViaToArray.size());
    
    //make sure all ids from noteIdsToInsertAsList are in idsViaToArray
    for (Integer currId : noteIdsToInsertAsList) {
      assertTrue(idsViaToArray.contains(currId));
    }

  }

}
