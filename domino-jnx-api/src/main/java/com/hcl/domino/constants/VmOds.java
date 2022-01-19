package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code vmods.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.1.2
 */
public interface VmOds {
  /** Set if obj is visible  */
  short VM_DROBJ_FLAGS_VISIBLE = 0x0002;
  /** Set if obj can be select (i.e. is not background) */
  short VM_DROBJ_FLAGS_SELECTABLE = 0x0004;
  /**  Set if obj can't be edited  */
  short VM_DROBJ_FLAGS_LOCKED = 0x0008;
  /**
   * Bitmap representing runtime image of the navigator.  Use to create
   * imagemaps from navigators.
   */
  short VM_DROBJ_FLAGS_IMAGEMAP_BITMAP = 0x0010;
  /**  reading order  */
  short VM_DSET_READING_ORDER_RTL = 0x0020;
  
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
