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
package com.hcl.domino.freebusy;

import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.DominoDateTime;

public interface ScheduleEntry {
  String getApptUnid();

  Set<ScheduleAttr> getAttributes();

  Optional<DominoDateTime> getFrom();

  String getUnid();

  Optional<DominoDateTime> getUntil();

  boolean hasAttribute(ScheduleAttr attr);

  boolean isAppointment();

  boolean isBusy();

  boolean isNonWork();

  boolean isPenciled();

  boolean isRepeatEvent();

}
