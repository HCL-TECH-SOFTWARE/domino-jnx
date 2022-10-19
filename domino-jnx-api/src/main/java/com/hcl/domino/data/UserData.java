package com.hcl.domino.data;

/**
 * Represents arbitrary binary data stored in a note item, as identified
 * by an arbitrary format name.
 * 
 * @since 1.12.0
 */
public interface UserData {
  /**
   * Retrieves the format name set for this user data.
   * 
   * @return the data format name
   */
  String getFormatName();
  
  /**
   * Retrieves the arbitrary data set for this as a byte array.
   * 
   * @return the user data as a byte array
   */
  byte[] getData();
}
