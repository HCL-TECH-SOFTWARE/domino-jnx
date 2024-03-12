package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code bsafe.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.29.0
 */
public interface Stats {
  /*    Value type constants */

  short VT_LONG = 0;
  short VT_TEXT = 1;
  short VT_TIMEDATE = 2;
  short VT_NUMBER = 3;

  /*  Flags for StatUpdate */
  /** Statistic is unique */
  short ST_UNIQUE = 0x0001;
  /** Add to VT_LONG statistic, don't replace */
  short ST_ADDITIVE = 0x0002;
  /** Statistic is resetable to 0 */
  short ST_RESETABLE = 0x0003;
}
