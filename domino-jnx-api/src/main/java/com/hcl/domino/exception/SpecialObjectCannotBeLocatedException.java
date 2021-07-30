package com.hcl.domino.exception;

import com.hcl.domino.DominoException;

/**
 * This {@link DominoException} subclass represents error code 0x0242,
 * "Special database object cannot be located".
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public class SpecialObjectCannotBeLocatedException extends DominoException {
  private static final long serialVersionUID = 1L;

  public SpecialObjectCannotBeLocatedException(int id, String message) {
    super(id, message);
  }

  public SpecialObjectCannotBeLocatedException(int id, String message, Throwable cause) {
    super(id, message, cause);
  }

  public SpecialObjectCannotBeLocatedException(String msg) {
    super(msg);
  }

  public SpecialObjectCannotBeLocatedException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
