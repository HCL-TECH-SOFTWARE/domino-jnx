package com.hcl.domino.data;

import com.hcl.domino.richtext.structures.LicenseID;

/**
 * Represents the "V1V2_Author" pseudo-structure used for {@link ItemDataType#TYPE_USERID}
 * values in pre-V3 documents.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
public interface PreV3Author {
  
  /**
   * Retrieves the user name stored in the structure.
   * 
   * @return a string user name
   */
  String getName();
  
  /**
   * Retrieves the semi-opaque license ID information stored
   * with the name.
   * 
   * @return a {@link LicenseID} instance
   */
  LicenseID getLicenseID();
}
