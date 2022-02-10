/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.commons.design.view;

import java.util.Optional;

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
  
  public DominoCalendarFormat() {
    format1 = ViewCalendarFormat.newInstanceWithDefaults();
  }
  
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

  public ViewCalendarFormat getFormat1() {
    return this.format1;
  }
  
  public Optional<ViewCalendarFormat2> getFormat2(boolean createIfMissing) {
    if (this.format2==null && createIfMissing) {
      this.format2 = ViewCalendarFormat2.newInstanceWithDefaults();
    }
    
    return Optional.ofNullable(this.format2);
  }
}
