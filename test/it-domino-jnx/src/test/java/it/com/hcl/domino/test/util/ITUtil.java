package it.com.hcl.domino.test.util;

import com.ibm.commons.util.StringUtil;

public enum ITUtil {
  ;
  
  /**
   * Converts Windows-style CRLF line endings to just LF.
   * 
   * @param value the value to convert
   * @return the converted value
   * @since 1.0.43
   */
  public static String toLf(String value) {
    return StringUtil.toString(value).replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
