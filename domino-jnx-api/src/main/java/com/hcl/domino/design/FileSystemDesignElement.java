package com.hcl.domino.design;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a design element that participates in the virtual filesystem
 * view of the NSF.
 * 
 * @author Jesse Gallagher
 * @since 1.0.39
 */
public interface FileSystemDesignElement extends DesignElement {
  InputStream getFileData();
  
  /**
   * Opens a new output stream to replace the content of the file.
   * 
   * <p>Note: it is not guaranteed that the data written to this stream
   * will be saved to the resource until {@link OutputStream#close()} is
   * called.</p>
   * 
   * @return a new {@link OutputStream} that writes new data to replace
   *         the content of the file
   * @since 1.0.39
   */
  OutputStream newOutputStream();
}
