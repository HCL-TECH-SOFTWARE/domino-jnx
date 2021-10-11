package it.com.hcl.domino.test.poc;

import com.hcl.domino.*;
import com.hcl.domino.calendar.*;
import com.hcl.domino.data.*;
import com.hcl.domino.design.*;
import com.hcl.domino.dxl.*;
import com.hcl.domino.jna.calendaring.*;
import com.hcl.domino.jna.data.*;
import com.hcl.domino.jna.internal.gc.handles.*;
import it.com.hcl.domino.test.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Karsakov_S created on 05.10.2021
 */

public class TestDxlImport extends AbstractNotesRuntimeTest {

    @Test
    public void testImportFile() throws Exception {
        System.out.println();
        this.withTempDb(db -> {
            final DxlImporter importer = this.getClient().createDxlImporter();
            InputStream is = this.getClass().getResourceAsStream("/dxl/testPoc/infoK.xml");
            importer.importDxl(is, db);
            Optional<UserNamesList> namesList = db.getNamesList();
            DHANDLE.ByReference rethNote = DHANDLE.newInstanceByReference();
            DocumentSelection documentSelection = db.createDocumentSelection();
            JNACalendaring calendaring = (JNACalendaring)this.getClient().getCalendaring();
            List<View> collect = db.getDesign().getViews().collect(Collectors.toList());
            System.out.println();

        });
        System.out.println();
    }
}
