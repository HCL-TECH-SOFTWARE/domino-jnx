package com.hcl.domino.commons.data;

import com.hcl.domino.data.UserData;

/**
 * Default implementation of {@link UserData} that stores the data
 * in memory.
 * 
 * <p>This method does not make a copy of the passed-in data array,
 * so users should be wary of manipulating the array after passing
 * it to this object.</p>
 * 
 * @since 1.12.0
 */
public class DefaultUserData implements UserData {
  private final String formatName;
  private final byte[] data;
  
  public DefaultUserData(String formatName, byte[] data) {
    this.formatName = formatName;
    this.data = data;
  }

  @Override
  public String getFormatName() {
    return this.formatName;
  }

  @Override
  public byte[] getData() {
    return this.data;
  }

}
