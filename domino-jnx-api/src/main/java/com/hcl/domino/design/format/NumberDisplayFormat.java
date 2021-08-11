package com.hcl.domino.design.format;

import com.hcl.domino.richtext.structures.NFMT;

/**
 * Represents the user-interface options for displaying numbers in collection
 * columns and form fields.
 * 
 * <p>This value is technically derived data based on the type and attributes
 * of the {@link NFMT} structure.</p>
 * 
 * @author Jesse Gallagher
 * @since 1.0.32
 */
public enum NumberDisplayFormat {
  DECIMAL,
  PERCENT,
  SCIENTIFIC,
  CURRENCY,
  BYTES
}
