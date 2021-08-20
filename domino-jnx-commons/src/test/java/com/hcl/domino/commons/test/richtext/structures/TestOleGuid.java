package com.hcl.domino.commons.test.richtext.structures;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.richtext.structures.OLE_GUID;

@SuppressWarnings("nls")
public class TestOleGuid {

  @Test
  public void testGuidString() {
    OLE_GUID guid = MemoryStructureUtil.newStructure(OLE_GUID.class, 0);
    guid.setGuidString("00112233-4455-6677-8899-aabbccddeeff");
    assertEquals("00112233-4455-6677-8899-aabbccddeeff", guid.toGuidString());
    assertEquals(0x00112233, guid.getData1());
    assertEquals(0x4455, guid.getData2());
    assertEquals(0x6677, guid.getData3());
    assertArrayEquals(
      new byte[] { (byte)0x88, (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff },
      guid.getData4()
   );
  }

}
