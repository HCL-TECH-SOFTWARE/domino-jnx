package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code vmods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.1.2
 */
public interface VmOds {

  short VM_ACTION_NONE = 0;
  short VM_ACTION_SWITCHVIEW = 1;
  short VM_ACTION_SWITCHNAV = 2;
  short VM_ACTION_ALIAS_FOLDER = 3;
  short VM_ACTION_GOTO_LINK = 4;
  short VM_ACTION_RUNSCRIPT = 5;
  short VM_ACTION_RUNFORMULA = 6;
  short VM_ACTION_GOTO_URL = 8;

  short VM_LINE_SOLID = 0;
  short VM_FILL_SOLID = 1;
}
