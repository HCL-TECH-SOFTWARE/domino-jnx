package com.hcl.domino.commons.design.view;

import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewCalendarFormat2;

/**
 * Contains format information related to calendar views.
 * 
 * @author Jesse Gallagher
 * @since 1.0.41
 */
public class DominoCalendarFormat implements IAdaptable {

  private ViewCalendarFormat format1;
  private ViewCalendarFormat2 format2;
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Class<T> clazz) {
    if(ViewCalendarFormat.class.equals(clazz)) {
      return (T)format1;
    } else if(ViewCalendarFormat2.class.equals(clazz)) {
      return (T)format2;
    }
    return null;
  }

  // *******************************************************************************
  // * CalendarLayout-reader hooks
  // *******************************************************************************
  
  public void read(ViewCalendarFormat format1) {
    this.format1 = format1;
  }
  
  public void read(ViewCalendarFormat2 format2) {
    this.format2 = format2;
  }

}
