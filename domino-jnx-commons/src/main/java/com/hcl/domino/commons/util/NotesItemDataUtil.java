package com.hcl.domino.commons.util;

import com.hcl.domino.commons.data.DefaultPreV3Author;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.PreV3Author;
import com.hcl.domino.richtext.structures.LicenseID;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Contains methods for interpreting raw Notes data in memory.
 * 
 * @author Jesse Gallagher
 * @since 1.0.42
 */
public enum NotesItemDataUtil {
  ;
  
  public static PreV3Author parsePreV3Author(ByteBuffer buf) {
    // Last 8 bytes are a LicenseID - the rest are the username
    
    byte[] lmbcs = new byte[buf.remaining()-8];
    buf.get(lmbcs);
    String name = new String(lmbcs, Charset.forName("LMBCS")); //$NON-NLS-1$
    
    byte[] licenseBytes = new byte[8];
    buf.get(licenseBytes);
    ByteBuffer licenseBuf = ByteBuffer.wrap(licenseBytes);
    LicenseID license = MemoryStructureUtil.forStructure(LicenseID.class, () -> licenseBuf);
    
    return new DefaultPreV3Author(name, license);
  }
}
